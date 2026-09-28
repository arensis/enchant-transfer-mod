#!/usr/bin/env bash
# Regenerates docs/modrinth-description.md from README.md:
#   - relative image paths (docs/images/...) become absolute raw.githubusercontent.com URLs
#   - the "For Developers" section is removed
#   - the <!-- AUTO:... --> markers used by sync-release-docs.py are removed
#
# Usage: ./scripts/build-modrinth-description.sh [output-file]

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE="$ROOT_DIR/README.md"
TARGET="${1:-$ROOT_DIR/docs/modrinth-description.md}"
RAW_BASE="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/"

# Drop everything from "## For Developers" up to and including the next "---"
# separator (plus the blank line after it), so the preceding separator leads
# straight into the next section.
awk '
  /^[[:space:]]*<!-- \/?AUTO:[a-z-]+ -->[[:space:]]*$/ { next }
  /^## For Developers[[:space:]]*$/ { skipping = 1; next }
  skipping && /^---[[:space:]]*$/   { skipping = 0; eat_blank = 1; next }
  eat_blank && /^[[:space:]]*$/     { eat_blank = 0; next }
                                    { eat_blank = 0 }
  !skipping                         { print }
' "$SOURCE" \
  | sed -E "s#([\"'(])(\./)?docs/images/#\1${RAW_BASE}#g" \
  > "$TARGET"

echo "Generated ${TARGET#"$ROOT_DIR"/}"
