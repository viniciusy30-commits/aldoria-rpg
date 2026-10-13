# Taverna v4 - paleta e pecas de pixel art compartilhadas
import random, math
from pa import *

def H(x,y,s=0):
    n=(x*374761393+y*668265263+s*982451653)&0xffffffff
    n=((n^(n>>13))*1274126177)&0xffffffff
    return (n^(n>>16))&0xffff

# paleta
WD=[C('2a170d'),C('4a2a16'),C('6b3f20'),C('8f5a2e'),C('b07a42')]       # madeira escura..clara
ST=[C('4d4a52'),C('6c6a70'),C('8a8780'),C('a8a398'),C('c7c1b0')]       # pedra
MT=C('3d3a40')                                                           # argamassa
PL=[C('a88a5c'),C('c9ab78'),C('e2cb9a'),C('f4e6bd')]                     # reboco
RT=[C('4a1d1a'),C('7a2e22'),C('a14530'),C('c4623c'),C('e08a55')]         # telhas
TEAL=[C('153f44'),C('1f6a6a'),C('2f9488'),C('62c2ac')]                   # venezianas
IRON=[C('15131a'),C('2c2a36'),C('4a4858'),C('77758a')]
GOLD=[C('7a4a10'),C('c98a1c'),C('f2c13a'),C('fff0a0')]
RED=[C('4a0f18'),C('8a1c28'),C('c43a3c'),C('e87060')]
GLASS=[C('3a2a1a'),C('9a5a1c'),C('f0a030'),C('ffdf80')]

def wood(c,x,y,w,h,horiz=True,seed=1,tone=2):
    """tabua com veios, luz de cima/esquerda"""
    for yy in range(y,y+h):
        for xx in range(x,x+w):
            t=(yy-y)/max(1,h-1) if horiz else (xx-x)/max(1,w-1)
            k=tone+(1 if t<0.25 else (-1 if t>0.75 else 0))
            k=max(0,min(4,k)); col=WD[k]
            n=H(xx//(3 if horiz else 1),yy//(1 if horiz else 3),seed)%11
            if n==0: col=sh(col,-0.12)
            elif n==1: col=sh(col,0.08)
            c.set(xx,yy,col)

def stone(c,x0,x1,y0,y1,seed=3,dark=0.0):
    rng=random.Random(seed)
    c.rect(x0,y0,x1-x0,y1-y0,MT)
    y=y0
    while y<y1:
        h=rng.choice([9,10,11])
        if y1-y-h<6: h=y1-y
        x=x0-rng.randrange(0,14)
        while x<x1:
            w=rng.choice([13,15,17,20]); sx=x; x+=w
            if sx+w<=x0 or sx>=x1: continue
            base=ST[rng.choice([1,2,2,3,2,1,3])]
            if rng.random()<0.25: base=mix(base,C('b08a60'),0.25)
            base=sh(base,dark)
            for yy in range(y+1,y+h):
                for xx in range(sx+1,sx+w):
                    if x0<=xx<x1 and y0<=yy<y1:
                        c.set(xx,yy,sh(base,0.10-0.28*(yy-y)/h))
            for xx in range(sx+1,sx+w):
                if x0<=xx<x1 and y0<=y+1<y1: c.set(xx,y+1,sh(base,0.36))
                if x0<=xx<x1 and y0<=y+h-1<y1: c.set(xx,y+h-1,sh(base,-0.45))
            for yy in range(y+1,y+h):
                if y0<=yy<y1:
                    if x0<=sx+1<x1: c.set(sx+1,yy,sh(base,0.22))
                    if x0<=sx+w-1<x1: c.set(sx+w-1,yy,sh(base,-0.35))
            for k in range(rng.randrange(2,5)):
                px=sx+2+rng.randrange(max(1,w-4)); py=y+2+rng.randrange(max(1,h-4))
                if x0<=px<x1 and y0<=py<y1: c.set(px,py,sh(base,rng.choice([-0.2,0.18,-0.1])))
        y+=h

def plaster(c,x0,x1,y0,y1,seed=5):
    for y in range(y0,y1):
        for x in range(x0,x1):
            t=(y-y0)/max(1,y1-y0-1)
            col=mix(PL[3],PL[1],t*0.7)
            n=H(x//2,y//2,seed)%13
            if n==0: col=sh(col,-0.09)
            elif n==1: col=sh(col,0.06)
            c.set(x,y,col)

def beam(c,x,y,w,h,seed=1):
    wood(c,x,y,w,h,horiz=(w>=h),seed=seed,tone=2)
    # contorno escuro
    if w>=h:
        for xx in range(x,x+w): c.set(xx,y,WD[0]); c.set(xx,y+h-1,WD[0])
    else:
        for yy in range(y,y+h): c.set(x,yy,WD[0]); c.set(x+w-1,yy,WD[0])

def nail(c,x,y): c.set(x,y,IRON[3]); c.set(x+1,y,IRON[1])

def lantern(c,x,y,glow=True):
    """lampiao pendurado: x,y = ponto de fixacao do topo"""
    c.rect(x-1,y,3,2,IRON[1]); c.rect(x-3,y+2,7,1,IRON[2])
    c.rect(x-3,y+3,7,9,IRON[0])
    c.rect(x-2,y+4,5,7,GLASS[2] if glow else GLASS[1]); c.rect(x-1,y+5,3,4,GLASS[3])
    c.rect(x-3,y+12,7,1,IRON[2]); c.rect(x-1,y+13,3,1,IRON[1])
    for dx in (-3,3):
        for yy in range(y+3,y+12): c.set(x+dx,yy,IRON[1])
    if glow:
        for dx in range(-6,7):
            for dy in range(-2,17):
                d=math.hypot(dx,(dy-7)*0.7)
                if d<8 and c.get(x+dx,y+dy)[3]==0:
                    a=int(70*(1-d/8)); c.set(x+dx,y+dy,(255,200,90,a))

def window_rect(c,x,y,w,h,seed=1,shutters=True,box=True,night=True):
    """janela de caixilho com vidro aceso, venezianas e jardineira"""
    # moldura de madeira
    c.rect(x-2,y-2,w+4,h+4,WD[0]); c.rect(x-1,y-1,w+2,h+2,WD[3])
    for yy in range(h):
        for xx in range(w):
            t=yy/h; col=mix(GLASS[3],GLASS[1],t*0.8)
            if H(x+xx,y+yy,seed)%9==0: col=sh(col,-0.07)
            c.set(x+xx,y+yy,col)
    # cortina vermelha
    c.rect(x,y,3,h,RED[1]); c.rect(x+w-3,y,3,h,RED[1]); c.rect(x,y,w,2,RED[0])
    for yy in range(y+2,y+h,3): c.set(x+1,yy,RED[2]); c.set(x+w-2,yy,RED[2])
    # caixilhos
    c.rect(x+w//2-1,y,2,h,WD[1]); c.rect(x,y+h//2-1,w,2,WD[1])
    # reflexo
    c.set(x+5,y+3,GLASS[3]); c.set(x+6,y+3,GLASS[3]); c.set(x+5,y+4,GLASS[3])
    # peitoril
    c.rect(x-3,y+h+2,w+6,3,ST[3]); c.rect(x-3,y+h+4,w+6,1,ST[0])
    if shutters:
        for sx in (x-9,x+w+3):
            c.rect(sx,y-2,6,h+4,TEAL[1])
            for yy in range(y-1,y+h+2,3): c.rect(sx+1,yy,4,1,TEAL[2]); c.rect(sx+1,yy+1,4,1,TEAL[0])
            c.rect(sx,y-2,1,h+4,TEAL[0]); c.rect(sx+5,y-2,1,h+4,TEAL[0])
            c.rect(sx,y-2,6,1,TEAL[3]); c.rect(sx,y+h+1,6,1,TEAL[0])
    if box:
        c.rect(x-4,y+h+5,w+8,6,WD[2]); c.rect(x-4,y+h+5,w+8,1,WD[3]); c.rect(x-4,y+h+10,w+8,1,WD[0])
        for k in range(w+6):
            xx=x-3+k
            c.set(xx,y+h+4,[C('3f8a3a'),C('5aa84a'),C('2e6a30')][k%3])
            if k%4==1: c.set(xx,y+h+3,[RED[2],GOLD[2],C('e8e8f0')][(k//4)%3])
            if k%4==3: c.set(xx,y+h+4,[C('3f8a3a'),C('6ac05a')][(k//4)%2])
