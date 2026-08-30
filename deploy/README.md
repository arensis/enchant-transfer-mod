# Despliegue y auto-actualización del servidor Minecraft

Automatización para desplegar este mod (y actualizarlo solo) en el servidor
Minecraft que corre en la Raspberry Pi de casa, con parada limpia del mundo y
aviso previo a los jugadores.

Los scripts de esta carpeta **no contienen credenciales** y están pensados para
instalarse en la Raspberry en `/usr/local/bin` y `/etc/systemd/system`.

---

## Cómo funciona (visión general)

```
  git tag vX.Y.Z / "Manual Release"
            │
            ▼
  GitHub Actions (release.yml)  ──►  crea un Release y adjunta el .jar como asset
            │
            ▼   (la Pi consulta, no hay webhook: está tras NAT)
  Raspberry Pi · systemd timer (diario 05:00)
            │
            ▼
  mc-mod-autoupdate.sh
     ├─ consulta releases/latest de arensis/enchant-transfer-mod
     ├─ ¿tag nuevo respecto al desplegado? ── no ──► fin (no-op)
     └─ sí ─► descarga el .jar ─► mc-update-mod.sh
                                      ├─ ¿jugadores conectados? ─► avisa (say) y cuenta atrás 5→1→0
                                      ├─ systemctl stop  (ExecStop = mc-stop.sh → guardado limpio)
                                      ├─ respalda y retira el jar antiguo, instala el nuevo
                                      ├─ systemctl start + verifica arranque y carga del mod
                                      └─ si falla ─► ROLLBACK a la versión anterior
```

Como la Pi está detrás de NAT doméstico, se usa **poll (pull)** desde la Pi en
lugar de un webhook/Action que empuje: así no hay que exponer ningún puerto ni
guardar llaves SSH en GitHub. El repo es **público**, por lo que no hace falta
token.

---

## Componentes

| Archivo | Ubicación en la Pi | Qué hace |
|---|---|---|
| `mc-stop.sh` | `/usr/local/bin/` | Parada limpia: manda `save-all flush` + `stop` a la consola del `screen` y espera. Se usa como `ExecStop` de `minecraft.service`. |
| `mc-update-mod.sh` | `/usr/local/bin/` | Actualiza **un** mod: (aviso) → parar → respaldar/retirar antiguo → instalar → arrancar → verificar → rollback si falla. Reutilizable a mano. |
| `mc-mod-autoupdate.sh` | `/usr/local/bin/` | Comprueba el último Release en GitHub y, si hay versión nueva, la descarga y llama a `mc-update-mod.sh`. |
| `mc-mod-autoupdate.service` | `/etc/systemd/system/` | Servicio oneshot que ejecuta el auto-updater. |
| `mc-mod-autoupdate.timer` | `/etc/systemd/system/` | Lo dispara **una vez al día a las 05:00**. |

Además, en `minecraft.service` se añadió:

```ini
ExecStop=/usr/local/bin/mc-stop.sh
TimeoutStopSec=120
```

### Estado y datos en la Pi

- Último tag desplegado: `/var/lib/mc-mod-autoupdate/arensis_enchant-transfer-mod.tag`
- Marca de release fallido (para no reintentar en bucle): mismo path con sufijo `.failed`
- Backups de jars anteriores: `<server>/mods_backup/` (nombre original + fecha)
- Servidor: `/home/alfonso/Documents/minecraft_servier` · mods en `mods/`

---

## Flujo de trabajo habitual (publicar una versión)

1. Hacer los cambios y subirlos.
2. Lanzar una release, de cualquiera de estas formas:
   - GitHub → **Actions → Manual Release** (elige `patch`/`minor`/`major`): sube la
     versión en `gradle.properties`, hace commit y crea el tag.
   - o crear el tag a mano: `git tag vX.Y.Z && git push origin vX.Y.Z`.
3. `release.yml` compila y publica el Release con el `.jar` adjunto.
4. A las **05:00** siguientes, la Pi detecta la versión nueva, avisa si hay
   alguien jugando, actualiza el servidor y verifica que arranca. Sin intervención.

> Despliegue inmediato (sin esperar a las 05:00): en la Pi
> `sudo systemctl start mc-mod-autoupdate.service`.

---

## Uso manual del actualizador

Desplegar un `.jar` concreto que ya esté en la Pi:

```bash
mc-update-mod.sh /ruta/al/enchant-transfer-mod-X.Y.Z.jar
# opcional: prefijo para identificar versiones antiguas (por defecto se deriva del nombre)
mc-update-mod.sh /ruta/al/mi-mod-1.2.3.jar mi-mod
```

Variable de entorno:

- `WARN_SECONDS` — segundos de aviso previo si hay jugadores (por defecto `300`;
  `0` = sin espera). Ej.: `WARN_SECONDS=60 mc-update-mod.sh ...`

El aviso **solo** ocurre si hay jugadores conectados; con el servidor vacío
reinicia al momento.

---

## Configuración

- **Cambiar la hora/frecuencia**: editar `OnCalendar` en `mc-mod-autoupdate.timer`
  (formato systemd; ej. `*-*-* 05:00:00` diario, `Mon *-*-* 05:00:00` solo lunes),
  luego `sudo systemctl daemon-reload && sudo systemctl restart mc-mod-autoupdate.timer`.
- **Añadir otro mod**: duplicar `mc-mod-autoupdate.sh` (y sus units) cambiando
  `REPO` y `ASSET_REGEX`. `mc-stop.sh` y `mc-update-mod.sh` son genéricos y se
  reutilizan tal cual.

---

## Instalación desde cero (referencia)

Desde esta carpeta, en la Pi:

```bash
sudo install -m 755 mc-stop.sh mc-update-mod.sh mc-mod-autoupdate.sh /usr/local/bin/
sudo install -m 644 mc-mod-autoupdate.service mc-mod-autoupdate.timer /etc/systemd/system/

# ExecStop en minecraft.service (una sola vez)
sudo sed -i '/^ExecStart=/a ExecStop=/usr/local/bin/mc-stop.sh\nTimeoutStopSec=120' \
  /etc/systemd/system/minecraft.service

# Estado inicial = versión ya instalada (evita un redepliegue innecesario)
sudo mkdir -p /var/lib/mc-mod-autoupdate
echo "vX.Y.Z" | sudo tee /var/lib/mc-mod-autoupdate/arensis_enchant-transfer-mod.tag

sudo systemctl daemon-reload
sudo systemctl enable --now mc-mod-autoupdate.timer
```

Requisitos en la Pi: `screen`, `curl`, `unzip`, `python3` (con `mcstatus` en
`/home/alfonso/mc_venv` para el aviso a jugadores) y `sudo` sin contraseña para
el usuario que ejecuta a mano.

---

## Diagnóstico

```bash
# Próxima ejecución programada
systemctl list-timers mc-mod-autoupdate.timer

# Logs del auto-updater
journalctl -t mc-mod-autoupdate
journalctl -u mc-mod-autoupdate.service -n 30

# Forzar una comprobación ahora
sudo systemctl start mc-mod-autoupdate.service

# Re-desplegar la última versión a la fuerza (dejar el estado en una anterior)
echo "v0.0.0" | sudo tee /var/lib/mc-mod-autoupdate/arensis_enchant-transfer-mod.tag
sudo systemctl start mc-mod-autoupdate.service

# Log del servidor
tail -f /home/alfonso/Documents/minecraft_servier/logs/latest.log
```

Si un release rompe el arranque, `mc-update-mod.sh` restaura la versión anterior
automáticamente y el auto-updater marca ese tag como `.failed` para no reintentarlo
hasta que salga uno más nuevo. Para reintentar tras arreglarlo: borrar el fichero
`.failed`.
