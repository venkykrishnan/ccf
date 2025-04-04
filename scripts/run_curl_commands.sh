#!/bin/bash

# Configuration
DEFAULT_COMMANDS_FILE="scripts/taxBasicCurlCmds.txt"
DELAY_SECONDS=1
LOG_FILE="logs/curl_commands.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to show usage
show_usage() {
    echo "Usage: $0 [commands_file]"
    echo
    echo "If commands_file is not provided, defaults to: $DEFAULT_COMMANDS_FILE"
    echo
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo
    echo "Environment variables:"
    echo "  DELAY_SECONDS  Delay between commands (default: 1)"
    echo "  LOG_FILE      Log file path (default: curl_commands.log)"
}

# Handle help flag
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
    show_usage
    exit 0
fi

# Determine which commands file to use
COMMANDS_FILE="${1:-$DEFAULT_COMMANDS_FILE}"

# Check if commands file exists
if [ ! -f "$COMMANDS_FILE" ]; then
    echo -e "${RED}Error: Commands file '$COMMANDS_FILE' not found${NC}"
    echo
    show_usage
    exit 1
fi

echo -e "${GREEN}Using commands file: $COMMANDS_FILE${NC}"

# Initialize log file
echo "=== Curl Commands Execution Log $(date) ===" > "$LOG_FILE"
echo "Commands file: $COMMANDS_FILE" >> "$LOG_FILE"

# Counter for commands
command_count=0
success_count=0
failed_count=0

# Function to execute command and handle errors
execute_command() {
    local cmd="$1"
    local command_num="$2"
    
    echo -e "${YELLOW}Executing command $command_num:${NC}"
    echo "$cmd"
    echo
    
    # Log command
    echo "Command $command_num: $cmd" >> "$LOG_FILE"
    
    # Execute command and capture output and exit status
    output=$(eval "$cmd" 2>&1)
    status=$?
    
    # Log output
    echo "Output:" >> "$LOG_FILE"
    echo "$output" >> "$LOG_FILE"
    echo "Exit status: $status" >> "$LOG_FILE"
    echo "----------------------------------------" >> "$LOG_FILE"
    
    # Display output
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}Success:${NC}"
        ((success_count++))
    else
        echo -e "${RED}Failed (exit code $status):${NC}"
        ((failed_count++))
    fi
    echo "$output"
    echo -e "${YELLOW}----------------------------------------${NC}"
}

# Read and execute commands
while IFS= read -r cmd; do
    # Skip empty lines and comments
    if [[ -z "${cmd// }" ]] || [[ "$cmd" =~ ^#.* ]]; then
        continue
    fi
    
    ((command_count++))
    execute_command "$cmd" "$command_count"
    
    # Add delay between commands
    if [ $DELAY_SECONDS -gt 0 ]; then
        sleep $DELAY_SECONDS
    fi
done < "$COMMANDS_FILE"

# Print summary
echo
echo -e "${GREEN}Execution Summary:${NC}"
echo "Commands file: $COMMANDS_FILE"
echo "Total commands: $command_count"
echo -e "Successful: ${GREEN}$success_count${NC}"
echo -e "Failed: ${RED}$failed_count${NC}"
echo "Log file: $LOG_FILE"
