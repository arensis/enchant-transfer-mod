#!/usr/bin/env bash
# Parada limpia del servidor Minecraft: envia "stop" a la consola del screen
# y espera a que java termine (guardado ordenado del mundo).
# Se usa como ExecStop de minecraft.service. Corre como el usuario del servicio.
set -u

SCREEN=minecraft
MATCH=fabric-server-launch.jar
TIMEOUT=100

# Si no hay servidor vivo, nada que hacer.
pgrep -f "$MATCH" >/dev/null || exit 0

CR=$(printf '\r')
# Forzar guardado y luego parar de forma ordenada por la consola del screen.
screen -S "$SCREEN" -p 0 -X stuff "save-all flush${CR}" 2>/dev/null || true
sleep 3
screen -S "$SCREEN" -p 0 -X stuff "stop${CR}" 2>/dev/null || true

# Esperar a que el proceso java termine solo.
for _ in $(seq 1 "$TIMEOUT"); do
    pgrep -f "$MATCH" >/dev/null || exit 0
    sleep 1
done

# Si sigue vivo tras el timeout, salimos y systemd aplicara SIGTERM/SIGKILL.
exit 0
