# Barajas de cartas: changelog y documentación (borrador 3.1.0)

Borrador de la próxima versión con las barajas de cartas (rama `feature/CardDecks`). Contiene el changelog y los textos para ampliar la web, Modrinth y CurseForge.

> **Estado:** todo lo de este documento **ya está aplicado** en la rama: README (sección, receta y changelog 3.1.0), `docs/index.html` y `docs/style.css` (sección "Card Decks", tabla de categorías, menú y changelog), `docs/curseforge-description.html` y `docs/modrinth-description.md` (regenerado). Los fragmentos se quedan aquí como referencia.

> **Versión propuesta: 3.1.0.** Añade contenido nuevo pero no rompe mundos existentes, así que es una versión menor. Solo cubre la línea 26.2; la de mantenimiento 1.21.11 no tiene barajas.

---

## 1. Changelog

Va en `README.md` como nueva sección `## Changelog — 3.1.0`, encima de la de 3.0.0 (que pasa a "Previous versions"). El texto de Modrinth se regenera desde el README.

```markdown
## Changelog — 3.1.0

- **Card Decks** — New brass-bound decks that store Magic Cards of one type: Protection, Nature, Combat, Utility, Arcane, and a Rarity wildcard filled with Curse cards. Crafted with 2 Brass Ingots, 2 Leather and 1 card of the deck's type (the card is consumed). Store and take cards with right-click, like a bundle
- **Deck Lectern** — Place decks in a Deck Lectern near an Enchanting Table: every 3 decks holding cards count as one bookshelf of enchanting power (a full shelf = 2), adding up with regular bookshelves (vanilla cap of 15 still applies)
- **Steered enchanting** — Decks make the table favor their category, and even more the exact enchantments stored (up to ×3 with a specialized library). The Rarity wildcard favors rare, low-weight enchantments
- **Location affinity** — Decks resonate with where the table is: Combat ×2 in the Nether (Fire Aspect ×3), fire protections and Flame ×1.5 in the Nether, Arcane ×2 in the End, Nature's water enchantments in oceans and rivers, Fortune and Silk Touch in forests, jungles and taigas
- **Datapack-driven affinities** — Affinity rules are JSON files in `data/<namespace>/enchanttransfer/library_affinity/`, so modpacks can add rules for any dimension or biome; they reload with `/reload`
- **Deck wear** — Each enchantment the library helps with has a 20% chance to consume the cheapest matching card
- **26.2 enchantments categorized** — Knockback, Density, Breach, Lunge, Flame, Multishot, Piercing, Quick Charge, Luck of the Sea, Lure and Wind Burst now have card colors
- **Arcane = rare magic** — All treasure enchantments (Mending, Swift Sneak, Soul Speed, Frost Walker, Wind Burst) are now Arcane cards. Cards already extracted keep their color and still go into the matching deck
```

<details>
<summary>Mismo changelog en español (por si quieres usarlo en Discord u otros canales)</summary>

- **Barajas de cartas**: estuches de latón que guardan cartas de un tipo (Protección, Naturaleza, Combate, Utilidad, Arcano) y un comodín de Rareza que se llena con cartas de maldición. Receta: 2 lingotes de latón, 2 de cuero y 1 carta del tipo, que se consume. Se guardan y sacan cartas con clic derecho, como en un saco.
- **Atril de barajas**: en un atril de barajas cerca de una mesa de encantamientos, cada 3 barajas con cartas cuentan como una librería (una llena = 2). Se suman a las normales, con el tope de 15 de vanilla.
- **Encantamientos dirigidos**: las barajas favorecen su categoría y, sobre todo, los encantamientos que guardan (hasta ×3). El comodín de Rareza favorece los encantamientos raros.
- **Afinidad por lugar**: Combate ×2 en el Nether (Fire Aspect ×3), protecciones contra fuego y Flame ×1,5 en el Nether, Arcano ×2 en el End, y Naturaleza según el bioma (agua en océanos y ríos; Fortune y Silk Touch en bosques, junglas y taigas).
- **Configurable por datapack**: reglas JSON para cualquier dimensión o bioma, también de otros mods; se recargan con `/reload`.
- **Desgaste**: cada encantamiento ayudado tiene un 20 % de probabilidad de consumir la carta más barata.
- **Encantamientos de 26.2 con categoría**: 11 encantamientos que no tenían color ahora lo tienen.
- **Arcano = magia rara**: todos los encantamientos de tesoro pasan a ser cartas arcanas.

</details>

---

## 2. Nuevos assets

| Asset | Ruta | Uso |
|---|---|---|
| <img src="docs/images/card_deck_blue.png" height="64"> <img src="docs/images/card_deck_green.png" height="64"> <img src="docs/images/card_deck_red.png" height="64"> <img src="docs/images/card_deck_yellow.png" height="64"> <img src="docs/images/card_deck_purple.png" height="64"> <img src="docs/images/card_deck_rare.png" height="64"> | `docs/images/card_deck_<tipo>.png` (88×135) | Renders 3D de las barajas para la web, Modrinth y CurseForge |
| <img src="docs/images/render_transfer_table.png" height="64"> <img src="docs/images/render_zinc_smelter.png" height="64"> <img src="docs/images/render_infusion_coil.png" height="64"> | `docs/images/render_<bloque>.png` | Sustituyen al 3D en vivo de la web (ya aplicado, ver abajo) |
| <img src="docs/images/blast_furnace.png" height="48"> <img src="docs/images/glass_block.png" height="48"> | `docs/images/blast_furnace.png`, `glass_block.png` (128×128) | Ingredientes vanilla tal y como se ven en el inventario, renderizados desde el jar de Minecraft (ya aplicado) |
| Texturas del modelo 3D | `src/main/resources/assets/enchanttransfer/textures/item/card_deck_<tipo>.png` (32×32) | Caras del modelo en el juego |

Todos los renders salen de los **modelos reales del juego** (JSON y texturas de `src/main/resources`), con la misma perspectiva y sombreado que la propuesta de diseño, en PNG con transparencia. Si cambia un modelo o una textura, basta con regenerarlos:

```bash
python scripts/gen_deck_textures.py
```

```bash
python scripts/render_model_icons.py
```

### Web más ligera en móvil (ya aplicado en esta rama)

La web pintaba la mesa, el fundidor y la bobina con 3D en CSS generado por JavaScript (`blocks3d.js`), y eso ralentizaba los móviles. Ahora son imágenes estáticas:
- `docs/index.html` usa `<img class="render-icon">` con los renders.
- `docs/blocks3d.js` se ha eliminado y `docs/style.css` cambia las reglas `.block-3d-wrap` por `.render-icon`.

Lo único que se pierde es lo que pinta el juego en tiempo real y no está en el modelo: el brillo del núcleo de la mesa, el fluido de la bobina y el fuego del fundidor. El fundidor se renderiza con su modelo encendido, así que la mirilla sí se ve.

Falta una captura del juego: una mesa rodeada de atriles de barajas. Sería la mejor imagen para la web y para la galería de Modrinth y CurseForge, pero hay que hacerla con `runClient`.

---

## 3. Web (`docs/index.html`)

### 3.1. Navegación

En el menú (`<nav class="nav-menu">`), encima de "Typed Cards":

```html
  <a href="#card-decks">Card Decks <span class="nav-badge">New</span></a>
```

Y quitar la etiqueta `New` de las secciones de 2.0 (Typed Cards, Zinc Smelter, Infusion Coil), que ya no son novedad.

### 3.2. Tabla de categorías (sección "Typed Magic Cards")

Sustituir las filas de Verde, Rojo, Amarillo y Morado:

```html
        <tr>
          <td><img class="card-icon" src="images/magic_card_green.png" alt="">Green</td>
          <td>Nature</td>
          <td>Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune, Luck of the Sea, Lure</td>
        </tr>
        <tr>
          <td><img class="card-icon" src="images/magic_card_red.png" alt="">Red</td>
          <td>Combat</td>
          <td>Sharpness, Smite, Bane of Arthropods, Impaling, Fire Aspect, Looting, Thorns, Sweeping Edge, Knockback, Density, Breach, Lunge</td>
        </tr>
        <tr>
          <td><img class="card-icon" src="images/magic_card_yellow.png" alt="">Yellow</td>
          <td>Utility</td>
          <td>Efficiency, Unbreaking, Infinity, Power, Punch, Flame, Multishot, Piercing, Quick Charge</td>
        </tr>
        <tr>
          <td><img class="card-icon" src="images/magic_card_purple.png" alt="">Purple</td>
          <td>Arcane</td>
          <td>Channeling, Riptide, Loyalty, Frost Walker, Wind Burst, Mending, Swift Sneak, Soul Speed</td>
        </tr>
```

### 3.3. Nueva sección "Card Decks"

Va justo después de la sección "Typed Magic Cards", antes de `<!-- ZINC SMELTER -->`. Usa las clases que ya existen (`card-sprites`, `card-table`, `recipe`, `render-icon`). Como los renders de las barajas no son cuadrados, hacen falta tres reglas en `docs/style.css` para que no se deformen:

```css
.card-sprite img.deck-sprite { width: auto; height: 64px; image-rendering: auto; }
.card-table .card-icon.deck-icon { width: auto; height: 28px; image-rendering: auto; }
@media (max-width: 500px) { .card-sprite img.deck-sprite { height: 52px; } }
```

```html
  <!-- CARD DECKS -->
  <h2 id="card-decks"><span class="accent">Card</span> Decks <span class="badge badge-new">New</span></h2>

  <p>Brass-bound decks that store Magic Cards of one type. Place them in a <strong>Chiseled Bookshelf</strong> near an Enchanting Table and the table gains power and starts favoring what you've stored.</p>

  <div class="card-sprites">
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_blue.png" alt="Protection Deck"><span>Protection</span></div>
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_green.png" alt="Nature Deck"><span>Nature</span></div>
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_red.png" alt="Combat Deck"><span>Combat</span></div>
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_yellow.png" alt="Utility Deck"><span>Utility</span></div>
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_purple.png" alt="Arcane Deck"><span>Arcane</span></div>
    <div class="card-sprite"><img class="deck-sprite" src="images/card_deck_rare.png" alt="Rarity Wildcard Deck"><span>Rarity</span></div>
  </div>

  <div class="recipe">
    <div class="recipe-title">Crafting Recipe</div>
    <div class="recipe-layout">
      <div class="recipe-grid">
        <div class="recipe-cell"></div>
        <div class="recipe-cell"><img src="images/brass_ingot.png" alt="">Brass Ingot</div>
        <div class="recipe-cell"></div>
        <div class="recipe-cell"><img src="images/leather.png" alt="">Leather</div>
        <div class="recipe-cell"><img src="images/magic_card_red.png" alt="">Card of the deck's type</div>
        <div class="recipe-cell"><img src="images/leather.png" alt="">Leather</div>
        <div class="recipe-cell"></div>
        <div class="recipe-cell"><img src="images/brass_ingot.png" alt="">Brass Ingot</div>
        <div class="recipe-cell"></div>
      </div>
      <span class="recipe-arrow-large">→</span>
      <div class="recipe-output">
        <img class="render-icon" src="images/card_deck_red.png" alt="Card Deck" width="88" height="135">
        <span>Card Deck</span>
      </div>
    </div>
    <p class="recipe-result">The card is consumed. The Rarity wildcard uses a <strong>Curse</strong> (black) card.</p>
  </div>

  <h3>Storing cards</h3>
  <ul>
    <li><strong>Right-click</strong> a card onto the deck (or the deck onto a card) to store it, like a bundle</li>
    <li>Right-click with an empty cursor to take the last stack back out</li>
    <li>A level N card is worth 2<sup>N−1</sup> points (the same ratio as combining). Each deck holds up to <strong>64 points</strong></li>
  </ul>

  <h3>At the Enchanting Table</h3>
  <div class="card-table-wrap">
    <table class="card-table">
      <thead><tr><th>Effect</th><th>How it works</th></tr></thead>
      <tbody>
        <tr><td>Power</td><td>Every 3 decks holding cards count as one bookshelf (a full Chiseled Bookshelf = 2). They add up with regular bookshelves; the vanilla cap of 15 still applies</td></tr>
        <tr><td>Category decks</td><td>Favor their category, and even more the exact enchantments stored — up to ×3 with a specialized library. A library with every category in equal parts barely changes anything</td></tr>
        <tr><td>Rarity wildcard</td><td>Favors rare, low-weight enchantments (Silk Touch, Thorns, Infinity…)</td></tr>
        <tr><td>Wear</td><td>Each enchantment the library helps with has a 20% chance to consume the cheapest matching card</td></tr>
      </tbody>
    </table>
  </div>

  <h3>Location affinity</h3>
  <p>Decks resonate with where the table is. Affinity multiplies the deck's extra effect (capped at ×3) and never affects a table without that kind of deck.</p>
  <div class="card-table-wrap">
    <table class="card-table">
      <thead><tr><th>Where</th><th>Deck</th><th>Boost</th></tr></thead>
      <tbody>
        <tr><td>Nether</td><td><img class="card-icon deck-icon" src="images/card_deck_red.png" alt="">Combat</td><td>×2 (Fire Aspect ×3)</td></tr>
        <tr><td>Nether</td><td><img class="card-icon deck-icon" src="images/card_deck_blue.png" alt="">Protection</td><td>Fire &amp; Blast Protection ×1.5</td></tr>
        <tr><td>Nether</td><td><img class="card-icon deck-icon" src="images/card_deck_yellow.png" alt="">Utility</td><td>Flame ×1.5</td></tr>
        <tr><td>Nether</td><td><img class="card-icon deck-icon" src="images/card_deck_rare.png" alt="">Rarity</td><td>×1.5</td></tr>
        <tr><td>End</td><td><img class="card-icon deck-icon" src="images/card_deck_purple.png" alt="">Arcane</td><td>×2</td></tr>
        <tr><td>Oceans &amp; rivers</td><td><img class="card-icon deck-icon" src="images/card_deck_green.png" alt="">Nature</td><td>Water enchantments ×1.5</td></tr>
        <tr><td>Forests, jungles &amp; taigas</td><td><img class="card-icon deck-icon" src="images/card_deck_green.png" alt="">Nature</td><td>Fortune &amp; Silk Touch ×1.5</td></tr>
      </tbody>
    </table>
  </div>

  <h3>For modpacks</h3>
  <p>Affinities are datapack JSON files in <code>data/&lt;namespace&gt;/enchanttransfer/library_affinity/</code> and reload with <code>/reload</code>. Add rules for any dimension or biome, including other mods':</p>
  <pre><code>{
  "deck": "enchanttransfer:card_deck_purple",
  "dimension": "twilightforest:twilight_forest",
  "enchantments": ["minecraft:loyalty"],
  "multiplier": 2.0
}</code></pre>
  <p><code>deck</code> and <code>multiplier</code> are required; <code>dimension</code>, <code>biome</code> (id, list or <code>#tag</code>) and <code>enchantments</code> are optional.</p>

  <hr class="sep">
```

`docs/images/leather.png` ya está añadido: es el icono vanilla del cuero, sacado del jar de 26.2 y escalado a 128×128 como el resto.

---

## 4. Modrinth (`docs/modrinth-description.md`)

No se edita a mano: se genera desde `README.md` con `scripts/build-modrinth-description.sh`, que el CI ejecuta en cada release. Basta con:

1. **La sección "Card Decks" del README** ya está escrita (entre "Typed Magic Cards" y "Module System"). Solo faltaría añadir los iconos debajo del título, igual que en la sección de cartas:

   ```markdown
   <img src="docs/images/card_deck_blue.png" height="56" alt="Protection Deck"> <img src="docs/images/card_deck_green.png" height="56" alt="Nature Deck"> <img src="docs/images/card_deck_red.png" height="56" alt="Combat Deck"> <img src="docs/images/card_deck_yellow.png" height="56" alt="Utility Deck"> <img src="docs/images/card_deck_purple.png" height="56" alt="Arcane Deck"> <img src="docs/images/card_deck_rare.png" height="56" alt="Rarity Deck">
   ```

2. **El changelog 3.1.0** del apartado 1.
3. **Regenerar**, cambiando las rutas `docs/images/` a URLs absolutas de GitHub:

   ```bash
   ./scripts/build-modrinth-description.sh
   ```

Para la galería de Modrinth: la captura de la librería con barajas y los seis iconos.

---

## 5. CurseForge (`docs/curseforge-description.html`)

CurseForge usa HTML simple con imágenes absolutas de GitHub. Va después de la sección "Typed Magic Cards (New in 2.0)".

```html
<h2>Card Decks (New in 3.1)</h2>

<p>
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_blue.png" height="56" alt="Protection Deck">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_green.png" height="56" alt="Nature Deck">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_red.png" height="56" alt="Combat Deck">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_yellow.png" height="56" alt="Utility Deck">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_purple.png" height="56" alt="Arcane Deck">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_rare.png" height="56" alt="Rarity Deck">
</p>

<p>Brass-bound decks that store Magic Cards of one type. Place them in a <b>Chiseled Bookshelf</b> near an Enchanting Table and the table gains power and starts favoring what you've stored.</p>

<ul>
<li><b>Power</b> — Every 3 decks holding cards count as one bookshelf (a full Chiseled Bookshelf = 2), adding up with regular bookshelves (cap of 15)</li>
<li><b>Category decks</b> — Favor their category, and even more the exact enchantments stored (up to ×3)</li>
<li><b>Rarity wildcard</b> — Filled with Curse cards; favors rare, low-weight enchantments</li>
<li><b>Location affinity</b> — Combat ×2 in the Nether (Fire Aspect ×3), Arcane ×2 in the End, Nature depends on the biome</li>
<li><b>Wear</b> — Each enchantment the library helps with has a 20% chance to consume a card</li>
<li><b>Modpack friendly</b> — Affinities are datapack JSON (<code>data/&lt;namespace&gt;/enchanttransfer/library_affinity/</code>), for any dimension or biome</li>
</ul>

<h3>Crafting Recipe — Card Deck</h3>
<table>
<tr><td></td><td>Brass Ingot</td><td></td></tr>
<tr><td>Leather</td><td>Card of the deck's type</td><td>Leather</td></tr>
<tr><td></td><td>Brass Ingot</td><td></td></tr>
</table>
<p><i>The card is consumed. The Rarity wildcard uses a Curse (black) card.</i></p>

<hr>
```

Y sustituir "What's New in 3.0.0" por:

```html
<h2>What's New in 3.1.0</h2>

<ul>
<li><b>Card Decks</b> — Store Magic Cards in brass-bound decks: Protection, Nature, Combat, Utility, Arcane and a Rarity wildcard</li>
<li><b>Libraries that enchant</b> — Decks in Chiseled Bookshelves add enchanting power and steer the table toward their enchantments</li>
<li><b>Location affinity</b> — Decks work better in the right dimension or biome; configurable by datapack</li>
<li><b>26.2 enchantments categorized</b> — 11 enchantments that had no card color now have one; treasure enchantments are now Arcane</li>
</ul>
```

También hay que actualizar la tabla de categorías de CurseForge (líneas de Green, Red, Yellow y Purple) con las mismas listas del apartado 3.2.

---

## 6. Checklist antes de publicar

- [ ] Probar en el juego con `runClient`: modelo 3D, guardar y sacar cartas, atril de barajas (pilotos, activar/desactivar, barajas en la bandeja), mesa y afinidades.
- [ ] Hacer la captura de la librería con barajas para la web y las galerías.
- [x] Añadir `docs/images/leather.png`.
- [ ] Revisar la web en móvil con los renders estáticos.
- [ ] Subir `mod_version` a `3.1.0` (el CI lo hace al publicar la release).
- [x] Aplicar los cambios de los apartados 3 y 5, y regenerar Modrinth (apartado 4).
- [ ] Ejecutar `scripts/sync-release-docs.py` al publicar (ya fallaba en `master` antes de esta rama; el CI lo ejecuta en cada release).
