#!/bin/bash

# Configuration
TAXONOMY_ID=""         # default empty, will be required
BASE_URL="http://localhost:9000"
LOGS_DIR="logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOGS_DIR/taxonomy_deletion_$TIMESTAMP.log"
DRY_RUN=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to show usage
show_usage() {
    echo "Usage: $0 <taxonomy_id> [options]"
    echo
    echo "Options:"
    echo "  --dry-run           Generate the curl command without executing it"
    echo "  -h, --help          Show this help message"
    echo
    echo "Example: $0 tax-001"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
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
            if [ -z "$TAXONOMY_ID" ]; then
                TAXONOMY_ID="$1"
                echo "Setting TAXONOMY_ID to: $1"
            else
                echo -e "${RED}Error: Unexpected argument $1${NC}"
                show_usage
                exit 1
            fi
            shift
            ;;
    esac
done

# Check if taxonomy ID is provided
if [ -z "$TAXONOMY_ID" ]; then
    echo -e "${RED}Error: Taxonomy ID is required${NC}"
    show_usage
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p "$LOGS_DIR"

echo "=== Starting Taxonomy Deletion $(date) ===" | tee "$LOG_FILE"
echo "Taxonomy ID: $TAXONOMY_ID" | tee -a "$LOG_FILE"
if [ "$DRY_RUN" = true ]; then
    echo "DRY RUN MODE - No commands will be executed" | tee -a "$LOG_FILE"
fi

# Create the delete curl command
DELETE_COMMAND="curl -X DELETE \"$BASE_URL/taxonomy/$TAXONOMY_ID\""

if [ "$DRY_RUN" = true ]; then
    CURL_COMMANDS_FILE="$LOGS_DIR/delete_taxonomy_curl_command_$TIMESTAMP.txt"
    echo "# Delete Taxonomy $TAXONOMY_ID" > "$CURL_COMMANDS_FILE"
    echo "$DELETE_COMMAND" >> "$CURL_COMMANDS_FILE"
    echo -e "${GREEN}Curl command has been written to: $CURL_COMMANDS_FILE${NC}"
    cat "$CURL_COMMANDS_FILE"
else
    # Execute command
    echo -e "${YELLOW}Deleting taxonomy with ID: $TAXONOMY_ID${NC}" | tee -a "$LOG_FILE"
    DELETE_RESPONSE=$(eval "$DELETE_COMMAND")
    echo "Delete Response: $DELETE_RESPONSE" | tee -a "$LOG_FILE"
    
    if [[ $DELETE_RESPONSE == *"OK"* ]]; then
        echo -e "${GREEN}Successfully deleted taxonomy with ID: $TAXONOMY_ID${NC}" | tee -a "$LOG_FILE"
    else
        echo -e "${RED}Failed to delete taxonomy with ID: $TAXONOMY_ID${NC}" | tee -a "$LOG_FILE"
        echo "Response: $DELETE_RESPONSE" | tee -a "$LOG_FILE"
    fi
fi

echo -e "${GREEN}Completed taxonomy deletion process${NC}" | tee -a "$LOG_FILE"
echo "Log file: $LOG_FILE"
if [ "$DRY_RUN" = true ]; then
    echo "Output file: $CURL_COMMANDS_FILE"
fi 