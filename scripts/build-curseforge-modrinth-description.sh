#!/usr/bin/env bash
# Regenerates docs/curseforge-modrinth-description.md (the store description
# pasted into both CurseForge and Modrinth) from README.md:
#   - relative image paths (docs/images/...) become absolute raw.githubusercontent.com URLs
#   - Markdown images (the badges) become <img>/<a> HTML: CurseForge doesn't parse
#     Markdown inside the README's centred <div>; GitHub and Modrinth do
#   - the "For Developers" section is removed
#   - the collapsible "Previous versions" changelog (<details>) is unwrapped, since
#     CurseForge strips <details>/<summary>: entries from the current minor series
#     (e.g. 3.1.0 under "Changelog — 3.1.1") stay, older ones become a link to the
#     website's changelog
#   - the <!-- AUTO:... --> markers used by sync-release-docs.py are removed; the
#     badges' ones become a <p>, so CurseForge sets them apart from the logo
#   - table alignment rows (|:-:|) become plain ones (|---|): CurseForge doesn't
#     render tables with alignment colons
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
  /^[[:space:]]*<!-- AUTO:badges -->[[:space:]]*$/  { print "<p>"; next }
  /^[[:space:]]*<!-- \/AUTO:badges -->[[:space:]]*$/ { print "</p>"; next }
  /^[[:space:]]*<!-- \/?AUTO:[a-z-]+ -->[[:space:]]*$/ { next }
  /^## For Developers[[:space:]]*$/ { skipping = 1; next }
  /^## Changelog — [0-9]+\.[0-9]+\./ {
    series = $0; sub(/^## Changelog — /, "", series); sub(/\.[0-9]+[^0-9.]*$/, "", series)
  }
  /^<details>[[:space:]]*$/          { in_details = 1; keep = 0; next }
  in_details && /^<\/details>[[:space:]]*$/ {
    in_details = 0
    if (last != "") print ""
    print "**Older versions:** see the [full changelog](https://arensis.github.io/enchant-transfer-mod/#changelog)."
    last = "link"
    next
  }
  in_details && /^### [0-9]+\.[0-9]+\./ {
    v = $0; sub(/^### /, "", v); sub(/\.[0-9]+[^0-9.]*$/, "", v)
    keep = (v == series)
  }
  in_details && !keep               { next }
  skipping && /^---[[:space:]]*$/   { skipping = 0; eat_blank = 1; next }
  eat_blank && /^[[:space:]]*$/     { eat_blank = 0; next }
                                    { eat_blank = 0 }
  !skipping                         { print; last = $0 }
' "$SOURCE" \
  | sed -E "s#([\"'(])(\./)?docs/images/#\1${RAW_BASE}#g" \
  | sed -E 's#\[!\[([^]]*)\]\(([^)]*)\)\]\(([^)]*)\)#<a href="\3"><img src="\2" alt="\1"></a>#g' \
  | sed -E 's#!\[([^]]*)\]\(([^)]*)\)#<img src="\2" alt="\1">#g' \
  | sed -E -e ':a' -e 's#(https://img\.shields\.io/[^" ]*)≥#\1%E2%89%A5#' -e 'ta' \
  | sed -E '/^\|( *:?-+:? *\|)+ *$/ { s/:-+:?/---/g; s/-+:/---/g; }' \
  > "$TARGET"

echo "Generated ${TARGET#"$ROOT_DIR"/}"
