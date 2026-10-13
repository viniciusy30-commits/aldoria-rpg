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
def staff_shaft(b, x, top, y0, y1, bot=60):
    for y in range(y0, y1):
        k = y - top
        b.set(x, y, WOOD[1]); b.set(x + 1, y, WOOD[2]); b.set(x + 2, y, WOOD[3])
        if k % 13 == 5:
            b.set(x, y, WOOD[3]); b.set(x + 1, y, WOOD[4]); b.set(x + 2, y, WOOD[4])
        if k % 13 == 6:
            b.set(x, y, WOOD[0])
        if 16 <= k <= 31 and (k % 6) < 3:
            b.set(x + (k % 6), y, GOLD[2])
        if k in (38, 45, 51) and y < bot - 5:
            b.set(x + 1, y, (0x9C, 0xD8, 0xFF)); b.set(x + 1, y + 1, (0x5C, 0x9C, 0xE0))
        if y >= bot - 4:
            r = y - (bot - 4)
            if r == 0:
                b.rect(x - 1, y, 5, 1, GOLD[0])
            elif r == 1:
                b.rect(x - 1, y, 5, 1, GOLD[2])
            elif r == 2:
                b.rect(x, y, 3, 1, GOLD[3])
            else:
                b.set(x + 1, y, GOLD[4])


def staff(b, x, top, bot, cy):
    """cajado: x = pixel esquerdo (3 de largura)."""
    cx = x + 1.5
    staff_shaft(b, x, top, top, bot, bot)
    # aneis de ouro
    for yy, w in ((top, 5), (top + 12, 5), (top + 32, 5)):
        b.rect(x - 1, yy, w, 1, GOLD[0]); b.rect(x - 1, yy + 1, w, 1, GOLD[2]); b.rect(x - 1, yy + 2, w, 1, GOLD[3])
    # chifres de ouro em crescente
    for sg in (-1, 1):
        s_ = curve([(cx + sg * .5, top + .5), (cx + sg * 6.5, top - .5), (cx + sg * 7.8, top - 8), (cx + sg * 2.6, top - 12.5)], 1.7, .8, 30, 1.0)
        tube(b, s_, GOLD)
    # cristal facetado grande
    pts = [(cx, cy - 6.6), (cx + 4.6, cy - .5), (cx, cy + 5.4), (cx - 4.6, cy - .5)]
    m = pmask(pts)
    for (px, py) in m:
        l = -(((px + .5 - cx) / 4.6) * LX + ((py + .5 - cy) / 6.0) * LY)
        b.set(px, py, CRY[tone(l)])
    for (px, py) in m:
        if abs(px + .5 - cx) < 1.3 and abs(py + .5 - (cy - .8)) < 2.8:
            b.set(px, py, (0xFF, 0xE0, 0xA0))
        elif int(px) == int(cx) and py > cy + 1:
            b.set(px, py, CRY[4])
    b.set(int(cx), int(cy - 1), WHITE); b.set(int(cx), int(cy - 2), WHITE)
    for dx, dy in ((9, -3), (-9, 3), (8, 6), (-8, -5)):
        b.set(int(cx) + dx, int(cy) + dy, (0xFF, 0xE8, 0xB0))
        b.set(int(cx) + dx + (1 if dx > 0 else -1), int(cy) + dy, (0xFF, 0xB0, 0x60))
    # amuleto pendurado
    for yy in range(top + 3, top + 8):
        b.set(x + 4, yy, LEA[3])
    ell(b, x + 4, top + 9.5, 2.0, 2.2, RED)
    b.set(x + 3, top + 8, (0xFF, 0xC8, 0xC8))


def pouch(b, x, y, w, h):
    b.rect(x + 1, y - 2, 2, 2, LEA[4])
    b.rect(x, y, w, h, LEA[2]); b.rect(x, y, 1, h, LEA[1]); b.rect(x + w - 1, y, 1, h, LEA[3])
    b.rect(x, y, w, 3, LEA[3]); b.rect(x + 1, y, w - 2, 1, LEA[1]); b.rect(x, y + 2, w, 1, LEA[4])
    b.set(x + w // 2, y + 3, GOLD[2]); b.rect(x, y + h - 1, w, 1, LEA[4])
    b.p[y + h - 1][x] = None; b.p[y + h - 1][x + w - 1] = None


def vial(b, x, y):
    b.rect(x, y - 2, 2, 2, LEA[4])
    b.rect(x, y, 2, 1, (0x9A, 0x6B, 0x3A)); b.rect(x, y + 1, 2, 2, (0xCF, 0xE6, 0xF5))
    ell(b, x + 1, y + 5.2, 2.8, 2.8, RED); b.set(x - 1, y + 4, (0xFF, 0xD0, 0xD0))


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


def hat(b, cx, cy_brim, P, rbase, tipr=0.9, brimrx=17, brimry=4.4, starpos=None, ph=0, buckle=True):
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
    if buckle:
        b.rect(int(cx) - 2, int(cy_brim) - 1, 4, 3, GOLD[1])
        b.rect(int(cx) - 1, int(cy_brim), 2, 1, GOLD[4])
        b.set(int(cx) - 2, int(cy_brim) - 1, GOLD[0])
    if starpos:
        star(b, *starpos)
        b.set(starpos[0] + 5, starpos[1] + 4, GOLD[0])
        b.set(starpos[0] - 4, starpos[1] + 5, GOLD[1])


def eye(b, x, y, flip=False):
    b.rect(x - 1, y - 1, 5, 1, SKIN[4])
    b.rect(x, y, 3, 2, (0xF6, 0xF6, 0xFA))
    ix = x if flip else x + 1
    b.rect(ix, y, 2, 2, (0x2F, 0x6D, 0xB5))
    b.set(ix + (0 if flip else 1), y + 1, DARK)
    b.set(ix + (1 if flip else 0), y, (0x9C, 0xD0, 0xFF))
    b.set(x, y + 2, SKIN[3]); b.set(x + 1, y + 2, SKIN[3])


def grip(b, cx, cy):
    """mao fechada em volta do cajado (cx = centro do cajado)."""
    x0 = cx - 3
    b.rect(x0, cy - 2, 7, 5, SKIN[2])
    for yy in range(cy - 2, cy + 3):
        b.set(x0, yy, SKIN[1]); b.set(x0 + 6, yy, SKIN[3])
    for yy in (cy - 1, cy + 1):
        b.rect(x0 + 1, yy, 5, 1, SKIN[3])
    b.rect(x0, cy - 2, 7, 1, SKIN[0])
    b.rect(x0, cy + 2, 7, 1, SKIN[4])
    ell(b, cx - 1, cy - 3.6, 1.8, 1.4, SKIN)


def boot_front(b, cx, fw):
    yb = {1: 59, 0: 58, -1: 56}[fw]
    b.rect(cx - 3, 48, 7, yb - 50, LEA[2]); b.rect(cx - 3, 48, 2, yb - 50, LEA[1]); b.rect(cx + 2, 48, 2, yb - 50, LEA[3])
    ell(b, cx, yb - 3.2, 4.7, 3.3, LEA)
    b.rect(cx - 4, yb - 1, 9, 1, LEA[4])
    b.set(cx - 2, yb - 5, LEA[0]); b.set(cx - 1, yb - 5, LEA[0]); b.set(cx - 2, yb - 4, LEA[0])


def boot_side(b, x, yt, yb, dark=0.0):
    rp = [sh(c, -dark) if dark else c for c in LEA]
    b.rect(x, yt, 6, yb - yt - 3, rp[2]); b.rect(x, yt, 2, yb - yt - 3, rp[1]); b.rect(x + 4, yt, 2, yb - yt - 3, rp[3])
    b.rect(x - 1, yt, 8, 2, rp[1]); b.rect(x - 1, yt + 1, 8, 1, rp[3])
    ell(b, x + 5, yb - 3, 6.4, 3.2, rp)
    b.rect(x - 1, yb - 4, 3, 3, rp[3]); b.rect(x - 1, yb - 1, 13, 1, rp[4])
    b.set(x + 9, yb - 5, rp[0]); b.set(x + 10, yb - 4, rp[0]); b.set(x + 2, yt + 5, GOLD[2])


# ------------------------------------------------------------------ FRENTE
def front(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    st = {0: 0, 1: 1, 2: 0, 3: -1}[f]
    ph = f * 1.3
    for cx, fw in ((25, st), (39, -st)):
        boot_front(out, cx, fw)
    staff(body, 49, 14, 60, 6)
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
    body.rect(31, 40, 2, 2, RED[2]); body.set(31, 40, RED[0]); body.set(30, 39, GOLD[0])
    pouch(body, 20, 43, 6, 6)
    vial(body, 39, 43)
    # braco pendurado (lado esquerdo de quem ve)
    hy = 47 + (-2 if st == 1 else 2 if st == -1 else 0)
    s = curve([(22, 31), (16, 35), (14.5, 41), (16.5, hy - 4)], 4.6, 5.6, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 17, hy, 3.1, 3.5, SKIN); body.set(16, hy + 2, SKIN[3]); body.set(18, hy + 1, SKIN[3])
    # braco que segura o cajado
    s = curve([(42, 31), (47, 32), (45.5, 37), (48, 39.5)], 4.6, 5.4, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.78, 0.97, GOLD)])
    grip(body, 50, 42)
    staff_shaft(body, 49, 14, 40, 45)
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
    eye(body, 28, 24); eye(body, 34, 24, True)
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
    return out, (50.5, 6 + bob)


# ------------------------------------------------------------------ COSTAS
BACKROBE = ramp((0x33, 0x55, 0xC8))


def back(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    st = {0: 0, 1: 1, 2: 0, 3: -1}[f]
    ph = f * 1.3
    for cx, fw in ((25, st), (39, -st)):
        boot_front(out, cx, fw)
    staff(body, 12, 14, 60, 6)
    rows = robe_rows(32, 29, 54, 10.5, 18.5)
    robe_body(body, rows, 53, ph, BACKROBE, (0.27, 0.73), 0.0)
    for y in range(34, 50):
        body.set(32, y, BACKROBE[4]); body.set(31, y, BACKROBE[1] if y % 3 else BACKROBE[2])
    # brasao dourado
    cx, cy = 32, 46
    for y in range(cy - 6, cy + 7):
        for x in range(cx - 6, cx + 7):
            n = ((x + .5 - cx) / 4.3) ** 2 + ((y + .5 - cy) / 4.3) ** 2
            if .5 < n <= 1:
                body.set(x, y, GOLD[0] if (x < cx and y < cy) else GOLD[3] if (x >= cx and y >= cy) else GOLD[2])
            elif n <= .5:
                body.set(x, y, BACKROBE[4])
    star(body, cx, cy)
    # cinto, no e pontas
    for y in range(37, 40):
        xl, xr = rows[y]
        for x in range(int(xl) + 1, int(xr)):
            body.set(x, y, LEA[1] if y == 37 else LEA[2] if y == 38 else LEA[4])
        for x in range(int(xl) + 3, int(xr) - 1, 4):
            body.set(x, 38, LEA[3])
    body.rect(30, 40, 2, 4, LEA[3]); body.rect(34, 40, 2, 3, LEA[3]); body.set(30, 43, LEA[4]); body.set(35, 42, LEA[4])
    ell(body, 32.5, 38.5, 3.0, 2.4, LEA); body.set(31, 37, LEA[0]); body.rect(32, 38, 2, 1, GOLD[2])
    # bracos (antes do manto, que cobre os ombros)
    hy = 47 + (-2 if st == 1 else 2 if st == -1 else 0)
    s_ = curve([(42, 32), (46.5, 36), (48, 41), (47.5, hy - 4)], 4.8, 5.4, 40, 1.0)
    tube(body, s_, ROBE, bands=[(0.78, 0.97, GOLD)])
    ell(body, 47.5, hy, 3.1, 3.5, SKIN); body.set(46, hy + 2, SKIN[3]); body.set(48, hy + 1, SKIN[3])
    s_ = curve([(22, 32), (18, 35), (17.5, 38), (15.5, 40)], 4.8, 5.2, 40, 1.0)
    tube(body, s_, ROBE, bands=[(0.74, 0.95, GOLD)])
    grip(body, 13, 42)
    # manto sobre os ombros + gola
    ell(body, 32, 31, 14, 5, MANT)
    for x in range(18, 46):
        nx = (x + .5 - 32) / 14.0
        if abs(nx) < 1:
            yy = int(31 + 5 * math.sqrt(1 - nx * nx))
            body.set(x, yy, GOLD[2]); body.set(x, yy - 1, GOLD[3] if x % 3 == 0 else GOLD[2])
    ell(body, 32, 28.3, 8.2, 3.0, MANT)
    # cabelo branco em mechas, com pontas
    sway = 1 if f == 1 else -1 if f == 3 else 0
    pts = [(26, 19), (38, 19), (39.5, 25), (38.5, 31), (37, 36), (35.5, 40), (34, 37), (32 + sway, 42), (30, 37), (28.5, 40), (27, 36), (25.5, 31), (24.5, 25)]
    m = pmask(pts)

    def fn(fr, x, y):
        t = 0 if fr < .12 else 1 if fr < .4 else 2 if fr < .66 else 3 if fr < .92 else 4
        k = (x + y // 6) % 4
        if k == 0 and y > 23:
            t = min(4, t + 1)
        elif k == 2 and y > 23:
            t = max(0, t - 1)
        return t
    paint_frac(body, m, BEARD, fn)
    # chapeu (ponta cai para a direita de quem ve)
    hat(body, 32, 15, [(32, 12), (33, 5.5), (41, 3.5), (51.5, 10)], 9.4, 0.9, 17.5, 4.4, None, ph, buckle=False)
    out.blit(body, 0, bob)
    return out, (13.5, 6 + bob)


# ------------------------------------------------------------------ LADO (direita)
def side(f):
    out = Buf()
    body = Buf()
    bob = -2 if f in (1, 3) else 0
    ph = f * 1.3
    sw = {0: 0, 1: -2, 2: 0, 3: 2}[f]
    staff(body, 46, 14, 60, 6)
    # botas: (x, yt, yb) longe e perto
    if f == 1:
        far = (20, 49, 56); near = (35, 48, 59)
    elif f == 3:
        far = (35, 48, 59); near = (21, 49, 56)
    else:
        far = (30, 48, 58); near = (26, 48, 58)
    boot_side(out, far[0], far[1], far[2], 0.18)
    boot_side(out, near[0], near[1], near[2])
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
    pouch(body, 25, 43, 6, 6)
    # manto
    ell(body, 31.5, 31, 8.5, 4.6, MANT)
    for x in range(23, 41):
        nx = (x + .5 - 31.5) / 8.5
        if abs(nx) < 1:
            yy = int(31 + 4.6 * math.sqrt(1 - nx * nx))
            body.set(x, yy, GOLD[2]); body.set(x, yy - 1, GOLD[3] if x % 3 == 0 else GOLD[2])
    # braco da frente segura o cajado
    s = curve([(30, 33), (33, 41), (38, 44.5), (44.5, 43)], 4.6, 5.3, 40, 1.0)
    tube(body, s, ROBE, bands=[(0.76, 0.95, GOLD)])
    grip(body, 47, 43)
    # cabelo atras da cabeca
    ell(body, 29.5, 27, 6, 8.5, BEARD)
    for yy in range(22, 35):
        body.set(25 + (yy % 3), yy, BEARD[3]) if yy % 2 else None
    # rosto de perfil
    ell(body, 35, 24.5, 5.8, 6.7, SKIN)
    ell(body, 41.2, 27, 2.6, 2.4, [SKIN[1], SKIN[1], (0xEE, 0xA8, 0x8A), (0xD8, 0x8C, 0x72), SKIN[4]])
    body.set(40, 26, SKIN[0])
    ell(body, 31.2, 25.5, 1.5, 2.3, SKIN)
    body.rect(36, 23, 4, 1, SKIN[4]); body.rect(37, 24, 3, 2, (0xF6, 0xF6, 0xFA)); body.rect(38, 24, 2, 2, (0x2F, 0x6D, 0xB5)); body.set(39, 25, DARK); body.set(38, 24, (0x9C, 0xD0, 0xFF))
    body.rect(35, 21, 6, 2, BEARD[1]); body.rect(35, 22, 6, 1, BEARD[2]); body.set(41, 21, BEARD[1])
    body.rect(37, 28, 2, 1, BLUSH)
    ell(body, 39.5, 30, 3.8, 2.0, BEARD)
    bx = 1 if f == 1 else -1 if f == 3 else 0
    pts = [(31, 28), (40, 30.5), (43, 34), (42, 40), (39 + bx, 46), (35 + bx, 50), (32, 44), (30, 36)]
    beard(body, pts, BEARD, [(36, 36, 44), (39, 35, 42), (34 + bx, 44, 48), (33, 33, 40)])
    # chapeu: ponta cai para tras
    hat(body, 33.5, 15.5, [(33.5, 12.5), (34, 5.5), (24, 3.5), (12, 10.5)], 9.0, 0.9, 15, 4.0, (31, 8), ph)
    out.blit(body, 0, bob)
    return out, (47.5, 6 + bob)


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
