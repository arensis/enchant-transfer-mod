#!/usr/bin/env bash
# Regenerates docs/curseforge-modrinth-description.md (the store description
# pasted into both CurseForge and Modrinth) from README.md:
#   - relative image paths (docs/images/...) become absolute raw.githubusercontent.com URLs
#   - Markdown images (the badges) become <img>/<a> HTML: CurseForge doesn't parse
#     Markdown inside the README's centred <div>; GitHub and Modrinth do
#   - in shields.io badge paths, "_" becomes "%20" (also a space for shields) and
#     "≥" is dropped: CurseForge turns the underscores into italics and breaks
#     the URL, and doesn't take an encoded ≥ either
#   - the "For Developers" section is removed
#   - the collapsible "Previous versions" changelog (<details>) is unwrapped, since
#     CurseForge strips <details>/<summary>: entries from the current minor series
#     (e.g. 3.1.0 under "Changelog — 3.1.1") stay, older ones become a link to the
#     website's changelog (the README heads the section "Changelog — 3.1.x",
#     each version under its own "### 3.1.1")
#   - the <!-- AUTO:... --> markers used by sync-release-docs.py are removed; the
#     badges' ones become a <p>, so CurseForge sets them apart from the logo
#   - table alignment rows (|:-:|) become plain ones (|---|): CurseForge doesn't
#     render tables with alignment colons
#   - each run of image lines becomes a one-line <p> (bold as <strong>):
#     CurseForge's editor turns other <img> into ![alt](url), losing the width
#   - text lines inside the centred header <div> become HTML (<strong>, <a>):
#     CurseForge doesn't parse Markdown inside an HTML block
#
# CurseForge also strips align and style attributes, so nothing can be
# centred there; Modrinth keeps the README's <div align="center">.
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
    version = $0; sub(/^## Changelog — /, "", version); sub(/[[:space:]]*$/, "", version)
    series = version; sub(/\.([0-9]+|x)$/, "", series)
    # Headed by the series it shows, each version under its own heading;
    # the README already writes it that way ("3.1.x"), older ones get it here.
    if (version ~ /\.x$/) { print; last = $0; next }
    print "## Changelog — " series ".x"; print ""; print "### " version
    last = "### " version
    next
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
  | sed -E -e ':a' -e 's#(https://img\.shields\.io/badge/[^" ?]*)≥#\1#' -e 'ta' \
  | sed -E -e ':b' -e 's#(https://img\.shields\.io/badge/[^" ?]*)_#\1%20#' -e 'tb' \
  | sed -E '/^\|( *:?-+:? *\|)+ *$/ { s/:-+:?/---/g; s/-+:/---/g; }' \
  | awk '
      # Each run of image lines becomes a one-line <p>: CurseForge'\''s editor
      # rewrites <img> in a plain Markdown paragraph (or a multi-line <p>) as
      # ![alt](url), losing the width.  Runs already inside a "<p>" … "</p>"
      # block (the badges, the GUI shots) are joined the same way.  Recipe
      # result lines ("→ <img> **Name** × 1") keep their bold as <strong>,
      # as HTML blocks are not parsed as Markdown.
      function flush() { if (n) { print "<p>" buf "</p>"; n = 0; buf = "" } }
      function bold(s) {
        while (match(s, /\*\*[^*]+\*\*/))
          s = substr(s, 1, RSTART - 1) "<strong>" substr(s, RSTART + 2, RLENGTH - 4) "</strong>" substr(s, RSTART + RLENGTH)
        return s
      }
      # [text](url) → <a href="url">text</a>
      function links(s,   t, i) {
        while (match(s, /\[[^]]*\]\([^)]*\)/)) {
          t = substr(s, RSTART, RLENGTH)
          i = index(t, "](")
          s = substr(s, 1, RSTART - 1) "<a href=\"" substr(t, i + 2, length(t) - i - 2) "\">" substr(t, 2, i - 2) "</a>" substr(s, RSTART + RLENGTH)
        }
        return s
      }
      # The centred header <div> is an HTML block: CurseForge shows its
      # Markdown text as is, so its text lines become HTML paragraphs.
      /^<div align="center">/ { flush(); in_div = 1; print; next }
      /^<\/div>/              { flush(); in_div = 0; print; next }
      in_div && /^[^<[:space:]]/ { flush(); print "<p>" links(bold($0)) "</p>"; next }
      /^<p>[[:space:]]*$/   { flush(); in_p = 1; next }
      /^<\/p>[[:space:]]*$/ { if (in_p) { in_p = 0; flush(); next } }
      /^(→ )?<img / || (in_p && /^<a /) {
        line = bold($0)
        buf = n ? buf " " line : line
        n++
        next
      }
      in_p { flush(); print "<p>"; in_p = 0 }
      { flush(); print }
      END { flush() }
    ' \
  > "$TARGET"

echo "Generated ${TARGET#"$ROOT_DIR"/}"
