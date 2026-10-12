import io, base64
import ground as G, objects as O
assets={}
def put(k,cv):
    b=io.BytesIO(); cv.im.save(b,'PNG',optimize=True); assets[k]=base64.b64encode(b.getvalue()).decode()
for v in range(8): put(f"g{v}",G.grass(v))
for v in range(4):
    put(f"dt{v}",G.dirt(v)); put(f"sd{v}",G.sand(v)); put(f"st{v}",G.cobble(v))
    put(f"wa{v}",G.water(v,False)); put(f"ws{v}",G.water(v,True))
for v in range(3): put(f"oak{v}",O.oak(v)); put(f"rock{v}",O.rock(v))
for v in range(2): put(f"pine{v}",O.pine(v))
for v in range(4):
    put(f"bu{v}0",O.bush(v,False)); put(f"bu{v}1",O.bush(v,True))
for m in range(16): put(f"fe{m}",O.fence(m))
out=["package com.aldoria.rpg","","/** Texturas de terreno, água, árvores, arbustos, pedras e cercas (PNG em base64), geradas por tools/gen_nature. */","object ArtNatureData {","    private val m: HashMap<String, String> by lazy {","        val h = HashMap<String, String>()"]
for k,v in assets.items():
    chunks=[v[i:i+30000] for i in range(0,len(v),30000)]
    if len(chunks)==1: out.append(f'        h["{k}"] = "{v}"')
    else: out.append(f'        h["{k}"] = listOf({", ".join(chr(34)+c+chr(34) for c in chunks)}).joinToString("")')
out+=["        h","    }","","    fun get(key: String): String? = m[key]","}",""]
open('/home/claude/proj/aldoria-rpg-main/app/src/main/java/com/aldoria/rpg/ArtNatureData.kt','w').write("\n".join(out))
print(len(assets), sum(len(v) for v in assets.values()))
