# Taxonomy Creation Scripts

## create_taxonomy_from_csv.sh

A shell script that creates taxonomies from CSV files. The script can either execute the creation commands or show what would be executed in a dry-run mode.

### Prerequisites

1. `jq` command-line JSON processor must be installed
2. The script must be run from the `scripts` directory

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
cd scripts
./create_taxonomy_from_csv.sh [csv_file] [taxonomy_id] [options]

Options:
  --dry-run [MODE]      Generate output without executing commands
                        MODE can be:
                          commands  - Show curl commands that would be executed (default)
                          rows      - Show taxonomy rows that would be created
  --batch-size SIZE     Specify the batch size for adding rows (default: 20)
  --debug              Enable detailed debug output
  -h, --help           Show this help message
```

### Examples

1. Create a taxonomy with default settings:

```bash
$ cd scripts
$ ./create_taxonomy_from_csv.sh data/1Taxonomy.csv tax-001
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:30:15 PDT 2024 ===
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
```

2. Dry run showing commands that would be executed:

```bash
$ cd scripts
$ ./create_taxonomy_from_csv.sh data/1Taxonomy.csv tax-001 --dry-run
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:31:00 PDT 2024 ===
DRY RUN MODE (commands) - No commands will be executed
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
Curl commands have been written to: logs/curl_commands_20240320_103100.txt
```

3. Dry run showing rows that would be created:

```bash
$ cd scripts
$ ./create_taxonomy_from_csv.sh data/1Taxonomy.csv tax-001 --dry-run rows
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:32:00 PDT 2024 ===
DRY RUN MODE (rows) - No commands will be executed
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
Row data has been written to: logs/taxonomy_rows_20240320_103200.json
```

4. Create taxonomy with custom batch size:

```bash
$ cd scripts
$ ./create_taxonomy_from_csv.sh data/1Taxonomy.csv tax-001 --batch-size 50
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:33:00 PDT 2024 ===
Processing rows...
Processing CSV line 3 - Created 12 taxonomy rows
Completed processing: 12 total taxonomy rows created from 3 CSV lines
```

5. Create taxonomy with debug output:

```bash
$ cd scripts
$ ./create_taxonomy_from_csv.sh data/1Taxonomy.csv tax-001 --debug
=== Starting Taxonomy Creation from CSV Wed Mar 20 10:34:00 PDT 2024 ===
Debug mode enabled
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
    -d '{"name":"CSV Based Taxonomy","description":"Taxonomy created from CSV file","version":"1.0"}'

# Add Taxonomy Rows in Batches
# Batch 1/1
curl -X POST "http://localhost:9000/taxonomy/tax-001/taxrows" \
    -H "Content-Type: application/json" \
    -d '{"rows":[{"rowId":"...","value":"Industry","description":"This is the Industry","aliases":[],"keywords":[],"dimensionSrcHints":{},"parent":null},...],"isReplace":true}'
```

### Notes

1. The script requires `jq` to be installed for JSON processing
2. The script must be run from the `scripts` directory
3. The `data` directory containing CSV files is located in the `scripts` directory
4. The script creates a `logs` directory if it doesn't exist
5. Each run creates timestamped log files to prevent overwriting previous runs
6. The default batch size is 20 rows, which can be adjusted with the `--batch-size` option 