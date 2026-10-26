# Enchant Transfer Mod — Diseño y decisiones

> Documento de referencia. Registra decisiones de diseño tomadas, no implementación técnica.
> Tres partes: **✅ Implementado** (está en el mod), **📝 Diseñado, sin implementar** (decidido pero sin código) y **❌ Descartado**.
> Última actualización: 2026-10-18

---

# ✅ Implementado

## Bloques y sistemas

### Transfer Table (core)
- Extrae encantamientos de cualquier ítem encantable → carta mágica (**coste 0**)
- Aplica cartas a ítems (**coste 0**, respeta reglas vanilla de compatibilidad)
- Combina dos cartas del mismo encantamiento y mismo nivel → carta de nivel superior (**coste 0**)
- GUI: grid visual de cartas, 100% configurable dentro de los límites vanilla
- **Shift + clic** en una carta del inventario: va al objeto de la mesa si la admite y no choca con sus encantamientos (una carta); si no, al par de combinación, repartiendo el stack a partes iguales entre las dos ranuras (lo impar se queda en el inventario). Con objeto en la mesa que la rechaza, solo pasa a combinación si forma pareja que pueda subir de nivel

### Zinc Smelter
- Horno independiente para la cadena del latón (calcita → óxido de zinc → lámina de zinc → lingote de latón); combustible solo lava o varas de blaze
- Se rompe con pico de hierro o superior y suelta el bloque (loot table)

### Infusion Coil (módulo 1)
- Carta mágica → XP almacenado en tank interno
- XP del tank → botellas de experiencia
- Se auto-adjunta al Transfer Table core adyacente
- Implementa `ExperienceStorage`, visible al agregado de XP de la red

### Infraestructura modular
- Interfaz `TransferTableModule` como contrato de cualquier módulo
- `ModuleNetwork` — módulos en las caras del core, leídos del mundo al consultarlos (sin registro que sincronizar); `storages(...)` encuentra recursos compartidos (`ExperienceStorage`, en el futuro esencia…) y la vista agregada de XP los trata como un solo depósito
- `ModulePreview` — snapshot de estado para la GUI del core
- `ExperienceStorage` + `SimpleExperienceTank`
- Máximo 6 módulos por core (uno por cara)

### Assets visuales (handoff completo en `src/main/resources/`)
- **Transfer Table:** cubo con embudo escalonado (oro → redstone → carbón → núcleo diamante emisivo). Ver `TRANSFER_TABLE_HANDOFF.md`
- **Infusion Coil:** matraz vertical de cobre/latón/vidrio, siempre vertical independientemente de la cara de acople. Ver `INFUSOR_COIL_HANDOFF.md`
- **GUIs:** Selector (L1, 6 sockets alrededor del hub dorado), Core (L2, slots + nav row), Infusor (slots + tank cian). Ver `GUI_HANDOFF.md`
- Estética general: fondo estrellado oscuro, acentos dorados y cian, borde morado
- Referencia visual interactiva en `reference/`

### Idiomas
- Inglés y español (revisados); portugués de Brasil, ruso, chino simplificado, francés, alemán y japonés pendientes de revisión nativa

---

## Barajas de cartas

Estuche de latón (10×14×4 px) que guarda cartas de un tipo. En un atril de barajas al alcance de una mesa de encantamientos (mismas posiciones y hueco de aire que las librerías normales, pero solo a la altura de la mesa: nada puede ir encima del atril ni el atril debajo de un bloque, así que ocupa toda su columna), modifica lo que ofrece la mesa.

| Baraja | Guarda | Efecto |
|---|---|---|
| Protección / Naturaleza / Combate / Utilidad / Arcano | Cartas de su color | Sesgo hacia su categoría y, más aún, hacia los encantamientos guardados |
| Comodín de Rareza | Cartas de maldición | Favorece encantamientos de peso bajo |

Cada color tiene un tema: **Combate**, todo lo que hace daño o empuja (también arco y ballesta); **Arcano**, lo mágico —fuego, hielo, rayo, viento, almas y experiencia—, salga o no de la mesa (Mending, Soul Speed, Frost Walker y Wind Burst son de tesoro: la mesa nunca los da, pero sus cartas suman fuerza a la baraja Arcana); **Utilidad**, velocidad y durabilidad (Efficiency, Unbreaking, Quick Charge, Swift Sneak, Magnetism).

- **Receta**: 2 latón + 2 cuero + 1 carta del tipo (se consume). Rareza: carta negra.
- **Puntos**: carta de nivel N = 2^(N-1) puntos (misma equivalencia que combinar). Máximo 64 por baraja.
- **Resonancia** `R = min(1, √(puntos de categoría / 64))`; comodín `Rr = min(1, √(puntos / 64))`. Crece rápido al principio (unas pocas cartas de nivel I ya se notan) y llega al máximo con una baraja llena; más barajas no lo suben, pero añaden reserva y poder.
- **Peso** de cada candidato `e`: `w · (1 + 2·R·cuotaCategoría) · (1 + 4·R·cuotaEncantamiento) · (1 + 2·Rr·(10 − w)/9)`. La parte de categoría favorece la categoría frente a las otras (hasta ×3); la exacta, el encantamiento frente al resto de su categoría (hasta ×5, ×15 en total). La cuota de categoría cuenta todas las cartas; la del encantamiento, solo las de nivel máximo. El factor de Rareza solo se aplica con peso ≤ 2. El sesgo es relativo: una librería equilibrada ≈ vanilla.
- **Poder de la mesa**: cada 3 barajas con cartas cuentan como 2 librerías (un atril lleno = las 2 librerías apiladas que cabrían en su columna): `⌊2·barajas/3⌋`. Se suman a las librerías normales y vanilla limita el total a 15. Es lo que sube el nivel, igual que con librerías.
- **Afinidad por lugar** (datapack, `data/<ns>/enchanttransfer/library_affinity/*.json`): cada regla dice qué baraja (`deck`) rinde más en qué `dimension` y/o `biome`, opcionalmente solo para algunos `enchantments`, con un `multiplier`. Escala el extra de la baraja: `factor' = 1 + (factor − 1)·afinidad`. Las reglas que coinciden se multiplican (una específica se suma a una general), con tope ×3. Por defecto: Nether → Combate ×2, Fire/Blast Protection ×1,5, fuego Arcano (Fire Aspect, Flame) ×2, Rareza ×1,5; End → Arcano ×2; océanos y ríos → encantamientos de agua de Naturaleza ×1,5; bosques, junglas y taigas → Fortune y Silk Touch ×1,5; llanuras (`#c:is_plains`) → Harvesting ×1,5.
- **Influencia**: cada elección se acopla a la vanilla (acoplamiento máximo): se hace la tirada vanilla con el random de la mesa y se conserva con probabilidad `min(1, p_baraja / p_vanilla)`; si no, se elige entre los encantamientos favorecidos en proporción a `p_baraja − p_vanilla`. La distribución final es la de las barajas, pero solo cambian una elección que han causado. Esas elecciones son las *influidas*; no cuentan las que salen igual que en vanilla, las que cambian en cadena porque una anterior cambió (se eligen con pesos vanilla) ni las debidas al poder. El azar del acoplamiento sale de un random aparte sembrado por opción, para que la pista coincida con el resultado. La mesa marca las opciones con alguno (carta pequeña arriba a la derecha y línea en el tooltip), sincronizado con un `DataSlot`.
- **Desgaste**: ver *Desgaste de cartas* más abajo.
- Código: paquete `library/` (`DeckEffect` y sus implementaciones, `CardStorage`, `DeckLibrary`, `LibraryProfile`), servicios `LibraryResonanceService`, `DeckEnchantmentSelector`, `DeckWearService` y mixin `EnchantmentMenuMixin`.
- **Atril de barajas** (`DeckLecternBlock`): base baja de latón y madera, bandeja inclinada de terciopelo con 3 barajas (las dibuja un BER con su modelo 3D) y campana de cristal escalonada. Un piloto por hueco (marrón: sin baraja; claro: sin cartas; esmeralda: con cartas) y una amatista en la cúspide que se enciende (con halo y runas hacia la mesa) cuando el atril está encendido. Apagado por defecto: Shift + clic derecho lo enciende (solo con alguna baraja con cartas) o lo apaga; se apaga solo cuando las barajas se quedan sin cartas, pero nunca se enciende solo. Los atriles apagados no cuentan. Clic derecho abre una ventana con 3 huecos. Receta: cristal, amatista, cristal / latón, redstone, latón / tablones ×3. Recursos generados con `scripts/gen_deck_lectern_assets.py`.


### Desgaste de cartas

Las cartas de una baraja se gastan poco a poco, en proporción a lo que la baraja aporta. Solo hay desgaste en las elecciones **influidas** (opciones con marca): nunca por lo que la mesa habría dado igualmente, por los cambios en cadena ni por el poder.

**Nivel relativo.** Los niveles se cuentan respecto al máximo de cada encantamiento (`M`): una carta de Toque de seda I o de Fortuna III es de nivel máximo.

**Reserva.** Cada carta tiene tantos puntos de reserva como puntos ocupa en la baraja (`2^(N-1)`: V = 16, IV = 8, III = 4, II = 2, I = 1). Una baraja llena tiene siempre 64 de reserva, la llenes como la llenes. Se ve como una barra en la carta. Una carta gastada:
- cuenta completa para la bonificación hasta agotarse (entonces desaparece);
- funciona completa en la Transfer Table (aplicar, extraer);
- al combinar dos para subir de nivel, la nueva tiene la **suma** de lo que les quedaba (dos IV enteras, 8 + 8, dan una V entera de 16);
- no se apila con las cartas sin desgaste.

**Cantidad.** Por cada encantamiento influido obtenido a nivel `ℓ`:
```
D = 20 % · reserva de una carta de nivel máximo de ese encantamiento · ℓ / M
```
| Obtienes | D |
|---|---|
| Eficiencia V (M = V, carta máx. = 16) | 3,2 |
| Eficiencia III | 1,92 |
| Irrompibilidad III (M = III, carta máx. = 4) | 0,8 |
| Toque de seda I (M = I, carta máx. = 1) | 0,2 |

Así toda carta aguanta 5 encantamientos de su nivel con bonus exacto, o 10 con bonus de categoría.

**Reparto entre barajas.** Las bonificaciones de los tipos de baraja se multiplican; el desgaste se reparte entre los tipos presentes que afectan a ese encantamiento en proporción a su **extra** (`factor − 1`, con afinidad). Ejemplo: Naturaleza ×2 (+1) y Rareza ×3 (+2) sobre Toque de seda → Naturaleza paga ⅓ y Rareza ⅔. Cada tipo paga su parte con su tasa:
- **Exacto (100 %)**: baraja de categoría cuando hay cartas de nivel máximo de ese encantamiento en barajas de ese tipo.
- **Categoría (50 %)**: baraja de categoría sin cartas de nivel máximo de ese encantamiento.
- **Rareza (50 %)**: comodín, con encantamientos raros (peso ≤ 2).

**Orden.** Dentro de cada tipo: primero la carta ya empezada; si no, la de menor nivel. Lo que sobre al agotarse una carta pasa a la siguiente de menor nivel; al agotarse la baraja, a la siguiente baraja de ese tipo (orden fijo: atriles en el orden de búsqueda de la mesa, huecos de izquierda a derecha). Varios encantamientos influidos de la misma opción suman su desgaste. El desgaste de cada encantamiento se aplica una sola vez, aunque haya varias barajas del tipo.

**Sin reserva.** El desgaste es determinista, así que se calcula al mostrar las opciones (sobre la lista final; en libros, tras quitar vanilla una entrada). Si las barajas de algún tipo no tienen reserva suficiente para su parte, la opción aparece **desactivada** y el tooltip explica que falta reserva en las barajas, como cuando faltan niveles.

**Rareza.** Solo favorece y solo se desgasta con encantamientos raros (peso ≤ 2), al 50 %, gastando cartas de maldición. Así nunca da un bonus que no paga.

### Bonificación

1. Cualquier carta da bonus de **categoría** (frente a otras categorías), además de lo que da vanilla con librerías.
2. Las cartas de nivel I–IV (relativo al máximo) solo dan el de categoría.
3. Las cartas de nivel máximo dan además bonus a su **encantamiento exacto** (y por eso su desgaste es del 100 %).
4. Fuerza acumulativa por puntos con curva de raíz (ver *Resonancia* y *Peso* arriba). Ejemplo, Irrompibilidad en una espada a nivel 30 con cartas de Eficiencia (solo categoría): vanilla 14 %; 4 cartas I → 20 %; 4 cartas III → 24 %; baraja llena → 33 %. Eficiencia en un pico a nivel 30: vanilla 56 %; 1 carta V → 82 %; 4 cartas V → 89 %.

---

## Encantamientos

### Peso y rareza

El **peso** de un encantamiento (vanilla) es su probabilidad relativa en la mesa: cuanto mayor, más común. La mesa elige con probabilidad `peso / suma de pesos` de los candidatos. Vanilla usa 10 (común: Protection, Sharpness, Efficiency, Power), 5 (poco común: Smite, Unbreaking, Feather Falling), 2 (raro: Fortune, Looting, Fire Aspect) y 1 (muy raro: Silk Touch, Thorns, Infinity). Las barajas multiplican esos pesos; la de Rareza solo actúa con peso ≤ 2, así ayuda a lo raro sin volverlo común. Los del mod: Magnetism 2 (raro, entra en Rareza), Harvesting 3.

### Categorías de encantamientos (por color de cristal/gema)

| Color | Categoría | Encantamientos vanilla incluidos |
|---|---|---|
| Azul | Protección | Protection, Fire Protection, Blast Protection, Projectile Protection, Feather Falling |
| Verde | Naturaleza | Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune, Luck of the Sea, Lure, Harvesting* |
| Rojo | Combate | Sharpness, Smite, Bane of Arthropods, Impaling, Looting, Thorns, Sweeping Edge, Knockback, Density, Breach, Lunge, Power, Punch, Multishot, Piercing, Momentum* |
| Amarillo | Utilidad | Efficiency, Unbreaking, Quick Charge, Swift Sneak, Magnetism* |
| Morado | Arcano | Channeling, Riptide, Loyalty, Frost Walker, Wind Burst, Fire Aspect, Flame, Infinity, Mending, Soul Speed, Freezing* + encantamientos propios del mod |
| Negro | Maldición | Curse of Vanishing, Curse of Binding + maldiciones propias del mod |

> \* Encantamientos nuevos del mod. Magnetism y Harvesting están implementados; Momentum y Freezing, solo diseñados (ver más abajo).

### Magnetism *(nuevo — Utilidad/Amarillo)*
- Ítems y orbes de XP cercanos se atraen automáticamente al jugador
- Solo armaduras metálicas (malla, hierro, cobre, oro y netherita; tag `enchanttransfer:enchantable/magnetism`); cuenta puesta, y vale el nivel más alto que lleve
- **Niveles y alcance:** I = 4 bloques · II = 6 · III = 8 · IV = 10 · V = 12
- Agacharse lo desactiva; los ítems que tira el propio jugador (o con retraso de recogida) no se atraen
- Mesa de encantamientos, aldeanos y botín (`non_treasure`); peso 2 (raro)
- No existe en vanilla

### Harvesting *(nuevo — Naturaleza/Verde)*
Azada y hacha; niveles I–IV; mesa de encantamientos, aldeanos y botín (`non_treasure`); peso 3. No existe en vanilla.

**Azada** — clic derecho en un cultivo maduro:
- Lo cosecha (botín normal, Fortuna incluida) y lo vuelve a plantar
- **Solo si el jugador lleva en el inventario la semilla de ese cultivo** (gasta una; en creativo no). Sin semilla, o con el cultivo sin madurar, no hace nada
- Coste de experiencia configurable por mundo con reglas de juego: `enchanttransfer:harvesting_field_xp_cost` (por 9×9) y `enchanttransfer:harvesting_tree_xp_cost` (por árbol), 0,5 niveles por defecto; 0 lo hace gratis
- Desgasta la azada 1 punto por cultivo, igual que arar la tierra; cada cultivo gasta su semilla, así que se cosecha como mucho tantos como semillas lleves
- **II:** además los dos cultivos siguientes en la dirección a la que miras (fila de 3)
- **III:** el campo entero: todos los cultivos iguales conectados en esa capa (los que no están maduros se cruzan pero no se cosechan). Sin tope de tamaño: cuesta **0,5 niveles de experiencia por cada 9×9 empezado** (81 cultivos), cobrados tras cosechar por lo realmente cosechado. Si no llega la experiencia se cosechan solo los 9×9 que se puedan pagar; si no llega ni para uno, hace la fila de II
- **IV:** además hace crecer con polvo de hueso cada cultivo replantado; en campo entero, si llevas polvo de hueso, el coste se duplica (1 nivel por 9×9)
- Agacharse limita la cosecha al cultivo pinchado
- Cultivos: cualquier `CropBlock` (trigo, zanahorias, patatas, remolachas y los de otros mods que usen esa clase, sin configurar nada) + el tag de bloque `enchanttransfer:harvesting_replantable` para los que crecen con una propiedad `age` sin ser `CropBlock` (verruga del Nether, cacao) − el tag `enchanttransfer:harvesting_excluded`. La semilla que gasta es el ítem de "pick block" del cultivo

**Hacha** — al romper un tronco de un **árbol natural** (algún tronco conectado toca hojas que no colocó un jugador, o follaje de `harvesting_foliage`); con construcciones no hace nada:
- **I:** si el tronco roto estaba sobre tierra (la base), planta el plantón del árbol, si el jugador lo lleva (gasta uno)
- **II:** además tala los troncos que hay justo encima, en línea recta (el tronco principal, sin ramas)
- **III:** tala el árbol entero, ramas incluidas (tope de 256 troncos), por **0,5 niveles de experiencia**; sin ellos tala como II
- **IV:** además hace crecer el plantón replantado con polvo de hueso del inventario, uno a uno con la probabilidad de vanilla, hasta que sale el árbol; si no hay sitio, se detiene sin gastar más
- La tala va de arriba abajo y cuesta 1 punto de durabilidad por tronco (para si el hacha se rompe). Agacharse desactiva la tala en cadena
- Árboles de 2×2 (roble oscuro, roble pálido, abeto y jungla gigantes): con III se replanta un plantón en cada una de las 4 bases
- Plantón de cada árbol: reglas de datapack en `data/<ns>/enchanttransfer/harvesting_sapling/*.json` (`logs`: id, lista o `#tag`; `sapling`), con las de vanilla incluidas (el mangle da propágulo; carmesí y distorsionado, hongos sobre nilio). Sin regla, se deduce por nombre en el mismo namespace: `x_log`/`x_stem` → `x_sapling`, `x_propagule` o `x_fungus`; si no existe, se tala pero no se replanta
- Árboles de otros mods: funcionan si sus troncos están en `#minecraft:logs` y sus hojas en `#minecraft:leaves` (lo normal); otro follaje natural se añade al tag `enchanttransfer:harvesting_foliage` (por defecto, los bloques de verruga del Nether)

---

# 📝 Diseñado, sin implementar

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

## Encantamientos pendientes

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

> En revisión: ideas parecidas existen en otros mods (Cyclic, etc.).

### Speed Pad
- Aumenta la velocidad de movimiento al pisarlo
- Crafteado con esencia mágica

### Attractor
- Atrae ítems y orbes de XP cercanos hacia él
- Útil junto a granjas automáticas
- Crafteado con esencia mágica
- Independiente de Magnetism: este atrae hacia un **bloque** (p. ej. para que una tolva los recoja); Magnetism, hacia el jugador

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

---

# ❌ Descartado

| Idea | Motivo |
|---|---|
| Sistema de portales | Demasiado similar a Waystones (mod muy popular). Los cristales de color siguen como material del mod con otros usos |
| Romper reglas vanilla (encantamientos incompatibles, superar nivel máximo, ítems no compatibles) | Más un cheat que una mecánica satisfactoria |
| Scrying Basin (ver otra ubicación en tiempo real) | Demasiado costoso en servidor |
| Echo Module (duplicar cartas con mucho XP) | Controvertido, rompe balance |
| Curse Siphon, Tome Binder, Auto-Combiner | No suficientemente diferenciadores |
| Efectos de área sin catalizador | Demasiado pasivo, no convence |

