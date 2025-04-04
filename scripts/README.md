# Taxonomy Creation Scripts

## create_taxonomy_from_csv.sh

A shell script that creates taxonomies from CSV files. The script can either execute the creation commands or show what would be executed in a dry-run mode.

### CSV Format

The script expects a CSV file with up to 6 levels of hierarchy. Each row represents a path from root to leaf, where each level is the child of the previous level.

Example CSV format:

```csv
Level 1,Level 2,Level 3,Level 4,Level 5,Level 6
Industry,Manufacturing,Electronics,Semiconductors,,
Industry,Manufacturing,Automotive,Cars,,
Industry,Services,Financial,Banking,,
```

### Usage

```bash
./create_taxonomy_from_csv.sh [csv_file] [taxonomy_id] [options]

Options:
  --dry-run [MODE]      Generate output without executing commands
                        MODE can be:
                          commands  - Show curl commands that would be executed (default)
                          rows      - Show taxonomy rows that would be created
  --dimension NAME      Specify the dimension name (default: account)
  -h, --help           Show this help message
```

### Examples

1. Create a taxonomy with default settings:

```bash
$ ./create_taxonomy_from_csv.sh data/Taxonomy1.csv tax-001
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:30:15 PDT 2024 ===
Dimension Name: account
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
```

1. Dry run showing commands that would be executed:

```bash
$ ./create_taxonomy_from_csv.sh data/Taxonomy1.csv tax-001 --dry-run
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:31:00 PDT 2024 ===
Dimension Name: account
DRY RUN MODE (commands) - No commands will be executed
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
Curl commands have been written to: logs/curl_commands_20240320_103100.txt
```

1. Dry run showing rows that would be created:

```bash
$ ./create_taxonomy_from_csv.sh data/Taxonomy1.csv tax-001 --dry-run rows
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:32:00 PDT 2024 ===
Dimension Name: account
DRY RUN MODE (rows) - No commands will be executed
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
Row data has been written to: logs/taxonomy_rows_20240320_103200.json
```

1. Create taxonomy with custom dimension:

```bash
$ ./create_taxonomy_from_csv.sh data/Taxonomy1.csv tax-001 --dimension industry
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:33:00 PDT 2024 ===
Dimension Name: industry
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
```

### Output Files

The script creates several output files in the `logs` directory:

1. `taxonomy_creation_TIMESTAMP.log`: General execution log
2. `curl_commands_TIMESTAMP.txt`: Generated curl commands (in dry-run commands mode)
3. `taxonomy_rows_TIMESTAMP.json`: Generated taxonomy data (in dry-run rows mode)

Example curl commands output:

```bash
# Create Taxonomy
curl -X POST "http://localhost:9000/taxonomy/tax-001" \
    -H "Content-Type: application/json" \
    -d '{"dimensionName":"account","name":"CSV Based Taxonomy","description":"Taxonomy created from CSV file","version":"1.0"}'

# Add Taxonomy Rows
curl -X POST "http://localhost:9000/taxonomy/tax-001/rows" \
    -H "Content-Type: application/json" \
    -d '{"rows":[...],"isReplace":true}'
```

### Requirements

- Bash shell
- Python 3 (for UUID generation)
- curl command-line tool 