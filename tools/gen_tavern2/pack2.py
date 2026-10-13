import io, base64, sys
sys.path.insert(0,'.')
import tav as T
import ext as E
import fac2 as F
import int2 as N
import interior as OI

T.HGT=190; T.GBASE=T.HGT-10; T.GAP=T.YR+16
assets={}
def put(k,cv):
    b=io.BytesIO(); cv.im.save(b,'PNG',optimize=True); assets[k]=base64.b64encode(b.getvalue()).decode()

f0=F.facade2(0)
for col in range(14): put(f"fc{col}",E.crop(f0,col*32,32))
for fr in range(5):
    ff=F.facade2(fr); put(f"d{fr}",E.crop(ff,192,64))
cut=F.cut2()
for col in range(14): put(f"cc{col}",E.crop(cut,col*32,32))
def roof_filled():
    r=T.roof()
    for y in range(T.MT,T.HGT-1):
        for x in range(T.BW):
            if T.inside(x,y) and r.get(x,y)[3]<255: r.set(x,y,T.R[1])
    return r
RF=roof_filled(); put("roof",RF)
for fr in (0,1):
    bs=N.backstrip(fr)
    for rel in range(14): put(f"bk{rel}{fr}",E.crop(bs,rel*32,32))
for fr in (0,1):
    h=N.hearth(fr); put(f"hl{fr}",E.crop(h,0,32)); put(f"hr{fr}",E.crop(h,32,32))
for k in range(3): put(f"sf{k}",N.shelf(k))
for k in range(2): put(f"ck{k}",N.casks(k))
put("cnL",N.counter(0)); put("cnM",N.counter(1,0)); put("cnR",N.counter(2)); put("cnT",N.counter(1,1)); put("cnC",N.counter(1,2)); put("cnB",N.counter(1,3))
put("tb0",N.table(0,0)); put("tb1",N.table(1,0)); put("tb2",N.table(0,1)); put("tb3",N.table(1,1)); put("stool",N.stool())
for v in range(4): put(f"fl{v}",OI.floor(v))

out=["package com.aldoria.rpg","","/** Arte da Taverna v3 (PNG em base64), gerada por tools/gen_tavern2. */","object ArtTavernData {","    private val m: HashMap<String, String> by lazy {","        val h = HashMap<String, String>()"]
for k,v in assets.items():
    chunks=[v[i:i+30000] for i in range(0,len(v),30000)]
    if len(chunks)==1: out.append(f'        h["{k}"] = "{v}"')
    else: out.append(f'        h["{k}"] = listOf({", ".join(chr(34)+c+chr(34) for c in chunks)}).joinToString("")')
out+=["        h","    }","","    fun get(key: String): String? = m[key]","}",""]
open('/home/claude/proj/aldoria-rpg-main/app/src/main/java/com/aldoria/rpg/ArtTavernData.kt','w').write("\n".join(out))
print(len(assets), sum(len(v) for v in assets.values()))
# preview exterior composto
from pa import Cv
P=Cv(470+20,222+10+128+10)
P.blit(RF,0,0)
P.blit(f0,11,190)
P.save('/home/claude/out/ext_all.png',2)
