import math, sys
from PIL import Image

N = 64
LX, LY = 0.55, 0.83


def sh(c, a):
    r, g, b = c
    if a >= 0:
        return (int(r + (255 - r) * a), int(g + (255 - g) * a), int(b + (255 - b) * a))
    k = 1 + a
    return (int(r * k), int(g * k), min(255, int(b * (k + .07))))


def ramp(c):
    return [sh(c, .34), sh(c, .16), c, sh(c, -.2), sh(c, -.38)]


def tone(l):
    return 0 if l > .55 else 1 if l > .15 else 2 if l > -.3 else 3 if l > -.65 else 4


ROBE = ramp((0x3B, 0x62, 0xD6))
MANT = ramp((0x4A, 0x74, 0xE6))
CAPE = ramp((0x2C, 0x46, 0xAA))
HAT = ramp((0x36, 0x4F, 0xC6))
GOLD = ramp((0xF2, 0xC9, 0x4C))
SKIN = ramp((0xF2, 0xC2, 0x9B))
BEARD = [(255, 255, 255), (0xF1, 0xF3, 0xF8), (0xDB, 0xE0, 0xEC), (0xB4, 0xBC, 0xD2), (0x8E, 0x98, 0xB6)]
LEA = ramp((0x7A, 0x4A, 0x24))
WOOD = ramp((0x8B, 0x5E, 0x3C))
CRY = ramp((0xFF, 0x9A, 0x3D))
RED = ramp((0xD8, 0x3A, 0x4A))
DARK = (0x2A, 0x1A, 0x10)
BLUSH = (0xE8, 0x9A, 0x88)
WHITE = (255, 255, 255)


class Buf:
    def __init__(s):
        s.p = [[None] * N for _ in range(N)]

    def get(s, x, y):
        return s.p[y][x] if 0 <= x < N and 0 <= y < N else None

    def set(s, x, y, c):
        if 0 <= x < N and 0 <= y < N:
            s.p[y][x] = c

    def rect(s, x, y, w, h, c):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                s.set(xx, yy, c)

    def blit(s, o, ox=0, oy=0):
        for y in range(N):
            for x in range(N):
                c = o.p[y][x]
                if c is not None:
                    s.set(x + ox, y + oy, c)


def ell(b, cx, cy, rx, ry, rp, flat=False):
    for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
        for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
            nx = (x + .5 - cx) / rx
            ny = (y + .5 - cy) / ry
            if nx * nx + ny * ny > 1:
                continue
            b.set(x, y, rp[2] if flat else rp[tone(-(nx * LX + ny * LY))])


def inpoly(pts, x, y):
    ins = False
    n = len(pts)
    for i in range(n):
        x1, y1 = pts[i]
        x2, y2 = pts[(i + 1) % n]
        if (y1 <= y < y2) or (y2 <= y < y1):
            if x < x1 + (y - y1) * (x2 - x1) / (y2 - y1):
                ins = not ins
    return ins


def pmask(pts):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    m = set()
    for y in range(max(0, int(min(ys)) - 1), min(N, int(max(ys)) + 2)):
        for x in range(max(0, int(min(xs)) - 1), min(N, int(max(xs)) + 2)):
            if inpoly(pts, x + .5, y + .5):
                m.add((x, y))
    return m


def paint_frac(b, m, rp, fn):
    """pinta mascara; fn(f,x,y)-> indice de tom (f = fracao horizontal na linha)."""
    rows = {}
    for (x, y) in m:
        lo, hi = rows.get(y, (99, -1))
        rows[y] = (min(lo, x), max(hi, x))
    for (x, y) in m:
        lo, hi = rows[y]
        f = (x - lo) / max(1, hi - lo)
        t = fn(f, x, y)
        b.set(x, y, rp[t] if isinstance(t, int) else t)
    return rows


def bez(P, t):
    u = 1 - t
    return (u ** 3 * P[0][0] + 3 * u * u * t * P[1][0] + 3 * u * t * t * P[2][0] + t ** 3 * P[3][0],
            u ** 3 * P[0][1] + 3 * u * u * t * P[1][1] + 3 * u * t * t * P[2][1] + t ** 3 * P[3][1])


def curve(P, r0, r1, n=56, pw=1.0):
    out = []
    for i in range(n + 1):
        t = i / n
        x, y = bez(P, t)
        out.append((x, y, r1 + (r0 - r1) * (1 - t) ** pw, t))
    return out


def tube(b, s, rp, bands=(), lo=0.0, hi=1.0, flat=False):
    S = [q for q in s if lo <= q[3] <= hi]
    x0 = int(min(q[0] - q[2] for q in S)) - 1
    x1 = int(max(q[0] + q[2] for q in S)) + 2
    y0 = int(min(q[1] - q[2] for q in S)) - 1
    y1 = int(max(q[1] + q[2] for q in S)) + 2
    for y in range(max(0, y0), min(N, y1)):
        for x in range(max(0, x0), min(N, x1)):
            best = None
            for (cx, cy, r, t) in S:
                d = math.hypot(x + .5 - cx, y + .5 - cy) / r
                if d <= 1 and (best is None or d < best[0]):
                    best = (d, cx, cy, r, t)
            if best:
                d, cx, cy, r, t = best
                l = -(((x + .5 - cx) / r) * LX + ((y + .5 - cy) / r) * LY)
                rr = rp
                for (t0, t1, bp) in bands:
                    if t0 <= t <= t1:
                        rr = bp
                b.set(x, y, rr[2] if flat else rr[tone(l)])


def hline_noise(b, y, x0, x1, c, step=4, off=0):
    for x in range(x0, x1):
        if (x + off) % step == 0:
            b.set(x, y, c)


# ------------------------------------------------------------------ pecas
def staff(b, x, top, bot, cy):
    """cajado: x = pixel esquerdo (3 de largura)."""
    for y in range(top, bot):
        b.set(x, y, WOOD[1])
        b.set(x + 1, y, WOOD[2])
        b.set(x + 2, y, WOOD[3])
        if (y - top) % 7 == 3:
            b.set(x + 2, y, WOOD[4])
            b.set(x, y, WOOD[2])
        if (y - top) % 11 == 5:
            b.set(x + 1, y, WOOD[3])
    b.set(x, top + 1, WOOD[0])
    # anel de ouro + garras
    b.rect(x - 1, top, 5, 2, GOLD[2])
    b.rect(x - 1, top, 5, 1, GOLD[0])
    b.rect(x - 1, top + 1, 5, 1, GOLD[3])
    b.rect(x - 1, top + 12, 5, 1, GOLD[2])
    b.rect(x - 1, top + 13, 5, 1, GOLD[3])
    cx = x + 1.5
    # garras (3 pontas)
    for dx, h in ((-2.6, 5), (2.6, 5), (0, 3)):
        for k in range(h):
            b.set(int(cx + dx * (1 - k / 7.0)), top - k, GOLD[2] if dx >= 0 else GOLD[1])
    # cristal facetado
    pts = [(cx, cy - 6.2), (cx + 4.2, cy - .5), (cx, cy + 5), (cx - 4.2, cy - .5)]
    m = pmask(pts)
    for (px, py) in m:
        l = -(((px + .5 - cx) / 4.2) * LX + ((py + .5 - cy) / 5.5) * LY)
        b.set(px, py, CRY[tone(l)])
    for (px, py) in m:
        if abs(px + .5 - cx) < 1.2 and abs(py + .5 - (cy - .8)) < 2.6:
            b.set(px, py, (0xFF, 0xE0, 0xA0))
    b.set(int(cx), int(cy - 1), WHITE)
    b.set(int(cx) - 2, int(cy - 4), WHITE)


def boot(b, x, yt, yb, ln=5, dark=0.0):
    rp = [sh(c, -dark) if dark else c for c in LEA]
    b.rect(x, yt, 7, yb - yt - 1, rp[2])
    b.rect(x, yt, 2, yb - yt - 1, rp[1])
    b.rect(x + 5, yt, 2, yb - yt - 1, rp[3])
    b.rect(x - 1, yt, 9, 2, rp[1])
    b.rect(x - 1, yt + 1, 9, 1, rp[3])
    ell(b, x + 3.5 + ln * .3, yb - 2.2, 4.8 + ln * .4, 2.7, rp)
    b.rect(x - 1, yb - 1, 8 + ln, 1, rp[4])
    b.set(x + 2, yt + 4, GOLD[2])


def robe_rows(cx, y0, y1, w0, w1, ex=1.15):
    rows = {}
    for y in range(y0, y1 + 1):
        t = (y - y0) / (y1 - y0)
        hw = w0 + (w1 - w0) * (t ** ex)
        rows[y] = (cx - hw, cx + hw)
    return rows


def hem_y(x, base, ph):
    return base + int(round(1.3 * max(0, math.sin((x + ph) * .62))))


def robe_body(b, rows, base, ph, rp, folds, foldlean=0.0, hemgold=True, tr=(0.18, 0.55, 0.82)):
    m = set()
    for y, (xl, xr) in rows.items():
        for x in range(int(xl), int(xr) + 1):
            if y <= hem_y(x, base, ph):
                m.add((x, y))

    def fn(f, x, y):
        if hemgold and y >= hem_y(x, base, ph) - 3:
            yy = hem_y(x, base, ph) - y
            c = GOLD[0] if yy == 3 else GOLD[2] if yy >= 1 else GOLD[3]
            if yy == 2 and (x + int(ph)) % 4 == 0:
                c = GOLD[4]
            return c
        t = 0 if f < tr[0] * .4 else 1 if f < tr[0] else 2 if f < tr[1] else 3 if f < tr[2] else 4
        if y > 38:
            for fp in folds:
                fx = fp + foldlean * (y - 38)
                if abs(f - fx) < 0.025:
                    t = min(4, t + 2)
                elif -0.07 < f - fx < -0.025:
                    t = max(0, t - 1)
        return t
    return paint_frac(b, m, rp, fn)


def beard(b, pts, rp, lines):
    m = pmask(pts)

    def fn(f, x, y):
        t = 0 if f < .1 else 1 if f < .35 else 2 if f < .62 else 3 if f < .88 else 4
        for (lx, ly0, ly1) in lines:
            if x == lx and ly0 <= y <= ly1:
                t = min(4, t + 1)
        return t
    paint_frac(b, m, rp, fn)


def star(b, cx, cy, c=GOLD):
    for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
        b.set(cx + dx, cy + dy, c[2])
    b.set(cx, cy, (0xFF, 0xF6, 0xC8))
    b.set(cx, cy - 2, c[2])
    b.set(cx, cy + 2, c[3])
    b.set(cx - 2, cy, c[2])
    b.set(cx + 2, cy, c[3])


def hat(b, cx, cy_brim, P, rbase, tipr=0.9, brimrx=17, brimry=4.4, starpos=None, ph=0):
    # cone (so a parte acima da aba) desenhado antes da aba
    tmp = Buf()
    s = curve(P, rbase, tipr, 64, 0.9)
    tube(tmp, s, HAT)
    for y in range(N):
        for x in range(N):
            if tmp.p[y][x] is not None and y <= cy_brim - 1:
                b.p[y][x] = tmp.p[y][x]
    # sombra e aba
    ell(b, cx, cy_brim + 1.4, brimrx, brimry, [sh(HAT[4], -.15)] * 5, flat=True)
    ell(b, cx, cy_brim, brimrx, brimry, HAT)
    # faixa de ouro (crescente) na base do cone
    cyb = cy_brim - 1.6
    rxb = rbase + .6
    for y in range(int(cyb - 6), int(cyb + 8)):
        for x in range(int(cx - rxb - 2), int(cx + rxb + 3)):
            n1 = ((x + .5 - cx) / rxb) ** 2 + ((y + .5 - cyb) / 3.4) ** 2
            n2 = ((x + .5 - cx) / rxb) ** 2 + ((y + .5 - (cyb - 2.6)) / 3.4) ** 2
            if n1 <= 1 and n2 > 1:
                t = 0 if n2 < 1.25 else 2 if n1 < .7 else 3
                b.set(x, y, GOLD[t])
    # fivela
    b.rect(int(cx) - 2, int(cy_brim) - 1, 4, 3, GOLD[1])
    b.rect(int(cx) - 1, int(cy_brim), 2, 1, GOLD[4])
    b.set(int(cx) - 2, int(cy_brim) - 1, GOLD[0])
    if starpos:
        star(b, *starpos)
        b.set(starpos[0] + 5, starpos[1] + 4, GOLD[0])
        b.set(starpos[0] - 4, starpos[1] + 5, GOLD[1])


# ------------------------------------------------------------------ FRENTE
def front(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    st = {0: 0, 1: 1, 2: 0, 3: -1}[f]
    ph = f * 1.3
    staff(out, 49, 14, 59, 6)
    # botas
    for cx, fw in ((24, st), (36, -st)):
        bb = 58 + (1 if fw == 1 else -3 if fw == -1 else 0)
        boot(out, cx, 48, bb, 4)
    # capa de baixo / tunica
    rows = robe_rows(32, 29, 54, 9.5, 17.5)
    robe_body(body, rows, 53, ph, ROBE, (0.30, 0.50, 0.72), 0.0)
    # faixa central dourada
    for y in range(44, 53):
        body.set(31, y, GOLD[2]); body.set(32, y, GOLD[3])
    for yy in (46, 51):
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            body.set(31 + dx, yy + dy, GOLD[1] if dx <= 0 else GOLD[3])
        body.set(31, yy, GOLD[0])
        body.set(32, yy, GOLD[2])
    # manto sobre os ombros
    ell(body, 32, 31, 13.5, 4.6, MANT)
    for x in range(19, 46):
        nx = (x + .5 - 32) / 13.5
        if abs(nx) < 1:
            yy = int(31 + 4.6 * math.sqrt(1 - nx * nx))
            body.set(x, yy, GOLD[2]); body.set(x, yy - 1, GOLD[3] if x % 3 == 0 else GOLD[2])
    # cinto
    for y in range(39, 43):
        xl, xr = rows[y]
        for x in range(int(xl) + 1, int(xr)):
            body.set(x, y, LEA[1] if y == 39 else LEA[2] if y < 42 else LEA[4])
    body.rect(29, 38, 6, 6, GOLD[2]); body.rect(29, 38, 6, 1, GOLD[0]); body.rect(29, 43, 6, 1, GOLD[4])
    body.rect(31, 40, 2, 2, LEA[4]); body.set(30, 39, GOLD[0])
    # bolsa (esquerda de quem ve)
    body.rect(21, 42, 8, 8, LEA[2]); body.rect(21, 42, 2, 8, LEA[1]); body.rect(27, 42, 2, 8, LEA[3])
    body.rect(21, 42, 8, 3, LEA[3]); body.rect(21, 45, 8, 1, LEA[4]); body.rect(24, 45, 2, 2, GOLD[2])
    body.rect(21, 49, 8, 1, LEA[4])
    # pocao (direita)
    body.rect(38, 43, 3, 2, (0x7A, 0xA8, 0xD8)); body.rect(38, 42, 3, 1, LEA[2])
    ell(body, 39.5, 48, 3.3, 3.4, RED); body.set(38, 47, (0xFF, 0xC8, 0xC8))
    # braco pendurado (lado esquerdo de quem ve)
    hy = 47 + (-2 if st == 1 else 2 if st == -1 else 0)
    s = curve([(22, 31), (16, 35), (14.5, 41), (16.5, hy - 4)], 4.6, 5.6, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 17, hy, 3.1, 3.5, SKIN); body.set(16, hy + 2, SKIN[3]); body.set(18, hy + 1, SKIN[3])
    # braco que segura o cajado
    s = curve([(42, 31), (48, 32), (47.5, 38), (50.5, 41)], 4.6, 5.4, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 50.5, 42.5, 3.4, 3.1, SKIN); body.rect(48, 42, 5, 1, SKIN[3]); body.set(50, 41, SKIN[0])
    # cabelo atras
    ell(body, 25.5, 28, 3.6, 7.5, BEARD); ell(body, 38.5, 28, 3.6, 7.5, BEARD)
    # orelhas
    ell(body, 26.6, 25, 1.6, 2.2, SKIN); ell(body, 37.4, 25, 1.6, 2.2, SKIN)
    # rosto
    ell(body, 32, 24, 6.3, 6.9, SKIN)
    # sobrancelhas
    for x0 in (27, 33):
        body.rect(x0, 21, 5, 2, BEARD[1]); body.rect(x0, 22, 5, 1, BEARD[2]); body.set(x0 + (0 if x0 == 27 else 4), 20, BEARD[1])
    # olhos
    for ex in (29, 35):
        body.rect(ex, 24, 2, 2, DARK); body.set(ex, 24, (0x55, 0x66, 0x99)); body.set(ex + 1, 26, SKIN[3])
    # nariz e bochechas
    ell(body, 32, 27.3, 2.2, 2.3, [sh(SKIN[1], .02), SKIN[1], (0xEE, 0xA8, 0x8A), (0xD8, 0x8C, 0x72), SKIN[4]])
    body.set(31, 26, SKIN[0])
    body.rect(26, 27, 2, 1, BLUSH); body.rect(37, 27, 2, 1, BLUSH)
    # bigode e barba
    ell(body, 29.2, 29.4, 3.8, 1.9, BEARD); ell(body, 34.8, 29.4, 3.8, 1.9, BEARD)
    body.rect(31, 30, 2, 1, (0x9A, 0x50, 0x50))
    tip = 49 + (1 if f in (1, 3) else 0)
    bx = 1 if f == 1 else -1 if f == 3 else 0
    pts = [(26, 27), (38.5, 27), (39.5, 32), (38, 38), (35.5 + bx, 44), (32 + bx, tip), (28.5 + bx, 44), (26, 38), (24.5, 32)]
    beard(body, pts, BEARD, [(30, 34, 41), (33, 33, 43), (35, 36, 42), (28, 35, 40), (32 + bx, 42, 46)])
    for x in range(28, 37):
        if body.get(x, 31) is not None:
            body.set(x, 31, BEARD[3] if x in (31, 32) else body.get(x, 31))
    # chapeu
    hat(body, 32, 15, [(32, 12), (31, 5.5), (23, 3.5), (12.5, 10)], 9.4, 0.9, 17.5, 4.4, (30, 8), ph)
    out.blit(body, 0, bob)
    return out, (50.5, 6)


# ------------------------------------------------------------------ COSTAS
def back(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    st = {0: 0, 1: 1, 2: 0, 3: -1}[f]
    ph = f * 1.3
    staff(out, 12, 14, 59, 6)
    for cx, fw in ((24, st), (36, -st)):
        bb = 58 + (1 if fw == 1 else -3 if fw == -1 else 0)
        boot(out, cx, 48, bb, 3)
    # tunica e capa
    rows = robe_rows(32, 29, 54, 10.5, 18.5)
    robe_body(body, rows, 53, ph, CAPE, (0.5,), 0.0)
    for y in range(36, 53):
        body.set(32, y, CAPE[4]); body.set(31, y, CAPE[1] if y % 2 else CAPE[2])
    # cinto + laco atras
    for y in range(39, 43):
        xl, xr = rows[y]
        for x in range(int(xl) + 1, int(xr)):
            body.set(x, y, LEA[1] if y == 39 else LEA[2] if y < 42 else LEA[4])
    ell(body, 29, 44, 2.4, 3.6, LEA); ell(body, 35, 44.5, 2.4, 3.6, LEA)
    body.rect(30, 39, 4, 4, GOLD[2]); body.set(30, 39, GOLD[0])
    # manto
    ell(body, 32, 31, 14, 5, MANT)
    for x in range(18, 46):
        nx = (x + .5 - 32) / 14.0
        if abs(nx) < 1:
            yy = int(31 + 5 * math.sqrt(1 - nx * nx))
            body.set(x, yy, GOLD[2]); body.set(x, yy - 1, GOLD[3] if x % 3 == 0 else GOLD[2])
    # bracos
    hy = 47 + (-2 if st == -1 else 2 if st == 1 else 0)
    s = curve([(42, 31), (48.5, 35), (48.5, 41), (47, hy - 4)], 4.6, 5.6, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 47, hy, 3.1, 3.5, SKIN)
    s = curve([(22, 31), (15.5, 33), (14, 36), (13, 40)], 4.6, 5.0, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 12.5, 42.5, 3.3, 3.1, SKIN); body.rect(10, 42, 5, 1, SKIN[3])
    # cabelo comprido
    pts = [(24.5, 20), (39.5, 20), (41.5, 29), (40, 38), (36.5, 43), (32, 40.5), (27.5, 43), (24, 38), (22.5, 29)]
    m = pmask(pts)

    def fn(fr, x, y):
        t = 0 if fr < .1 else 1 if fr < .35 else 2 if fr < .62 else 3 if fr < .88 else 4
        if (x in (28, 31, 34, 37)) and y > 28 and (y + x) % 9 != 0:
            t = min(4, t + 1)
        return t
    paint_frac(body, m, BEARD, fn)
    body.rect(24, 30, 16, 1, BEARD[4])  # sombra do manto na nuca
    # chapeu (ponta cai para a direita de quem ve)
    hat(body, 32, 15, [(32, 12), (33, 5.5), (41, 3.5), (51.5, 10)], 9.4, 0.9, 17.5, 4.4, None, ph)
    out.blit(body, 0, bob)
    return out, (13.5, 6)


# ------------------------------------------------------------------ LADO (direita)
def side(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    ph = f * 1.3
    sw = {0: 0, 1: -2, 2: 0, 3: 2}[f]
    staff(out, 46, 14, 59, 6)
    # botas: (x, yt, yb) longe e perto
    if f == 1:
        far = (20, 49, 56); near = (35, 48, 59)
    elif f == 3:
        far = (35, 48, 59); near = (21, 49, 56)
    else:
        far = (30, 48, 58); near = (26, 48, 58)
    boot(out, far[0], far[1], far[2], 6, dark=0.18)
    boot(out, near[0], near[1], near[2], 6)
    # braco de tras
    s = curve([(29, 32), (27, 37), (23 + sw * .5, 42), (22 + sw, 46 + (0 if f in (0, 2) else -1))], 4.2, 5.0, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 22 + sw, 48.5, 2.9, 3.3, SKIN)
    # capa balancando atras
    capepts = [(26, 30), (27, 35), (24, 52), (12 + sw * 1.0, 54), (14 + sw, 46), (21, 35)]
    mc = pmask(capepts)
    paint_frac(body, mc, CAPE, lambda fr, x, y: (4 if fr < .08 else 3 if fr < .45 else 2 if fr < .8 else 1) if y < 50 else GOLD[2] if y < 54 else CAPE[4])
    # tunica
    rows = {}
    for y in range(29, 55):
        t = (y - 29) / 25.0
        rows[y] = (25 - 7.5 * (t ** 1.2) + (-1 if f in (1, 3) else 0), 38 + 5.5 * (t ** 1.3))
    robe_body(body, rows, 53, ph, ROBE, (0.35, 0.62), 0.04)
    # cinto
    for y in range(39, 43):
        xl, xr = rows[y]
        for x in range(int(xl) + 1, int(xr) + 1):
            body.set(x, y, LEA[1] if y == 39 else LEA[2] if y < 42 else LEA[4])
    body.rect(36, 38, 4, 6, GOLD[2]); body.rect(36, 38, 4, 1, GOLD[0]); body.rect(36, 43, 4, 1, GOLD[4]); body.rect(37, 40, 2, 2, LEA[4])
    body.rect(24, 42, 8, 8, LEA[2]); body.rect(24, 42, 2, 8, LEA[1]); body.rect(30, 42, 2, 8, LEA[3])
    body.rect(24, 42, 8, 3, LEA[3]); body.rect(24, 45, 8, 1, LEA[4]); body.rect(27, 45, 2, 2, GOLD[2]); body.rect(24, 49, 8, 1, LEA[4])
    # manto
    ell(body, 31.5, 31, 8.5, 4.6, MANT)
    for x in range(23, 41):
        nx = (x + .5 - 31.5) / 8.5
        if abs(nx) < 1:
            yy = int(31 + 4.6 * math.sqrt(1 - nx * nx))
            body.set(x, yy, GOLD[2]); body.set(x, yy - 1, GOLD[3] if x % 3 == 0 else GOLD[2])
    # braco da frente segura o cajado
    s = curve([(30, 33), (33, 41), (40, 45), (46.5, 43.5)], 4.6, 5.3, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.76, 0.95, GOLD)])
    ell(body, 47.5, 43.5, 3.4, 3.1, SKIN); body.rect(45, 43, 5, 1, SKIN[3]); body.set(47, 42, SKIN[0])
    # cabelo atras da cabeca
    ell(body, 29.5, 27, 6, 8.5, BEARD)
    for yy in range(22, 35):
        body.set(25 + (yy % 3), yy, BEARD[3]) if yy % 2 else None
    # rosto de perfil
    ell(body, 35, 24.5, 5.8, 6.7, SKIN)
    ell(body, 41.2, 27, 2.6, 2.4, [SKIN[1], SKIN[1], (0xEE, 0xA8, 0x8A), (0xD8, 0x8C, 0x72), SKIN[4]])
    body.set(40, 26, SKIN[0])
    ell(body, 31.2, 25.5, 1.5, 2.3, SKIN)
    body.rect(36, 24, 2, 2, DARK); body.set(36, 24, (0x55, 0x66, 0x99))
    body.rect(35, 21, 6, 2, BEARD[1]); body.rect(35, 22, 6, 1, BEARD[2]); body.set(41, 21, BEARD[1])
    body.rect(37, 28, 2, 1, BLUSH)
    ell(body, 39.5, 30, 3.8, 2.0, BEARD)
    bx = 1 if f == 1 else -1 if f == 3 else 0
    pts = [(31, 28), (40, 30.5), (43, 34), (42, 40), (39 + bx, 46), (35 + bx, 50), (32, 44), (30, 36)]
    beard(body, pts, BEARD, [(36, 36, 44), (39, 35, 42), (34 + bx, 44, 48), (33, 33, 40)])
    # chapeu: ponta cai para tras
    hat(body, 33.5, 15.5, [(33.5, 12.5), (34, 5.5), (24, 3.5), (12, 10.5)], 9.0, 0.9, 15, 4.0, (31, 8), ph)
    out.blit(body, 0, bob)
    return out, (47.5, 6)


# ------------------------------------------------------------------ saida
def outline(b):
    add = {}
    for y in range(N):
        for x in range(N):
            if b.p[y][x] is not None:
                continue
            ns = [b.get(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            ns = [c for c in ns if c is not None]
            if ns:
                r = sum(c[0] for c in ns) // len(ns)
                g = sum(c[1] for c in ns) // len(ns)
                bl = sum(c[2] for c in ns) // len(ns)
                d = sh((r, g, bl), -.72)
                add[(x, y)] = (int(d[0] * .65 + 0x14 * .35), int(d[1] * .65 + 0x18 * .35), int(d[2] * .65 + 0x3A * .35))
    for (x, y), c in add.items():
        b.p[y][x] = c


def build():
    frames = []
    orbs = []
    for fn in (back, side, front):
        for f in range(4):
            b, o = fn(f)
            outline(b)
            frames.append(b)
            orbs.append(o)
    return frames, orbs


def sheet(frames, path, sc=4):
    im = Image.new('RGBA', (N * sc * 4, N * sc * 3), (70, 110, 70, 255))
    for i, b in enumerate(frames):
        for y in range(N):
            for x in range(N):
                c = b.p[y][x]
                if c:
                    for yy in range(sc):
                        for xx in range(sc):
                            im.putpixel(((i % 4) * N * sc + x * sc + xx, (i // 4) * N * sc + y * sc + yy), c + (255,))
    im.save(path)


CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#%&()*+,-/:;<=>?@[]^_{|}~"


def export(frames, orbs, path):
    pal = {}
    for b in frames:
        for row in b.p:
            for c in row:
                if c is not None and c not in pal:
                    pal[c] = len(pal) + 1
    B = len(CHARS)
    assert len(pal) + 1 < B * B
    code = lambda i: CHARS[i // B] + CHARS[i % B]
    pals = ''.join('%02X%02X%02X' % c for c, _ in sorted(pal.items(), key=lambda kv: kv[1]))
    strs = []
    for b in frames:
        flat = [0 if c is None else pal[c] for row in b.p for c in row]
        s = ''
        i = 0
        while i < len(flat):
            j = i
            while j < len(flat) and flat[j] == flat[i]:
                j += 1
            n = j - i
            s += code(flat[i]) + (str(n) if n > 1 else '')
            i = j
        strs.append(s)
    with open(path, 'w') as fh:
        fh.write('package com.aldoria.rpg\n\n')
        fh.write('/** Dados da sprite do mago (64 x 64), gerados por tools/gen_mage.py. Não editar à mão. */\n')
        fh.write('object MageData {\n')
        fh.write('    /** alfabeto dos códigos (2 letras por cor; "AA" = transparente) */\n')
        fh.write('    const val CH = "%s"\n' % CHARS)
        fh.write('    const val HEX = "%s"\n' % pals)
        fh.write('    /** posição do cristal no cajado [direção 0 costas, 1 direita, 2 frente][quadro][x,y] em pixels de 64 */\n')
        fh.write('    val ORB = floatArrayOf(%s)\n' % ', '.join('%.1ff' % v for o in orbs for v in o))
        fh.write('    val F = arrayOf(\n')
        for i, s in enumerate(strs):
            fh.write('        "%s"%s\n' % (s, ',' if i < len(strs) - 1 else ''))
        fh.write('    )\n}\n')
    return sum(len(s) for s in strs), len(pal)


if __name__ == '__main__':
    fr, ob = build()
    sheet(fr, '/home/claude/mage_sheet.png')
    if len(sys.argv) > 1:
        print(export(fr, ob, sys.argv[1]))
