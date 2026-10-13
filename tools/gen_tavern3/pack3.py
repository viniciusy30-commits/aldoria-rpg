import io, base64, re, sys
sys.path.insert(0,'.')
from PIL import Image
import ext3 as E, int3 as N
from pa import Cv
KT='../../app/src/main/java/com/aldoria/rpg/ArtTavernData.kt'
old={}
src=open(KT).read()
for m in re.finditer(r'h\["(\w+)"\] = (.*)',src):
    old[m.group(1)]=''.join(re.findall(r'"([A-Za-z0-9+/=]+)"',m.group(2)))
assets={}
def put(k,cv):
    b=io.BytesIO(); cv.im.save(b,'PNG',optimize=True); assets[k]=base64.b64encode(b.getvalue()).decode()
# exterior novo (15 colunas)
fac=E.facade_full()
for col in range(15): put(f"fc{col}",E.crop(fac,col*32,32))

for fr in range(5):
    im=Cv(E.FW,E.FH); im.blit(fac,0,0); im.blit(E.door_frame(fr),E.DX0,0)
    put(f"d{fr}",E.crop(im,E.DX0,96))          # 96x192: as 3 colunas da porta (fachada completa + portal com folhas)
cut=E.cut_full()
for col in range(15): put(f"cc{col}",E.crop(cut,col*32,32))
put("roof",N.roof())
for fr in (0,1):
    bs=N.backstrip(fr)
    for rel in range(15): put(f"bk{rel}{fr}",E.crop(bs,rel*32,32))
for v in range(4): put(f"fl{v}",N.floor_hall(v)); put(f"lf{v}",N.floor_loft(v))
for k in range(4): put(f"rl{k}",N.rail(k))
for s in (0,1):
    for p in range(3): put(f"st{s}{p}",N.stairs(s,p))
for k in range(2): put(f"bd{k}",N.bed(k))
# moveis do salao: mantidos da versao anterior
for k in ["hl0","hl1","hr0","hr1","sf0","sf1","sf2","ck0","ck1","cnL","cnM","cnR","cnT","cnC","cnB","tb0","tb1","tb2","tb3","stool"]:
    assets[k]=old[k]
out=["package com.aldoria.rpg","","/** Arte da Taverna v4 (PNG em base64), gerada por tools/gen_tavern3. */","object ArtTavernData {","    private val m: HashMap<String, String> by lazy {","        val h = HashMap<String, String>()"]
for k,v in assets.items():
    chunks=[v[i:i+30000] for i in range(0,len(v),30000)]
    if len(chunks)==1: out.append(f'        h["{k}"] = "{v}"')
    else: out.append(f'        h["{k}"] = listOf({", ".join(chr(34)+c+chr(34) for c in chunks)}).joinToString("")')
out+=["        h","    }","","    fun get(key: String): String? = m[key]","}",""]
open(KT,'w').write("\n".join(out))
print(len(assets), sum(len(v) for v in assets.values()))
