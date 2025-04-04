#!/bin/bash

# Configuration
CSV_FILE="data/Taxonomy1.csv"  # default value
TAXONOMY_ID="tax-001"         # default value
BASE_URL="http://localhost:9000"
LOGS_DIR="logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOGS_DIR/taxonomy_creation_$TIMESTAMP.log"
CURL_COMMANDS_FILE="$LOGS_DIR/curl_commands_$TIMESTAMP.txt"
ROWS_FILE="$LOGS_DIR/taxonomy_rows_$TIMESTAMP.json"
DRY_RUN=""
DIMENSION_NAME="account"
DEBUG_MODE=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to show usage
show_usage() {
    echo "Usage: $0 [csv_file] [taxonomy_id] [options]"
    echo
    echo "Options:"
    echo "  --dry-run [MODE]      Generate output without executing commands"
    echo "                        MODE can be:"
    echo "                          commands  - Show curl commands that would be executed (default)"
    echo "                          rows      - Show taxonomy rows that would be created"
    echo "  --dimension NAME      Specify the dimension name (default: account)"
    echo "  --debug              Enable detailed debug output"
    echo "  -h, --help           Show this help message"
}

# Function for debug output
debug_echo() {
    if [ "$DEBUG_MODE" = true ]; then
        echo "$@" | tee -a "$LOG_FILE"
    fi
    # If debug mode is false, don't output anything
}

# Debug: Print all arguments
debug_echo "All arguments: $@"

# Parse arguments
FOUND_CSV=false
while [[ $# -gt 0 ]]; do
    debug_echo "Processing argument: $1"
    case $1 in
        --debug)
            DEBUG_MODE=true
            echo "Debug mode enabled"
            shift
            ;;
        --dry-run)
            if [ -z "$2" ] || [[ "$2" =~ ^(-|--).* ]]; then
                DRY_RUN="commands"
                echo "Setting dry run to default: commands"
                shift
            elif [[ "$2" =~ ^(commands|rows)$ ]]; then
                DRY_RUN="$2"
                echo "Setting dry run to: $2"
                shift 2
            else
                echo -e "${RED}Error: --dry-run mode must be 'commands' or 'rows'${NC}"
                exit 1
            fi
            ;;
        --dimension)
            if [ -n "$2" ]; then
                DIMENSION_NAME="$2"
                echo "Setting dimension to: $2"
                shift 2
            else
                echo -e "${RED}Error: --dimension requires a name${NC}"
                exit 1
            fi
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        --*)
            echo -e "${RED}Error: Unknown option $1${NC}"
            show_usage
            exit 1
            ;;
        *)
            if [ "$FOUND_CSV" = false ]; then
                CSV_FILE="$1"
                FOUND_CSV=true
                echo "Setting CSV_FILE to: $1"
            else
                TAXONOMY_ID="$1"
                echo "Setting TAXONOMY_ID to: $1"
            fi
            shift
            ;;
    esac
done

# Debug: Print final values
debug_echo "Final values:"
debug_echo "CSV_FILE: $CSV_FILE"
debug_echo "TAXONOMY_ID: $TAXONOMY_ID"
debug_echo "DRY_RUN: $DRY_RUN"
debug_echo "DIMENSION_NAME: $DIMENSION_NAME"

# Create logs directory if it doesn't exist
mkdir -p "$LOGS_DIR"

# Check if csv file exists
if [ ! -f "$CSV_FILE" ]; then
    echo -e "${RED}Error: CSV file '$CSV_FILE' not found${NC}"
    exit 1
fi

echo "=== Starting Taxonomy Creation from CSV $(date) ===" | tee "$LOG_FILE"
echo "Dimension Name: $DIMENSION_NAME" | tee -a "$LOG_FILE"
if [ -n "$DRY_RUN" ]; then
    echo "DRY RUN MODE ($DRY_RUN) - No commands will be executed" | tee -a "$LOG_FILE"
fi

# Create the taxonomy creation curl command
TAXONOMY_CREATE_JSON=$(cat <<EOF
{
    "dimensionName": "$DIMENSION_NAME",
    "name": "CSV Based Taxonomy",
    "description": "Taxonomy created from CSV file",
    "version": "1.0"
}
EOF
)

CREATE_COMMAND="curl -X POST \"$BASE_URL/taxonomy/$TAXONOMY_ID\" \\
    -H \"Content-Type: application/json\" \\
    -d '$TAXONOMY_CREATE_JSON'"

# Function to generate a UUID
generate_uuid() {
    python3 -c 'import uuid; print(str(uuid.uuid4()))'
}

# Process CSV and create rows JSON
echo -e "${YELLOW}Processing CSV file and creating taxonomy rows...${NC}" | tee -a "$LOG_FILE"

# Initialize variables
rows_json="["
first=true
row_count=0
csv_line_count=0
duplicate_count=0

# Track existing values to prevent duplicates - using temp file for compatibility
EXISTING_VALUES_FILE=$(mktemp)

# Debug: Show CSV content with line numbers for troubleshooting
debug_echo "CSV file content with line numbers:"
if [ "$DEBUG_MODE" = true ]; then
    nl -ba "$CSV_FILE" | tee -a "$LOG_FILE"
fi
debug_echo "---"

# Process the CSV file
echo "Processing rows..." | tee -a "$LOG_FILE"

# First line is header
HEADER=$(head -n 1 "$CSV_FILE")
debug_echo "Header line: $HEADER"

# Process each data row directly
while IFS= read -r line || [[ -n "$line" ]]; do
    # Skip empty lines
    [ -z "$line" ] && continue
    
    # Skip header line
    if [ $csv_line_count -eq 0 ]; then
        ((csv_line_count++))
        continue
    fi
    
    ((csv_line_count++))
    debug_echo "=== Processing CSV Line $csv_line_count (data row $((csv_line_count-1))) ==="
    debug_echo "Raw line: '$line'"
    
    # Split line into fields
    IFS=',' read -r level1 level2 level3 level4 level5 level6 <<< "$line"
    
    debug_echo "Raw values:"
    debug_echo "Level1: '$level1'"
    debug_echo "Level2: '$level2'"
    debug_echo "Level3: '$level3'"
    debug_echo "Level4: '$level4'"
    debug_echo "Level5: '$level5'"
    debug_echo "Level6: '$level6'"
    
    # Process each level
    current_parent=""
    for level_var in level1 level2 level3 level4 level5 level6; do
        # Get the value of the variable
        level="${!level_var}"
        
        # Trim whitespace
        level=$(echo "$level" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        debug_echo "Processing level $level_var: '$level'"
        
        # Skip empty levels
        if [ -z "$level" ]; then
            debug_echo "Skipping empty level"
            continue
        fi
        
        # Check if this value already exists by looking for it in the temp file
        existing_id=$(grep "^${level}=" "$EXISTING_VALUES_FILE" | cut -d= -f2)
        
        if [ -z "$existing_id" ]; then
            # New value, create it
            ((row_count++))
            
            # Generate UUID for this row
            row_id=$(generate_uuid)
            debug_echo "Creating row with ID: $row_id"
            
            # Store the row_id for this value in the temp file
            echo "${level}=${row_id}" >> "$EXISTING_VALUES_FILE"
            
            # Create row JSON
            row_json=$(cat <<EOF
{
    "rowId": "$row_id",
    "value": "${level//\"/}",
    "description": "This is the ${level//\"/} $DIMENSION_NAME",
    "aliases": [],
    "keywords": [],
    "dimensionSrcHints": {},
    "parent": $( [ -z "$current_parent" ] && echo "null" || echo "\"$current_parent\"" )
}
EOF
)
            
            # Add to rows_json
            if [ "$first" = true ]; then
                first=false
                rows_json+="$row_json"
            else
                rows_json+=",$row_json"
            fi
            
            debug_echo "Added new row for value: '$level'"
        else
            # Value already exists, use its ID
            row_id="$existing_id"
            ((duplicate_count++))
            debug_echo "Using existing row ID: $row_id for value: '$level' (duplicate #$duplicate_count)"
        fi
        
        # Update current parent for next level
        current_parent="$row_id"
        debug_echo "Updated parent to: $current_parent"
        
        # Print progress
        echo -ne "\rProcessing CSV line $csv_line_count - Created $row_count taxonomy rows (skipped $duplicate_count duplicates)" | tee -a "$LOG_FILE"
    done
    debug_echo ""
done < "$CSV_FILE"

# Clean up temp file
rm -f "$EXISTING_VALUES_FILE"

# Move to new line after progress updates
echo
echo -e "${GREEN}Completed processing: $row_count total taxonomy rows created from $((csv_line_count-1)) CSV lines (skipped $duplicate_count duplicates)${NC}" | tee -a "$LOG_FILE"

# Write debug summary info
if [ "$DEBUG_MODE" = true ]; then
    echo -e "\n${YELLOW}Debug Summary:${NC}" | tee -a "$LOG_FILE"
    echo "CSV file: $CSV_FILE" | tee -a "$LOG_FILE"
    echo "Total data rows: $((csv_line_count-1))" | tee -a "$LOG_FILE"
    echo "Total taxonomy rows created: $row_count" | tee -a "$LOG_FILE"
    echo "Duplicate values skipped: $duplicate_count" | tee -a "$LOG_FILE"
    echo "Average hierarchy depth per row: $(awk "BEGIN {print $row_count/($csv_line_count-1)}")" | tee -a "$LOG_FILE"
fi

rows_json+="]"

# Create the add rows request JSON
ADD_ROWS_JSON=$(cat <<EOF
{
    "rows": $rows_json,
    "isReplace": true
}
EOF
)

# Create the add rows curl command
ADD_ROWS_COMMAND="curl -X POST \"$BASE_URL/taxonomy/$TAXONOMY_ID/rows\" \\
    -H \"Content-Type: application/json\" \\
    -d '$ADD_ROWS_JSON'"

case "$DRY_RUN" in
    "commands")
        echo "# Create Taxonomy" > "$CURL_COMMANDS_FILE"
        echo "$CREATE_COMMAND" >> "$CURL_COMMANDS_FILE"
        echo >> "$CURL_COMMANDS_FILE"
        echo "# Add Taxonomy Rows" >> "$CURL_COMMANDS_FILE"
        echo "$ADD_ROWS_COMMAND" >> "$CURL_COMMANDS_FILE"
        echo -e "${GREEN}Curl commands have been written to: $CURL_COMMANDS_FILE${NC}"
        ;;
    "rows")
        echo "# Taxonomy Creation JSON" > "$ROWS_FILE"
        echo "$TAXONOMY_CREATE_JSON" >> "$ROWS_FILE"
        echo >> "$ROWS_FILE"
        echo "# Taxonomy Rows JSON" >> "$ROWS_FILE"
        echo "$ADD_ROWS_JSON" | python3 -m json.tool >> "$ROWS_FILE"
        echo -e "${GREEN}Row data has been written to: $ROWS_FILE${NC}"
        ;;
    "")
        # Execute commands
        echo -e "${YELLOW}Creating taxonomy with ID: $TAXONOMY_ID${NC}" | tee -a "$LOG_FILE"
        CREATE_RESPONSE=$(eval "$CREATE_COMMAND")
        if [ "$DEBUG_MODE" = true ]; then
            echo "Create Response: $CREATE_RESPONSE" | tee -a "$LOG_FILE"
        else
            echo "Create Response: $CREATE_RESPONSE" >> "$LOG_FILE"
        fi
        
        echo -e "${YELLOW}Adding rows to taxonomy...${NC}" | tee -a "$LOG_FILE"
        ADD_ROWS_RESPONSE=$(eval "$ADD_ROWS_COMMAND")
        if [ "$DEBUG_MODE" = true ]; then
            echo "Add Rows Response: $ADD_ROWS_RESPONSE" | tee -a "$LOG_FILE"
        else
            echo "Add Rows Response: $ADD_ROWS_RESPONSE" >> "$LOG_FILE"
        fi
        ;;
esac

echo -e "${GREEN}Completed processing CSV file${NC}" | tee -a "$LOG_FILE"
echo "Log file: $LOG_FILE"
if [ -n "$DRY_RUN" ]; then
    echo "Output file: $([ "$DRY_RUN" = "commands" ] && echo "$CURL_COMMANDS_FILE" || echo "$ROWS_FILE")"
fi 