#!/usr/bin/env bash
# Actualiza un mod del servidor Minecraft de forma segura:
#   (avisar a jugadores si los hay) -> parar -> respaldar y retirar version(es)
#   antigua(s) -> instalar el jar nuevo -> arrancar -> verificar. Si el arranque
#   falla, hace ROLLBACK automatico a la version anterior.
#
# Uso: mc-update-mod.sh <ruta-jar-nuevo> [prefijo-mod]
# Env:
#   WARN_SECONDS  segundos de aviso previo si hay jugadores (por defecto 300; 0 = sin espera)
set -uo pipefail

MC_DIR=/home/alfonso/Documents/minecraft_servier
MODS="$MC_DIR/mods"
BACKUP="$MC_DIR/mods_backup"
LOG="$MC_DIR/logs/latest.log"
SERVICE=minecraft.service
MATCH=fabric-server-launch.jar
OWNER=alfonso:alfonso
SCREEN_USER=alfonso
MCPY=/home/alfonso/mc_venv/bin/python3
BOOT_TIMEOUT=180
WARN_SECONDS="${WARN_SECONDS:-300}"

if [ "$(id -u)" -eq 0 ]; then SUDO=""; else SUDO="sudo -n"; fi
log(){  echo "[mc-update-mod] $*"; }
fail(){ echo "[mc-update-mod][ERROR] $*" >&2; exit 1; }

# --- helpers de consola / jugadores ---
run_as_mc(){ if [ "$(id -u)" -eq 0 ]; then sudo -u "$SCREEN_USER" "$@"; else "$@"; fi; }
mc_say(){ run_as_mc screen -S minecraft -p 0 -X stuff "say $1$(printf '\r')" 2>/dev/null || true; }
players_online(){ "$MCPY" -c 'from mcstatus import JavaServer; print(JavaServer.lookup("127.0.0.1:25565").status().players.online)' 2>/dev/null || echo 0; }

warn_players(){
    local n; n=$(players_online); n=${n:-0}
    if [ "$n" -le 0 ]; then log "sin jugadores conectados; reinicio inmediato."; return 0; fi
    if [ "$WARN_SECONDS" -le 0 ]; then log "$n jugador(es) conectado(s); avisos desactivados."; return 0; fi
    log "$n jugador(es) conectado(s); avisando y esperando ${WARN_SECONDS}s..."
    local rem=$WARN_SECONDS
    mc_say "[Auto-update] Nueva version del mod disponible. El servidor se reiniciara en $((rem/60)) minutos."
    if [ "$rem" -gt 60 ]; then sleep $((rem-60)); rem=60; fi
    mc_say "[Auto-update] Reinicio en 1 minuto. Id a un lugar seguro."
    if [ "$rem" -gt 10 ]; then sleep $((rem-10)); rem=10; fi
    mc_say "[Auto-update] Reinicio en 10 segundos..."
    sleep "$rem"
}

# --- validacion del jar ---
NEW_JAR="${1:-}"
[ -n "$NEW_JAR" ] || fail "falta la ruta del jar. Uso: $0 <jar> [prefijo]"
[ -r "$NEW_JAR" ] || fail "no puedo leer el jar: $NEW_JAR"
BASENAME=$(basename "$NEW_JAR")
unzip -l "$NEW_JAR" >/dev/null 2>&1 || fail "$BASENAME no es un zip/jar valido"
unzip -p "$NEW_JAR" fabric.mod.json >/dev/null 2>&1 || fail "$BASENAME no contiene fabric.mod.json"
read -r MODID MODVER < <(unzip -p "$NEW_JAR" fabric.mod.json \
    | python3 -c 'import sys,json; d=json.load(sys.stdin); print(d.get("id",""), d.get("version",""))')
[ -n "$MODID" ] || fail "no pude leer el modid de fabric.mod.json"
log "mod: id=$MODID version=$MODVER  archivo=$BASENAME"

PREFIX="${2:-}"
[ -n "$PREFIX" ] || PREFIX=$(echo "$BASENAME" | sed -E 's/-[0-9][^-]*\.jar$//; s/\.jar$//')
log "versiones antiguas a retirar: '$PREFIX-*.jar'"

mkdir -p "$BACKUP"
OLD_FIRSTLINE=$(head -1 "$LOG" 2>/dev/null || true)

stop_server(){
    $SUDO systemctl stop "$SERVICE" || true
    for _ in $(seq 1 130); do pgrep -f "$MATCH" >/dev/null && sleep 1 || break; done
    pgrep -f "$MATCH" >/dev/null && return 1 || return 0
}
start_server(){
    $SUDO systemctl reset-failed "$SERVICE" 2>/dev/null || true
    $SUDO systemctl start "$SERVICE"
}
wait_boot(){  # 0=Done, 2=error, 1=timeout
    for _ in $(seq 1 "$BOOT_TIMEOUT"); do
        local FL; FL=$(head -1 "$LOG" 2>/dev/null || true)
        if [ "$FL" != "$OLD_FIRSTLINE" ] && echo "$FL" | grep -q "Loading Minecraft"; then
            grep -q 'Done (' "$LOG" 2>/dev/null && return 0
            grep -qE 'A mod crashed|Crash report|Failed to start the minecraft server|Incompatible mod set|could not be found|java.lang.NoSuchMethod|Mixin apply.*failed' "$LOG" 2>/dev/null && return 2
        fi
        sleep 1
    done
    return 1
}

# --- AVISO + PARAR ---
warn_players
log "parando servidor..."
stop_server || fail "el servidor no se detuvo a tiempo"
log "servidor parado."

# --- RETIRAR ANTIGUOS + INSTALAR NUEVO ---
shopt -s nullglob
declare -a MOVED=()
for f in "$MODS/$PREFIX-"*.jar; do
    dest="$BACKUP/$(basename "$f").$(date +%Y%m%d_%H%M%S)"
    mv "$f" "$dest"; MOVED+=("$dest")
    log "respaldado y retirado: $(basename "$f")"
done
cp "$NEW_JAR" "$MODS/$BASENAME"
chown "$OWNER" "$MODS/$BASENAME" 2>/dev/null || true
log "instalado: $BASENAME"

# --- ARRANCAR + VERIFICAR ---
log "arrancando servidor..."
start_server
log "esperando arranque (max ${BOOT_TIMEOUT}s)..."
wait_boot; RC=$?

if [ "$RC" -eq 0 ] && grep -qiE "[- ]${MODID}[ ]${MODVER}\b|[- ]${MODID}\b" "$LOG"; then
    log "OK: servidor arrancado y mod '$MODID $MODVER' cargado."
    log "DONE."
    exit 0
fi

# --- FALLO -> ROLLBACK ---
log "ARRANQUE FALLIDO (rc=$RC). Iniciando ROLLBACK..."
stop_server || true
rm -f "$MODS/$BASENAME"
if [ "${#MOVED[@]}" -gt 0 ]; then
    last="${MOVED[-1]}"; orig=$(basename "$last"); orig=${orig%.*}
    cp "$last" "$MODS/$orig"; chown "$OWNER" "$MODS/$orig" 2>/dev/null || true
    log "restaurado: $orig"
else
    log "AVISO: no habia version anterior que restaurar."
fi
OLD_FIRSTLINE=$(head -1 "$LOG" 2>/dev/null || true)
start_server
wait_boot && log "rollback OK: servidor operativo con la version anterior." \
          || log "AVISO: el servidor no confirmo arranque tras el rollback; revisar manualmente."
fail "el despliegue de $BASENAME fallo; se restauro la version anterior."
