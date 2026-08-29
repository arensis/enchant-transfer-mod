# Enchant Transfer Mod — Diseño y decisiones

> Documento de referencia. Registra decisiones de diseño tomadas, no implementación técnica.
> Última actualización: 2026-05-29

---

## Estado actual del mod (implementado)

### Transfer Table (core)
- Extrae encantamientos de cualquier ítem encantable → carta mágica (**coste 0**)
- Aplica cartas a ítems (**coste 0**, respeta reglas vanilla de compatibilidad)
- Combina dos cartas del mismo encantamiento y mismo nivel → carta de nivel superior (**coste 0**)
- GUI: grid visual de cartas, 100% configurable dentro de los límites vanilla

### Infusion Coil (módulo 1)
- Carta mágica → XP almacenado en tank interno
- XP del tank → botellas de experiencia
- Se auto-adjunta al Transfer Table core adyacente
- Implementa `ExperienceStorage`, visible al agregado de XP de la red

### Infraestructura modular
- Interfaz `TransferTableModule` como contrato de cualquier módulo
- `ModuleConnectionRegistry` — gestiona conexiones core↔módulos por cara
- `ModulePreview` — snapshot de estado para la GUI del core
- `ExperienceStorage` + `SimpleExperienceTank`
- Máximo 6 módulos por core (uno por cara)

### Assets visuales (handoff completo en `src/main/resources/`)
- **Transfer Table:** cubo con embudo escalonado (oro → redstone → carbón → núcleo diamante emisivo). Ver `TRANSFER_TABLE_HANDOFF.md`
- **Infusion Coil:** matraz vertical de cobre/latón/vidrio, siempre vertical independientemente de la cara de acople. Ver `INFUSOR_COIL_HANDOFF.md`
- **GUIs:** Selector (L1, 6 sockets alrededor del hub dorado), Core (L2, slots + nav row), Infusor (slots + tank cian). Ver `GUI_HANDOFF.md`
- Estética general: fondo estrellado oscuro, acentos dorados y cian, borde morado
- Referencia visual interactiva en `reference/`

---

## Material base del mod

### Cristal Horneado ♦
**Crafteo:** Cuarzo de Nether → (horno) → Cristal Horneado

- Forma de rombo, igual que un fragmento de amatista pero transparente/blanquecino
- **Material base de todo lo mágico del mod** — aparece en la cadena de crafteo de cristales de color, gemas arcanas, essence shield y futuros módulos
- Requiere haber ido al Nether → progresión natural antes de acceder a mecánicas avanzadas
- Equivalente al *vis cristal* de Thaumcraft: señaliza al jugador que está en territorio avanzado del mod

### Cristales de Color
**Crafteo:** Cristal Horneado + mineral de color → Cristal de [color]

6 tipos, forma de rombo, cada uno con su color distintivo:

| Color | Mineral | Usos en el mod |
|---|---|---|
| Azul | Lapislázuli | Gemas de Protección |
| Verde | Esmeralda | Gemas de Naturaleza |
| Rojo | Redstone | Gemas de Combate |
| Amarillo | Oro (lingote) | Gemas de Utilidad |
| Morado | Fragmento de amatista | Gemas Arcanas |
| Negro | Obsidiana | Gemas de Maldición |

- El **color identifica la categoría** del encantamiento
- La **runa grabada** sobre el cristal identifica el encantamiento específico dentro de esa categoría
- Son el ingrediente intermedio para craftear gemas arcanas del colgante
- Se reutilizarán en futuras mecánicas del mod como material común

---

## Esencia Mágica
- Nuevo material producido a partir de cartas + XP (módulo destilador, **pendiente de definir**)
- Ingrediente en crafteos avanzados: cristales de color, essence shield, gemas arcanas, Cristal Foco
- No es un recurso de energía en tiempo real, es un material crafteable/consumible

### Cristal Foco
- Curio equipable que el jugador lleva encima para almacenar esencia mágica
- Crafteado con Cristal Horneado
- Los encantamientos mágicos del mod (como Freezing) consumen esencia de este depósito al activarse
- Si se agota, los efectos que requieren esencia no se activan
- Niveles con distintas capacidades de almacenamiento (**pendiente de definir**)

---

## Nuevos encantamientos

### Categorías de encantamientos (por color de cristal/gema)

| Color | Categoría | Encantamientos vanilla incluidos |
|---|---|---|
| Azul | Protección | Protection, Fire Protection, Blast Protection, Projectile Protection, Feather Falling |
| Verde | Naturaleza | Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune, Harvesting* |
| Rojo | Combate | Sharpness, Smite, Bane of Arthropods, Impaling, Fire Aspect, Looting, Thorns, Sweeping Edge, Momentum* |
| Amarillo | Utilidad | Efficiency, Unbreaking, Mending, Swift Sneak, Infinity, Power, Punch |
| Morado | Arcano | Channeling, Riptide, Loyalty, Frost Walker, Freezing* + encantamientos propios del mod |
| Negro | Maldición | Curse of Vanishing, Curse of Binding + maldiciones propias del mod |

> *Encantamientos nuevos del mod

---

### Magnetism *(nuevo — Utilidad/Amarillo)*
- Ítems y orbes de XP cercanos se atraen automáticamente al jugador
- Disponible en herramientas y armadura
- No existe en vanilla

### Harvesting *(nuevo — Naturaleza/Verde)*
- Los cultivos se auto-replanten al ser cosechados
- Disponible en azada
- No existe en vanilla

### Momentum *(nuevo — Combate/Rojo)*
- Cada golpe consecutivo al mismo mob aumenta ligeramente el daño
- **Condición:** se resetea si el jugador recibe daño durante la cadena de golpes
- **Nivel máximo:** 4
- **Coste:** consume esencia mágica del Cristal Foco del jugador
- Mecánica de habilidad: premia juego agresivo y limpio

### Freezing *(nuevo — Arcano/Morado)*
- Paraliza totalmente al mob golpeado durante la duración del efecto
- **Efecto visual en el mob:** textura de escarcha vanilla (frozenTicks)
- **Efecto visual en el arma:** partículas de hielo/copo de nieve
- **Niveles y duración:** I=1s · II=1.5s · III=2s · IV=2.5s · V=3s
- **Cooldown:** 2 segundos tras expirar el efecto antes de poder volver a congelar al mismo mob. Si el mob ya tiene el efecto, el golpe no lo renueva
- **Coste:** consume esencia mágica del Cristal Foco del jugador
- **Incompatible con:** Fire Aspect
- **Mobs inmunes:**
  - *Por fuego:* Blaze, Magma Cube, Strider
  - *Por boss/magia:* Ender Dragon, Wither, Bruja, Evoker, Illusioner, Vex
  - *Otros:* Enderman, Elder Guardian, Ghast

---

## Nuevos bloques

### Speed Pad
- Aumenta la velocidad de movimiento al pisarlo
- Crafteado con esencia mágica

### Attractor
- Atrae ítems y orbes de XP cercanos hacia él
- Útil junto a granjas automáticas
- Crafteado con esencia mágica

---

## Essence Shield
- Ítem equipable (ranura curio / accesorio)
- **Efecto visual:** halo rosáceo alrededor del personaje mientras está activo
- **Protección:** solo bloquea daño de proyectiles y daño no cuerpo a cuerpo (explosiones, fuego, magia...)
- **Mecánica:**
  - Tiene una capacidad de esencia (barra visible en el ítem)
  - Absorbe daño consumiendo esencia del depósito
  - Cuando se agota la esencia el halo desaparece y el ítem queda "vacío"
  - Se recarga insertándolo en un módulo/bloque con esencia disponible
- **Niveles I, II, III:** aumentan la capacidad del depósito, no la protección por golpe
- Cristal horneado en su receta de crafteo

---

## Arcane Pedestal + Colgante Arcano

### Arcane Pedestal
- Bloque que irradia el efecto de la carta insertada en un radio pequeño
- **Sin consumo de esencia** — el coste está en fabricar las gemas del colgante
- Solo afecta a jugadores que lleven el **Colgante Arcano** equipado con la gema correspondiente
- Útil para: Respiration bajo el agua, Fire Protection en el Nether, Feather Falling en zonas de caída

### Colgante Arcano
- Ítem equipable (ranura curio / accesorio)
- **Hasta 3 ranuras de gemas**
- Sin desgaste
- El jugador elige qué 3 efectos quiere recibir de los pedestales cercanos

### Gemas Arcanas
**Crafteo:** Cristal de [color] + carta mágica del encantamiento correspondiente → Gema Arcana

- El **color del cristal** determina la categoría del encantamiento (debe coincidir)
- La **runa grabada** identifica el encantamiento específico dentro de esa categoría
- Cada gema activa su efecto en el radio del pedestal para los jugadores con el colgante equipado
- Ejemplos:
  - Cristal azul + carta Fire Protection → Gema azul con runa de fuego → niega daño de fuego/lava/explosiones en el radio
  - Cristal verde + carta Respiration → Gema verde con runa de agua → permite respirar bajo el agua en el radio
  - Cristal azul + carta Feather Falling → Gema azul con runa de caída → anula daño de caída en el radio

---

## Sistema de Portales
> ⚠️ **DESCARTADO** — demasiado similar a Waystones (mod muy popular que hace exactamente esto). Los cristales de color siguen existiendo como material del mod con otros usos.

---

## Ideas descartadas

| Idea | Motivo |
|---|---|
| Sistema de portales | Demasiado similar a Waystones (mod muy popular) |
| Romper reglas vanilla (encantamientos incompatibles, superar nivel máximo, ítems no compatibles) | Más un cheat que una mecánica satisfactoria |
| Scrying Basin (ver otra ubicación en tiempo real) | Demasiado costoso en servidor |
| Echo Module (duplicar cartas con mucho XP) | Controvertido, rompe balance |
| Curse Siphon, Tome Binder, Auto-Combiner | No suficientemente diferenciadores |
| Efectos de área sin catalizador | Demasiado pasivo, no convence |
| Speed Pad, Attractor | Replicados en docenas de mods (Cyclic, etc.) — reconsiderar |

---

## Pendiente de definir

### Mecánicas
- [ ] Módulo/bloque que produce esencia mágica (destilador) — cómo y dónde
- [ ] Bloque de recarga del Essence Shield y Cristal Foco
- [x] ~~Categoría del cristal Negro (Obsidiana) — qué encantamientos agrupa~~ → Maldiciones (Curse of Vanishing, Curse of Binding y futuras propias)
- [ ] Coste exacto en esencia de Momentum y Freezing (¿por activación? ¿por segundo activo?)
- [ ] Cooldown de Momentum (¿cuánto hay que esperar tras romper la cadena?)
- [ ] Reconsiderar Speed Pad y Attractor (replicados en otros mods)

### Crafteos
- [ ] Receta del Arcane Pedestal
- [ ] Receta del Colgante Arcano
- [ ] Receta del Essence Shield (lleva Cristal Horneado, resto pendiente)
- [ ] Receta del Cristal Foco (lleva Cristal Horneado, resto pendiente)
- [ ] Niveles del Cristal Foco y sus capacidades
