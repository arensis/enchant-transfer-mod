#!/usr/bin/env python3
"""Genera los recursos del atril de barajas (Deck Lectern).

- Texturas del bloque (latón, madera, terciopelo, cristal, pilotos, amatista)
- Fondo de la ventana (176x166, misma paleta que el resto de pantallas)
- Modelos: base con bandeja y campana, un piloto por hueco y estado, remate
- Blockstate por partes (facing + slot_0..2 + active) y modelo del objeto

La geometría sigue la propuesta "H · Atril bajo campana" del lienzo de diseño;
la bandeja debe coincidir con DeckLecternRenderer (TRAY_CENTER y TRAY_TILT).

Uso:  python scripts/gen_deck_lectern_assets.py
"""
import json
import os

from pngio import write_png

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
ASSETS = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'enchanttransfer')
NS = 'enchanttransfer'

# ── Paletas ─────────────────────────────────────────────────────────────────
B = dict(H='ffe888', L='f0d060', M='dab040', D='a07828', S='705010')
W = dict(base='5a3d24', light='6e4b2c', dark='47301c', seam='34220f', grain='7d5733')
VELVET, VELVET_D = '2a1a2e', '1e1222'
AM_ON = ['f6e0ff', 'd2a2ff', 'a66ce6', '6d3fb0']
AM_OFF = ['6a5a7a', '54466a', '3f3452', '2c2340']
LAMPS = {'none': '4a1e10', 'empty': 'fff1b8', 'full': '2bdc6b'}
LAMP_X = [2, 7, 11]          # left edge of each slot's 3-px lamp on the front
SLOTS = 3
# Tray centre (px) and tilt: its front edge rests just on top of the 5-px base
TRAY_CENTER = [8, 7.6, 8.5]
TRAY_TILT = 18

GUI = dict(frame='8a6030', inner='5a3a18', bg='1e160e', title='c89030', title_line='8a6020',
           slot_border='3a2810', slot_bg='0e0a06')


def planks(horizontal):
    def at(x, y):
        a, b = (y, x) if horizontal else (x, y)
        if a % 4 == 0:
            return W['light']
        if a % 4 == 3:
            return W['seam']
        if (b * 7 + a * 3) % 11 == 0:
            return W['dark']
        if (b * 5 + a) % 13 == 0:
            return W['grain']
        return W['base']
    return at


def grid(at, w=16, h=16):
    return [[at(x, y) for x in range(w)] for y in range(h)]


def front(x, y):
    """Front of the base: rows 11..15 hold the 5-px brass panel (lamps are separate elements)."""
    if y < 11:
        return B['M']
    r = y - 11
    if r == 0:
        return B['D'] if x in (0, 15) else B['L']
    if r == 4:
        return B['D'] if x in (0, 15) else B['S']
    if x == 0:
        return B['L']
    if x == 15:
        return B['D']
    if r == 2 and any(lx <= x <= lx + 2 for lx in LAMP_X):
        return B['S']
    if x in (1, 14) and r in (1, 3):
        return B['D']
    return B['M']


def side(x, y):
    return B['M'] if x == 0 else (B['D'] if x == 1 else planks(True)(x, y))


def top(x, y):
    return B['M'] if y == 15 else (B['D'] if y == 14 else planks(False)(x, y))


def tray(x, y):
    if x >= 14 or y >= 13:
        return B['M']
    if x == 0 or y == 0:
        return B['L']
    if x == 13 or y == 12:
        return B['D']
    return VELVET_D if (x + y) % 5 == 0 else VELVET


def glass(x, y):
    if x < 4 and (x + y) % 7 in (2, 3):
        return (255, 255, 255, 107)
    if x in (0, 15) or y in (0, 15):
        return (200, 230, 255, 70)
    return (190, 225, 255, 41)


def amethyst(palette):
    def at(x, y):
        d = abs(x - 7.5) + abs(y - 7.5)
        if d < 3:
            return palette[0]
        if d < 6:
            return palette[1]
        if d < 9:
            return palette[2]
        return palette[3]
    return at


def lamp(color):
    def at(x, y):
        return color
    return at


def gui():
    px = [[None] * 256 for _ in range(256)]

    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[y][x] = c

    def slot(x, y, border):
        rect(x, y, x + 18, y + 18, border)
        rect(x + 1, y + 1, x + 17, y + 17, GUI['slot_bg'])

    rect(0, 0, 176, 166, GUI['frame'])
    rect(2, 2, 174, 164, GUI['inner'])
    rect(3, 3, 173, 163, GUI['bg'])
    rect(5, 4, 171, 16, GUI['title'])
    rect(5, 16, 171, 17, GUI['title_line'])
    for sx in (53, 80, 107):                        # deck slots (item at sx, 27)
        slot(sx - 1, 26, GUI['frame'])
        rect(sx + 1, 47, sx + 15, 52, GUI['slot_bg'])  # lamp socket
    rect(144, 27, 158, 41, GUI['frame'])            # amethyst gem socket
    rect(145, 28, 157, 40, GUI['slot_bg'])
    rect(8, 66, 168, 67, GUI['slot_border'])        # separator
    rect(86, 65, 90, 68, GUI['title'])
    for row in range(3):
        for col in range(9):
            slot(7 + col * 18, 83 + row * 18, GUI['slot_border'])
    for col in range(9):
        slot(7 + col * 18, 141, GUI['slot_border'])
    return px


# ── Models ──────────────────────────────────────────────────────────────────

def faces(tex, sizes, skip=()):
    """Faces with UVs sized to the element so textures aren't stretched."""
    w, h, d = sizes
    uv = {
        'north': [0, 0, w, h], 'south': [0, 0, w, h],
        'east': [0, 0, d, h], 'west': [0, 0, d, h],
        'up': [0, 0, w, d], 'down': [0, 0, w, d],
    }
    return {k: {'uv': [min(16, v) for v in uv[k]], 'texture': tex} for k in uv if k not in skip}


def element(name, f, t, face_map, **extra):
    e = {'name': name, 'from': f, 'to': t, 'faces': face_map}
    e.update(extra)
    return e


def brass_box(name, f, t, skip=()):
    sizes = [round(t[i] - f[i], 3) for i in range(3)]
    return element(name, f, t, faces('#brass', sizes, skip))


def glass_box(name, f, t):
    face_map = {k: {'uv': [0, 0, 16, 16], 'texture': '#glass'} for k in ('north', 'south', 'east', 'west', 'up')}
    return element(name, f, t, face_map)


def base_model():
    base = element('base', [0, 0, 0], [16, 5, 16], {
        'north': {'uv': [16, 11, 0, 16], 'texture': '#side'},
        'south': {'uv': [0, 11, 16, 16], 'texture': '#front'},
        'east': {'uv': [0, 11, 16, 16], 'texture': '#side'},
        'west': {'uv': [16, 11, 0, 16], 'texture': '#side'},
        'up': {'uv': [0, 0, 16, 16], 'texture': '#top'},
        'down': {'uv': [0, 0, 16, 16], 'texture': '#bottom'},
    })
    tray_faces = faces('#brass', [14, 1, 13])
    tray_faces['up'] = {'uv': [0, 0, 14, 13], 'texture': '#tray'}
    cy = TRAY_CENTER[1]
    tray_el = element('tray', [1, cy - 0.5, 2], [15, cy + 0.5, 15], tray_faces,
                      rotation={'origin': TRAY_CENTER, 'axis': 'x', 'angle': TRAY_TILT})
    elements = [
        base, tray_el,
        brass_box('ring_base', [0.6, 5, 1.3], [15.4, 5.5, 15.7], skip=('down',)),
        glass_box('bell_lower', [0.8, 5.5, 1.5], [15.2, 12, 15.5]),
        brass_box('rib_sw', [0.8, 5.5, 15.1], [1.2, 12, 15.5]),
        brass_box('rib_se', [14.8, 5.5, 15.1], [15.2, 12, 15.5]),
        brass_box('rib_ne', [14.8, 5.5, 1.5], [15.2, 12, 1.9]),
        brass_box('rib_nw', [0.8, 5.5, 1.5], [1.2, 12, 1.9]),
        # Hollow brass frame between the two glass tiers
        brass_box('ring_middle_front', [2.6, 12, 13.3], [13.4, 12.4, 13.7]),
        brass_box('ring_middle_back', [2.6, 12, 3.3], [13.4, 12.4, 3.7]),
        brass_box('ring_middle_left', [2.6, 12, 3.7], [3.0, 12.4, 13.3]),
        brass_box('ring_middle_right', [13.0, 12, 3.7], [13.4, 12.4, 13.3]),
        glass_box('bell_upper', [3, 12.4, 3.7], [13, 14.4, 13.3]),
        brass_box('ring_top', [5.6, 14.4, 6.3], [10.4, 14.7, 10.7]),
    ]
    return {
        'parent': 'minecraft:block/block',
        'render_type': 'minecraft:translucent',
        'textures': {
            'particle': f'{NS}:block/deck_lectern_side',
            'front': f'{NS}:block/deck_lectern_front',
            'side': f'{NS}:block/deck_lectern_side',
            'top': f'{NS}:block/deck_lectern_top',
            'bottom': f'{NS}:block/deck_lectern_bottom',
            'tray': f'{NS}:block/deck_lectern_tray',
            'glass': f'{NS}:block/deck_lectern_glass',
            'brass': f'{NS}:block/infusor_brass',
        },
        'elements': elements,
    }


def lamp_model(slot, state):
    x = LAMP_X[slot]
    el = element(f'lamp_{slot}', [x, 2, 15.9], [x + 3, 3, 16.05], {
        'south': {'uv': [0, 0, 3, 1], 'texture': '#lamp'},
        'up': {'uv': [0, 0, 3, 1], 'texture': '#lamp'},
    })
    if state == 'full':
        el['light_emission'] = 12
    return {
        'parent': 'minecraft:block/block',
        'textures': {'particle': f'{NS}:block/deck_lectern_side', 'lamp': f'{NS}:block/deck_lectern_lamp_{state}'},
        'elements': [el],
    }


def finial_model(on):
    el = element('finial', [7.2, 14.7, 7.9], [8.8, 16.3, 9.5],
                 faces('#amethyst', [1.6, 1.6, 1.6], skip=('down',)))
    if on:
        el['light_emission'] = 14
    return {
        'parent': 'minecraft:block/block',
        'textures': {
            'particle': f'{NS}:block/deck_lectern_side',
            'amethyst': f'{NS}:block/deck_lectern_amethyst_{"on" if on else "off"}',
        },
        'elements': [el],
    }


FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def blockstate():
    parts = []
    for facing, y in FACING_Y.items():
        rot = {'y': y} if y else {}
        parts.append({'when': {'facing': facing}, 'apply': {'model': f'{NS}:block/deck_lectern', **rot}})
        for slot in range(SLOTS):
            for state in LAMPS:
                parts.append({'when': {'facing': facing, f'slot_{slot}': state},
                              'apply': {'model': f'{NS}:block/deck_lectern_lamp_{slot}_{state}', **rot}})
        for active in ('true', 'false'):
            model = 'deck_lectern_finial_on' if active == 'true' else 'deck_lectern_finial_off'
            parts.append({'when': {'facing': facing, 'active': active},
                          'apply': {'model': f'{NS}:block/{model}', **rot}})
    return {'multipart': parts}


def item_definition():
    models = [f'{NS}:block/deck_lectern'] + [f'{NS}:block/deck_lectern_lamp_{s}_none' for s in range(SLOTS)] \
        + [f'{NS}:block/deck_lectern_finial_on']
    return {'model': {'type': 'minecraft:composite',
                      'models': [{'type': 'minecraft:model', 'model': m} for m in models]}}


def write_json(rel, data):
    path = os.path.join(ASSETS, *rel.split('/'))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(data, f, indent=2)
        f.write('\n')
    print('wrote', rel)


def write_texture(rel, rows):
    path = os.path.join(ASSETS, *rel.split('/'))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    write_png(path, rows)
    print('wrote', rel)


if __name__ == '__main__':
    write_texture('textures/block/deck_lectern_front.png', grid(front))
    write_texture('textures/block/deck_lectern_side.png', grid(side))
    write_texture('textures/block/deck_lectern_top.png', grid(top))
    write_texture('textures/block/deck_lectern_bottom.png', grid(planks(True)))
    write_texture('textures/block/deck_lectern_tray.png', grid(tray))
    write_texture('textures/block/deck_lectern_glass.png', grid(glass))
    write_texture('textures/block/deck_lectern_amethyst_on.png', grid(amethyst(AM_ON)))
    write_texture('textures/block/deck_lectern_amethyst_off.png', grid(amethyst(AM_OFF)))
    for state, color in LAMPS.items():
        write_texture(f'textures/block/deck_lectern_lamp_{state}.png', grid(lamp(color)))
    write_texture('textures/gui/container/deck_lectern_gui.png', gui())

    write_json('models/block/deck_lectern.json', base_model())
    for slot in range(SLOTS):
        for state in LAMPS:
            write_json(f'models/block/deck_lectern_lamp_{slot}_{state}.json', lamp_model(slot, state))
    write_json('models/block/deck_lectern_finial_on.json', finial_model(True))
    write_json('models/block/deck_lectern_finial_off.json', finial_model(False))
    write_json('blockstates/deck_lectern.json', blockstate())
    write_json('items/deck_lectern.json', item_definition())
