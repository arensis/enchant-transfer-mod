#!/usr/bin/env python3
"""Renders 3D models to transparent PNGs for the docs.

Reads real block/item model JSONs (elements, faces, UVs, textures, parent
chain) and their textures — the mod's from src/main/resources, vanilla ones
from the Minecraft client jar Loom downloads — projects them orthographically
and writes antialiased PNGs with transparency to docs/images/.

Static images instead of live CSS 3D keep the website fast on phones.
Block-entity-rendered parts (Transfer Table core glow, Infusion Coil fluid,
Zinc Smelter fire) are not in the models, so they don't appear.

Usage:  python scripts/render_model_icons.py
        (set MINECRAFT_JAR to use a client jar other than Loom's cache)
"""
import glob
import json
import math
import os
import zipfile

from pngio import read_png, write_png

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
ASSETS = os.path.join(ROOT, 'src', 'main', 'resources', 'assets')
OUT = os.path.join(ROOT, 'docs', 'images')

SUPERSAMPLE = 3     # antialiasing
PADDING = 2         # transparent border, in output pixels

# The deck design proposal's view (CSS rotateX(-22deg) rotateY(-35deg))
PROPOSAL_VIEW = dict(azimuth=35, elevation=22)
# Vanilla inventory view: display "gui" rotation [30, 225, 0]
INVENTORY_VIEW = dict(azimuth=225, elevation=30)

# file, model, view, px per model unit, square canvas (None = crop to content)
TARGETS = [
    ('render_transfer_table.png', 'enchanttransfer:block/transfer_table', PROPOSAL_VIEW, 8, None),
    ('render_zinc_smelter.png', 'enchanttransfer:block/zinc_smelter_lit', PROPOSAL_VIEW, 8, None),
    ('render_infusion_coil.png', 'enchanttransfer:block/infusor_coil', PROPOSAL_VIEW, 8, None),
] + [
    (f'card_deck_{kind}.png', f'enchanttransfer:item/card_deck_{kind}', PROPOSAL_VIEW, 8, None)
    for kind in ('blue', 'green', 'red', 'yellow', 'purple', 'rare')
] + [
    # Vanilla recipe ingredients, as they look in the inventory (128×128 like the other icons)
    ('blast_furnace.png', 'minecraft:block/blast_furnace', INVENTORY_VIEW, 5, 128),
    ('glass_block.png', 'minecraft:block/glass', INVENTORY_VIEW, 5, 128),
]

# Minecraft-like directional shading
SHADE = {'up': 1.0, 'down': 0.5, 'north': 0.86, 'south': 0.86, 'east': 0.7, 'west': 0.7}
NORMAL = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0),
          'west': (-1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}


# ── Resources (mod assets first, then the vanilla client jar) ───────────────

def _vanilla_jar():
    jar = os.environ.get('MINECRAFT_JAR')
    if jar:
        return jar
    props = open(os.path.join(ROOT, 'gradle.properties'), encoding='utf-8').read()
    version = next(line.split('=', 1)[1].strip() for line in props.splitlines()
                   if line.replace(' ', '').startswith('minecraft_version='))
    found = glob.glob(os.path.expanduser(f'~/.gradle/caches/fabric-loom/{version}/minecraft-client.jar'))
    return found[0] if found else None


_JAR_PATH = _vanilla_jar()
_jar = None


def read_resource(ref, kind, ext):
    namespace, path = ref.split(':', 1) if ':' in ref else ('minecraft', ref)
    relative = f'{namespace}/{kind}/{path}{ext}'
    on_disk = os.path.join(ASSETS, *relative.split('/'))
    if os.path.exists(on_disk):
        return open(on_disk, 'rb').read()
    global _jar
    if _jar is None:
        if not _JAR_PATH:
            raise FileNotFoundError(f'{relative}: no Minecraft jar (run a Gradle build or set MINECRAFT_JAR)')
        _jar = zipfile.ZipFile(_JAR_PATH)
    return _jar.read('assets/' + relative)


def load_model(ref):
    """Model with its parent chain merged: (elements, textures)."""
    model = json.loads(read_resource(ref, 'models', '.json'))
    elements, textures = model.get('elements'), dict(model.get('textures', {}))
    if 'parent' in model and not model['parent'].endswith('builtin/generated'):
        parent_elements, parent_textures = load_model(model['parent'])
        elements = elements or parent_elements
        textures = {**parent_textures, **textures}
    return elements or [], textures


def resolve_texture(name, textures, cache):
    while isinstance(name, dict) or name.startswith('#'):
        # 26.x texture entries may be {"sprite": "...", "force_translucent": ...}
        name = name['sprite'] if isinstance(name, dict) else textures[name[1:]]
    if name not in cache:
        width, height, px = read_png(read_resource(name, 'textures', '.png'))
        cache[name] = (width, min(height, width), px)   # first frame of animated strips
    return cache[name]


# ── Geometry ────────────────────────────────────────────────────────────────

def face_corners(direction, f, t):
    """(origin, u-end, v-end) of a face, following Minecraft's UV orientation."""
    x1, y1, z1 = f
    x2, y2, z2 = t
    return {
        'north': ((x2, y2, z1), (x1, y2, z1), (x2, y1, z1)),
        'south': ((x1, y2, z2), (x2, y2, z2), (x1, y1, z2)),
        'east':  ((x2, y2, z2), (x2, y2, z1), (x2, y1, z2)),
        'west':  ((x1, y2, z1), (x1, y2, z2), (x1, y1, z1)),
        'up':    ((x1, y2, z1), (x2, y2, z1), (x1, y2, z2)),
        'down':  ((x1, y1, z2), (x2, y1, z2), (x1, y1, z1)),
    }[direction]


def default_uv(direction, f, t):
    """UVs Minecraft derives from the element's bounds when a face has none."""
    x1, y1, z1 = f
    x2, y2, z2 = t
    return {
        'north': (16 - x2, 16 - y2, 16 - x1, 16 - y1),
        'south': (x1, 16 - y2, x2, 16 - y1),
        'east':  (16 - z2, 16 - y2, 16 - z1, 16 - y1),
        'west':  (z1, 16 - y2, z2, 16 - y1),
        'up':    (x1, z1, x2, z2),
        'down':  (x1, 16 - z2, x2, 16 - z1),
    }[direction]


def camera(azimuth, elevation):
    az, el = math.radians(azimuth), math.radians(elevation)
    to_camera = (math.sin(az) * math.cos(el), math.sin(el), math.cos(az) * math.cos(el))
    right = (math.cos(az), 0.0, -math.sin(az))                       # up × to_camera
    up = (to_camera[1] * right[2] - to_camera[2] * right[1],
          to_camera[2] * right[0] - to_camera[0] * right[2],
          to_camera[0] * right[1] - to_camera[1] * right[0])
    return to_camera, right, up


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


# ── Rendering ───────────────────────────────────────────────────────────────

def render(model_ref, view, unit, canvas):
    elements, textures = load_model(model_ref)
    to_camera, right, up = camera(view['azimuth'], view['elevation'])
    scale = unit * SUPERSAMPLE
    cache = {}

    def project(p):
        q = (p[0] - 8, p[1] - 8, p[2] - 8)
        return dot(q, right) * scale, -dot(q, up) * scale, dot(q, to_camera)

    faces = []
    for element in elements:
        for direction, face in element.get('faces', {}).items():
            if dot(NORMAL[direction], to_camera) <= 0:
                continue
            corners = [project(p) for p in face_corners(direction, element['from'], element['to'])]
            uv = face.get('uv') or default_uv(direction, element['from'], element['to'])
            texture = resolve_texture(face['texture'], textures, cache)
            faces.append((corners, uv, texture, SHADE[direction]))

    xs = [c[0] for f in faces for c in f[0]] + [f[0][1][0] + f[0][2][0] - f[0][0][0] for f in faces]
    ys = [c[1] for f in faces for c in f[0]] + [f[0][1][1] + f[0][2][1] - f[0][0][1] for f in faces]
    if canvas:
        size = canvas * SUPERSAMPLE
        cx, cy = (min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2
        ox, oy = round(cx - size / 2), round(cy - size / 2)
        width = height = size
    else:
        pad = PADDING * SUPERSAMPLE
        ox, oy = math.floor(min(xs)) - pad, math.floor(min(ys)) - pad
        width = math.ceil(max(xs)) - ox + pad
        height = math.ceil(max(ys)) - oy + pad
    width -= width % SUPERSAMPLE
    height -= height % SUPERSAMPLE
    color = [[None] * width for _ in range(height)]
    depth = [[-1e9] * width for _ in range(height)]
    translucent = []

    for corners, uv, (tw, th, tex), shade in faces:
        (x0, y0, z0), (xu, yu, zu), (xv, yv, zv) = corners
        ax, ay, bx, by = xu - x0, yu - y0, xv - x0, yv - y0
        det = ax * by - ay * bx
        if abs(det) < 1e-9:
            continue
        u1, v1, u2, v2 = uv
        xs4 = [x0, xu, xv, xu + xv - x0]
        ys4 = [y0, yu, yv, yu + yv - y0]
        for py in range(max(0, math.floor(min(ys4)) - oy), min(height, math.ceil(max(ys4)) - oy + 1)):
            for px in range(max(0, math.floor(min(xs4)) - ox), min(width, math.ceil(max(xs4)) - ox + 1)):
                dx, dy = px + ox + 0.5 - x0, py + oy + 0.5 - y0
                a = (dx * by - dy * bx) / det
                b = (ax * dy - ay * dx) / det
                if not (0 <= a < 1 and 0 <= b < 1):
                    continue
                tx = min(tw - 1, max(0, int((u1 + a * (u2 - u1)) * tw / 16)))
                ty = min(th - 1, max(0, int((v1 + b * (v2 - v1)) * th / 16)))
                r, g, bl, alpha = tex[ty][tx]
                if alpha == 0:
                    continue
                z = z0 + a * (zu - z0) + b * (zv - z0)
                rgb = (r * shade, g * shade, bl * shade)
                if alpha < 255:
                    translucent.append((z, px, py, rgb, alpha / 255))
                elif z > depth[py][px]:
                    depth[py][px] = z
                    color[py][px] = (*rgb, 1.0)

    # Translucent texels (glass) blended back to front over what's behind them
    for z, px, py, rgb, alpha in sorted(translucent):
        if z <= depth[py][px]:
            continue
        below = color[py][px] or (0.0, 0.0, 0.0, 0.0)
        out_a = alpha + below[3] * (1 - alpha)
        mixed = tuple((rgb[i] * alpha + below[i] * below[3] * (1 - alpha)) / out_a for i in range(3))
        color[py][px] = (*mixed, out_a)

    return downsample(color, width, height)


def downsample(color, width, height):
    out = []
    for y in range(0, height, SUPERSAMPLE):
        row = []
        for x in range(0, width, SUPERSAMPLE):
            r = g = b = a = 0.0
            for yy in range(y, y + SUPERSAMPLE):
                for xx in range(x, x + SUPERSAMPLE):
                    c = color[yy][xx]
                    if c:
                        r, g, b, a = r + c[0] * c[3], g + c[1] * c[3], b + c[2] * c[3], a + c[3]
            n = SUPERSAMPLE * SUPERSAMPLE
            if a <= 0:
                row.append(None)
            else:
                row.append((round(r / a), round(g / a), round(b / a), round(255 * a / n)))
        out.append(row)
    return out


if __name__ == '__main__':
    for filename, model, view, unit, canvas in TARGETS:
        path = os.path.join(OUT, filename)
        write_png(path, render(model, view, unit, canvas))
        print('wrote', os.path.normpath(os.path.relpath(path, ROOT)))
