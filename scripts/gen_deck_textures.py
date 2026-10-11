#!/usr/bin/env python3
"""Genera las texturas de las barajas (estuche de latón) sin dependencias.

Cada textura es una hoja de 32x32 con las caras del modelo 3D
(models/item/card_deck_base.json, cubo de 10x14x4 px):

    portada  10x14 en (0, 0)   -> caras north/south
    canto     4x14 en (10, 0)  -> caras east/west (cierre de latón)
    tapa     10x4  en (14, 0)  -> cara up
    base     10x4  en (14, 4)  -> cara down

La portada reproduce la carta mágica de su color (mismo campo, gema
central y remaches plateados) dentro de un marco de latón con los
colores del lingote de latón.

Uso:  python scripts/gen_deck_textures.py
"""
import os

from pngio import write_png

OUT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources',
                   'assets', 'enchanttransfer', 'textures', 'item')

# Latón (de brass_ingot.png)
BH, BL, BM, BD, BS = 'ffe888', 'f0d060', 'dab040', 'a07828', '705010'
# Comunes a todas las cartas: remaches plateados y gema central
COMMON = {'D': 'a0b0c8', 'E': '607080', 'H': 'a060e0', 'I': 'c090ff', 'J': '80e8ff', 'L': 'ffffff'}

# Paletas de magic_card_<color>.png: B claro, C campo, K medio, F/G oscuros, M/N plata
PALETTES = {
    'blue':   dict(B='60a8ff', C='1a4eb0', F='09091e', G='0c173b', K='5296ef', M='859cc3', N='52698a'),
    'green':  dict(B='50d060', C='1a7a30', F='08100a', G='0c2512', K='45bf56', M='85a5aa', N='527270'),
    'red':    dict(B='ff5040', C='b01820', F='180606', G='360a0b', K='ef453a', M='a392a6', N='705e6d'),
    'yellow': dict(B='ffd030', C='a07a10', F='12100a', G='2e250b', K='ecbf2a', M='a0a5a3', N='6d726a'),
    'purple': dict(B='c070ff', C='6020b0', F='0c0618', G='1d0b36', K='ad60ef', M='9393c3', N='60608a'),
    # Comodín de Rareza: se llena con cartas de maldición, usa su paleta (negra)
    'rare':   dict(B='888888', C='303030', F='080808', G='101010', K='767676', M='8a96aa', N='566370'),
}

# Interior de la carta (x4..11, y2..13 de magic_card_*.png); '*' = esquina de latón
CARD_FACE = [
    '*CCDECC*',
    'CFDBFEFC',
    'CDGGGGEC',
    'CGGHGGGC',
    'CGIJIGGC',
    'KHJLJHGK',
    'CGIJIGGC',
    'CGGHGGGC',
    'CDGGGGEC',
    'CFMGGNFC',
    'CFFBEFFC',
    '*CCCCCC*',
]


def front(p):
    rows = [[BD] + [BL, BH] + [BL] * 6 + [BD]]
    for line in CARD_FACE:
        inner = [BL if ch == '*' else (COMMON.get(ch) or p[ch]) for ch in line]
        rows.append([BM] + inner + [BD])
    rows.append([BD] + [BS] * 8 + [BD])
    return rows


def side(p):
    rows = []
    for y in range(14):
        if y == 0:
            rows.append([BM] * 4)
        elif y == 13:
            rows.append([BS] * 4)
        elif y in (2, 11):
            rows.append([BD, BM, BM, BD])
        elif y == 5:
            rows.append([BD, BL, BH, BD])
        elif y in (6, 7):
            rows.append([BD, BM, BL, BD])
        elif y == 8:
            rows.append([BS, BD, BD, BS])
        else:
            rows.append([p['C'], p['K'], p['C'], p['C']])
    return rows


def top(p):
    return [
        [BL] * 10,
        [BM] + [p['B']] * 8 + [BD],
        [BM] + [p['C']] * 8 + [BD],
        [BS] * 10,
    ]


def bottom(p):
    return [
        [BD] * 10,
        [BD] + [p['G']] * 8 + [BD],
        [BD] + [p['G']] * 8 + [BD],
        [BS] * 10,
    ]


def sheet(p):
    px = [[None] * 32 for _ in range(32)]

    def blit(rows, ox, oy):
        for y, row in enumerate(rows):
            for x, c in enumerate(row):
                px[oy + y][ox + x] = c

    blit(front(p), 0, 0)
    blit(side(p), 10, 0)
    blit(top(p), 14, 0)
    blit(bottom(p), 14, 4)
    return px


if __name__ == '__main__':
    for kind, palette in PALETTES.items():
        path = os.path.join(OUT, f'card_deck_{kind}.png')
        write_png(path, sheet(palette))
        print('wrote', os.path.normpath(path))
