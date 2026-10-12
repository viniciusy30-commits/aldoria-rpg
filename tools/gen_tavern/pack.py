import io, base64
import facade as F, door as D, roof as R, interior as I

assets={}
def put(k,cv):
    b=io.BytesIO(); cv.im.save(b,'PNG',optimize=True); assets[k]=base64.b64encode(b.getvalue()).decode()

for kind in (0,1,2,3):
    for e in (0,1,2):
        for fr in ((0,1) if kind==2 else (0,)):
            put(f"f{kind}{e}{fr}",F.facade(kind,e,fr))
for f in range(5): put(f"d{f}",D.door(f))
put("roof",R.roof())
for kind in (0,1,2,3):
    for e in (0,1,2):
        for fr in ((0,1) if kind==2 else (0,)):
            put(f"b{kind}{e}{fr}",I.backwall(kind,e,fr))
for v in range(4): put(f"fl{v}",I.floor(v))
put("tb0",I.table(0)); put("tb1",I.table(1)); put("stool",I.stool())
for v in range(3): put(f"ct{v}",I.counter(v))
put("cask",I.cask())

out=["package com.aldoria.rpg","","/** Arte da Taverna (PNG em base64), gerada por tools/gen_tavern. */","object ArtTavernData {","    private val m: HashMap<String, String> by lazy {","        val h = HashMap<String, String>()"]
for k,v in assets.items():
    chunks=[v[i:i+30000] for i in range(0,len(v),30000)]
    if len(chunks)==1: out.append(f'        h["{k}"] = "{v}"')
    else: out.append(f'        h["{k}"] = listOf({", ".join(chr(34)+c+chr(34) for c in chunks)}).joinToString("")')
out+=["        h","    }","","    fun get(key: String): String? = m[key]","}",""]
open('/home/claude/proj/aldoria-rpg-main/app/src/main/java/com/aldoria/rpg/ArtTavernData.kt','w').write("\n".join(out))
print(len(assets), sum(len(v) for v in assets.values()))
