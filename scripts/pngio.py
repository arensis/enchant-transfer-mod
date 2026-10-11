"""Minimal PNG reading/writing (no dependencies), shared by the asset scripts.

Pixels are rows of (r, g, b, a) tuples, or None for fully transparent.
"""
import struct
import zlib


def read_png(source):
    """Returns (width, height, rows of (r, g, b, a)).  `source` is a path or the file's bytes."""
    data = source if isinstance(source, bytes) else open(source, 'rb').read()
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError('not a PNG')
    pos, idat, palette, trns = 8, b'', None, None
    while pos < len(data):
        length, = struct.unpack('>I', data[pos:pos + 4])
        tag, chunk = data[pos + 4:pos + 8], data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if tag == b'IHDR':
            width, height, depth, color_type, _, _, interlace = struct.unpack('>IIBBBBB', chunk)
        elif tag == b'PLTE':
            palette = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif tag == b'tRNS':
            trns = list(chunk)
        elif tag == b'IDAT':
            idat += chunk
    if interlace:
        raise ValueError('interlaced PNG not supported')

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    bpp = max(1, channels * depth // 8)
    stride = (width * channels * depth + 7) // 8
    raw = zlib.decompress(idat)
    lines, prev, p = [], bytearray(stride), 0
    for _ in range(height):
        kind, line = raw[p], bytearray(raw[p + 1:p + 1 + stride])
        p += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if kind == 1:
                line[x] = (line[x] + a) & 255
            elif kind == 2:
                line[x] = (line[x] + b) & 255
            elif kind == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif kind == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        lines.append(bytes(line))
        prev = line

    def pixel(row, x):
        if color_type == 6:
            return tuple(row[x * 4:x * 4 + 4])
        if color_type == 2:
            return tuple(row[x * 3:x * 3 + 3]) + (255,)
        if color_type == 4:
            return (row[x * 2],) * 3 + (row[x * 2 + 1],)
        if color_type == 0:
            return (row[x],) * 3 + (255,)
        per = 8 // depth
        index = row[x] if depth == 8 else (row[x // per] >> (8 - depth * (x % per + 1))) & ((1 << depth) - 1)
        alpha = trns[index] if trns and index < len(trns) else 255
        return palette[index] + (alpha,)

    return width, height, [[pixel(row, x) for x in range(width)] for row in lines]


def write_png(path, px):
    """Writes RGBA rows; each pixel is a hex 'rrggbb' string, an (r, g, b, a) tuple, or None."""
    height, width = len(px), len(px[0])
    raw = bytearray()
    for row in px:
        raw.append(0)
        for c in row:
            if c is None:
                raw += b'\x00\x00\x00\x00'
            elif isinstance(c, str):
                raw += bytes.fromhex(c) + b'\xff'
            else:
                raw += bytes(c)

    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)

    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(raw), 9)) + chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(png)
