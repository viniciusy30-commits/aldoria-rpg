"""Gera app/src/main/java/com/aldoria/rpg/MageData.kt a partir dos PNGs do mago em tools/mage_src.

Uso:  python3 tools/gen_mage_png.py

Entrada: tools/mage_src/{north,east,south,west}/frame_000..005.png (quadros de caminhada 96x96).
Saida:   MageData.kt com 24 quadros de 64x64 (4 direcoes x 6 quadros) e a posicao da bola de fogo de cada um.
Ordem das direcoes no jogo: 0 costas (north), 1 direita (east), 2 frente (south), 3 esquerda (west).
"""
import os
from collections import Counter, deque
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "mage_src")
OUT = os.path.join(HERE, "..", "app", "src", "main", "java", "com", "aldoria", "rpg", "MageData.kt")

DIRS = ["north", "east", "south", "west"]
FRAMES = 6
CROP = (16, 16, 80, 80)  # recorte fixo 64x64 dentro do quadro 96x96 (nada do boneco fica de fora)
CH = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#%&()*+,-/:;<=>?@[]^_{|}~"
BS = len(CH)

# cores da chama (para achar o centro da bola de fogo em cada quadro)
FLAME = {
    (0xF8, 0xD5, 0x57), (0xFD, 0xC3, 0x51), (0xF4, 0xAB, 0x32), (0xEC, 0x98, 0x2D), (0xF1, 0x81, 0x21),
    (0xEB, 0x78, 0x1E), (0xEB, 0x4D, 0x0F), (0xF9, 0x5B, 0x13), (0xE9, 0x5C, 0x1A), (0xFC, 0xDB, 0x6B),
    (0xF9, 0xE2, 0x8B), (0xFA, 0xEA, 0xA7), (0xDE, 0x37, 0x1D), (0xFB, 0xBA, 0x3B), (0xDA, 0x81, 0x25),
}


def load(d, k):
    im = Image.open(os.path.join(SRC, d, "frame_%03d.png" % k)).convert("RGBA").crop(CROP)
    return im


def orb_center(im):
    px = im.load()
    seen = set()
    best = []
    for y in range(64):
        for x in range(64):
            if (x, y) in seen or px[x, y][3] < 128 or px[x, y][:3] not in FLAME:
                continue
            q = deque([(x, y)])
            seen.add((x, y))
            comp = []
            while q:
                cx, cy = q.popleft()
                comp.append((cx, cy))
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        nx, ny = cx + dx, cy + dy
                        if 0 <= nx < 64 and 0 <= ny < 64 and (nx, ny) not in seen and px[nx, ny][3] >= 128 and px[nx, ny][:3] in FLAME:
                            seen.add((nx, ny))
                            q.append((nx, ny))
            if len(comp) > len(best):
                best = comp
    # centro de massa: as linguas de fogo finas puxam pouco, entao cai no corpo redondo da bola
    sx = sum(p[0] + 0.5 for p in best) / len(best)
    sy = sum(p[1] + 0.5 for p in best) / len(best)
    return sx, sy


def main():
    imgs = [[load(d, k) for k in range(FRAMES)] for d in DIRS]
    count = Counter()
    for row in imgs:
        for im in row:
            for p in im.getdata():
                if p[3] >= 128:
                    count[p[:3]] += 1
    palette = [c for c, _ in count.most_common()]
    index = {c: i + 1 for i, c in enumerate(palette)}  # 0 = transparente
    if len(palette) + 1 > BS * BS:
        raise SystemExit("paleta grande demais")

    def code(i):
        return CH[i // BS] + CH[i % BS]

    frames, orb = [], []
    for row in imgs:
        for im in row:
            px = im.load()
            toks = []
            run_i, run_n = None, 0
            for y in range(64):
                for x in range(64):
                    p = px[x, y]
                    i = 0 if p[3] < 128 else index[p[:3]]
                    if i == run_i:
                        run_n += 1
                    else:
                        if run_i is not None:
                            toks.append(code(run_i) + (str(run_n) if run_n > 1 else ""))
                        run_i, run_n = i, 1
            toks.append(code(run_i) + (str(run_n) if run_n > 1 else ""))
            frames.append("".join(toks))
            ox, oy = orb_center(im)
            orb += [round(ox, 1), round(oy, 1)]

    hexs = "".join("%02X%02X%02X" % c for c in palette)
    lines = []
    lines.append("package com.aldoria.rpg")
    lines.append("")
    lines.append("/** Dados da sprite do mago (64 x 64), gerados por tools/gen_mage_png.py a partir de tools/mage_src. Não editar à mão. */")
    lines.append("object MageData {")
    lines.append("    /** quadros de caminhada por direção (0 costas, 1 direita, 2 frente, 3 esquerda) */")
    lines.append("    const val FRAMES = %d" % FRAMES)
    lines.append("    /** alfabeto dos códigos (2 letras por cor; \"AA\" = transparente) */")
    lines.append('    const val CH = "%s"' % CH)
    lines.append('    const val HEX = "%s"' % hexs)
    lines.append("    /** posição (x, y em pixels de 64 x 64) da bola de fogo em cada quadro: [(direção * FRAMES + quadro) * 2] */")
    lines.append("    val ORB = floatArrayOf(%s)" % ", ".join("%.1ff" % v for v in orb))
    lines.append("    /** [direção * FRAMES + quadro] */")
    lines.append("    val F = arrayOf(")
    for i, f in enumerate(frames):
        lines.append('        "%s"%s' % (f, "," if i < len(frames) - 1 else ""))
    lines.append("    )")
    lines.append("}")
    with open(OUT, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines) + "\n")
    print("cores:", len(palette), "quadros:", len(frames), "maior quadro:", max(len(f) for f in frames), "bytes")


if __name__ == "__main__":
    main()
