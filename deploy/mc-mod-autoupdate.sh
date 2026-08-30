#!/usr/bin/env bash
# Consulta el ultimo release de un repo de GitHub y, si hay una version del mod
# mas nueva que la desplegada, la descarga y la instala con mc-update-mod.sh.
# Pensado para correr periodicamente (systemd timer). Repo publico: sin token.
set -uo pipefail

REPO="arensis/enchant-transfer-mod"
ASSET_REGEX='enchant-transfer-mod-.*\.jar$'
UPDATER=/usr/local/bin/mc-update-mod.sh
STATEDIR=/var/lib/mc-mod-autoupdate
STATE="$STATEDIR/$(echo "$REPO" | tr '/' '_').tag"
FAILED="$STATE.failed"
TMPDIR=/tmp/mc-mod-autoupdate
TAG=mc-mod-autoupdate

log(){ logger -t "$TAG" -- "$*" 2>/dev/null || true; echo "[$TAG] $*"; }

mkdir -p "$STATEDIR" "$TMPDIR"

API="https://api.github.com/repos/$REPO/releases/latest"
JSON=$(curl -fsSL -H "Accept: application/vnd.github+json" --max-time 20 "$API") \
    || { log "ERROR: no pude consultar la API de GitHub"; exit 1; }

read -r RTAG URL NAME < <(echo "$JSON" | ASSET_REGEX="$ASSET_REGEX" python3 -c '
import sys, os, json, re
d = json.load(sys.stdin)
tag = d.get("tag_name", "")
rx = re.compile(os.environ["ASSET_REGEX"])
url = name = ""
for a in d.get("assets", []):
    if rx.search(a.get("name", "")):
        url = a.get("browser_download_url", ""); name = a.get("name", ""); break
print(tag, url, name)
')

[ -n "${RTAG:-}" ] || { log "ERROR: no pude leer el tag del release"; exit 1; }
[ -n "${URL:-}" ]  || { log "ERROR: el release $RTAG no tiene asset que coincida con $ASSET_REGEX"; exit 1; }

CURRENT=$(cat "$STATE"  2>/dev/null || echo "")
FAILTAG=$(cat "$FAILED" 2>/dev/null || echo "")

# Ya desplegado: nada que hacer (salida silenciosa).
[ "$RTAG" = "$CURRENT" ] && exit 0
# Ya intentado y fallo: no reintentar en bucle hasta que salga uno mas nuevo.
[ "$RTAG" = "$FAILTAG" ] && exit 0

log "nueva version: $RTAG (desplegada: '${CURRENT:-ninguna}'). Descargando $NAME ..."
OUT="$TMPDIR/$NAME"
curl -fsSL --max-time 90 -o "$OUT" "$URL" || { log "ERROR: fallo la descarga de $URL"; exit 1; }

if "$UPDATER" "$OUT"; then
    echo "$RTAG" > "$STATE"; rm -f "$FAILED" "$OUT"
    log "OK: desplegada $RTAG"
else
    echo "$RTAG" > "$FAILED"
    log "ERROR: el despliegue de $RTAG fallo (rollback aplicado por el updater). Marcado para no reintentar en bucle."
    exit 1
fi
