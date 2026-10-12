#!/usr/bin/env bash
# Regenerates docs/curseforge-modrinth-description.md (the store description
# pasted into both CurseForge and Modrinth) from README.md:
#   - relative image paths (docs/images/...) become absolute raw.githubusercontent.com URLs
#   - Markdown images (the badges) become <img>/<a> HTML: CurseForge doesn't parse
#     Markdown inside the README's centred <div>; GitHub and Modrinth do
#   - the "For Developers" section is removed
#   - the collapsible "Previous versions" changelog (<details>) becomes a link to
#     the website's changelog: CurseForge strips <details>/<summary>
#   - the <!-- AUTO:... --> markers used by sync-release-docs.py are removed
#
# Usage: ./scripts/build-curseforge-modrinth-description.sh [output-file]

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE="$ROOT_DIR/README.md"
TARGET="${1:-$ROOT_DIR/docs/curseforge-modrinth-description.md}"
RAW_BASE="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/"

# Drop everything from "## For Developers" up to and including the next "---"
# separator (plus the blank line after it), so the preceding separator leads
# straight into the next section.
awk '
  /^[[:space:]]*<!-- \/?AUTO:[a-z-]+ -->[[:space:]]*$/ { next }
  /^## For Developers[[:space:]]*$/ { skipping = 1; next }
  /^<details>[[:space:]]*$/          { in_details = 1; next }
  in_details && /^<\/details>[[:space:]]*$/ {
    in_details = 0
    print "**Previous versions:** see the [full changelog](https://arensis.github.io/enchant-transfer-mod/#changelog)."
    next
  }
  in_details                        { next }
  skipping && /^---[[:space:]]*$/   { skipping = 0; eat_blank = 1; next }
  eat_blank && /^[[:space:]]*$/     { eat_blank = 0; next }
                                    { eat_blank = 0 }
  !skipping                         { print }
' "$SOURCE" \
  | sed -E "s#([\"'(])(\./)?docs/images/#\1${RAW_BASE}#g" \
  | sed -E 's#\[!\[([^]]*)\]\(([^)]*)\)\]\(([^)]*)\)#<a href="\3"><img src="\2" alt="\1"></a>#g' \
  | sed -E 's#!\[([^]]*)\]\(([^)]*)\)#<img src="\2" alt="\1">#g' \
  | sed -E -e ':a' -e 's#(https://img\.shields\.io/[^" ]*)≥#\1%E2%89%A5#' -e 'ta' \
  > "$TARGET"

echo "Generated ${TARGET#"$ROOT_DIR"/}"
