# Taverna "Caneca Dourada" v2 - redesenhada do zero.
# Base de pedra lascada, andar de cima em tábuas com madeiramento pesado, venezianas verde-azuladas,
# telhado de telhas de madeira com empena central, porta em arco de pedra com degraus.
from pa import *

OUT=C('24141c')
S=[C('4e4640'),C('6e655c'),C('8c8274'),C('aa9f8b'),C('c8bca2')]      # pedra
MORT=C('2e272c')
W=[C('2a1812'),C('48281a'),C('683a22'),C('8a5230'),C('ae7240'),C('d09a58')]  # madeira (viga)
A=[C('5a301c'),C('7c4626'),C('9a5a2e'),C('b87a42'),C('d6a05c')]      # tábuas
I=[C('1a1822'),C('34323e'),C('58566a'),C('8a889c')]                   # ferro
G=[C('9a6418'),C('d89a2c'),C('f6cc54'),C('fff0a0')]                    # ouro
TEAL=[C('163c3c'),C('21605c'),C('2f8a80'),C('62bcaa')]                 # venezianas
R=[C('34161a'),C('5a2626'),C('84382c'),C('ac5636'),C('d27c48')]       # telhas
GLOW=[C('fff2b8'),C('ffd878'),C('eeaa44'),C('b86a2c')]

def hsh(*a):
    h=2166136261
    for v in a: h=((h^(v&0xffffffff))*16777619)&0xffffffff
    return h

def beam_h(c,x,y,w,t):
    c.rect(x,y,w,t,W[2]); c.rect(x,y,w,1,W[4]); 
    if t>=3: c.rect(x,y+1,w,1,W[3])
    c.rect(x,y+t-1,w,1,W[0])
    if t>=4: c.rect(x,y+t-2,w,1,W[1])
    for i in range(2,w,11): c.set(x+i,y+t//2,W[1]); c.set(x+i+1,y+t//2,W[3])
    for i in range(6,w,17): c.set(x+i,y+1,W[5])
def beam_v(c,x,y,t,h):
    c.rect(x,y,t,h,W[2]); c.rect(x,y,1,h,W[4]); c.rect(x+t-1,y,1,h,W[0])
    if t>=3: c.rect(x+1,y,1,h,W[3]); c.rect(x+t-2,y,1,h,W[1])
    for j in range(3,h,9): c.set(x+t//2,y+j,W[1])
    for j in range(7,h,19): c.set(x+1,y+j,W[5])
def brace(c,x0,y0,x1,y1,t=3):
    for k in range(t):
        c.line(x0+k,y0,x1+k,y1,W[2])
    c.line(x0,y0,x1,y1,W[4]); c.line(x0+t-1,y0,x1+t-1,y1,W[0])
def nail(c,x,y): c.set(x,y,I[3]); c.set(x+1,y,I[1])

def stone(c,x0,x1,y0,y1,seed=11,quoin=0):
    rng=random.Random(seed)
    c.rect(x0,y0,x1-x0,y1-y0,MORT)
    y=y0; row=0
    while y<y1:
        h=rng.choice([7,8,9])
        if y1-y-h<5: h=y1-y
        x=0; row_st=[]
        while x<32:
            w=rng.choice([9,11,13,15])
            if 32-x-w<8: w=32-x
            row_st.append((x,w)); x+=w
        for (sx,w) in row_st:
            if sx+w<=x0 or sx>=x1: continue
            tone=rng.choice([1,2,2,3,2,1,3])
            base=S[tone]
            warm=rng.choice([0,0,1])
            if warm: base=mix(base,C('b08a60'),0.22)
            for yy in range(y+1,y+h):
                for xx in range(sx+1,sx+w):
                    if not (x0<=xx<x1 and y0<=yy<y1): continue
                    col=base
                    ty=(yy-y)/h
                    col=sh(col,0.10-0.26*ty)
                    c.set(xx,yy,col)
            # arestas
            for xx in range(sx+1,sx+w):
                if x0<=xx<x1: c.set(xx,y+1,sh(base,0.34)); c.set(xx,y+h-1,sh(base,-0.42))
            for yy in range(y+1,y+h):
                if y0<=yy<y1:
                    if x0<=sx+1<x1: c.set(sx+1,yy,sh(base,0.22))
                    if x0<=sx+w-1<x1: c.set(sx+w-1,yy,sh(base,-0.34))
            # cantos arredondados
            for (cx,cy) in ((sx+1,y+1),(sx+w-1,y+1),(sx+1,y+h-1),(sx+w-1,y+h-1)):
                if x0<=cx<x1 and y0<=cy<y1: c.set(cx,cy,MORT)
            # manchas e rachaduras
            for k in range(rng.randrange(2,5)):
                px=sx+2+rng.randrange(max(1,w-4)); py=y+2+rng.randrange(max(1,h-4))
                if x0<=px<x1 and y0<=py<y1: c.set(px,py,sh(base,rng.choice([-0.18,0.16,-0.1])))
            if rng.random()<0.18:
                px=sx+3+rng.randrange(max(1,w-6)); py=y+2
                for j in range(rng.randrange(2,4)):
                    if x0<=px<x1 and y0<=py+j<y1: c.set(px+(j%2),py+j,sh(base,-0.38))
        y+=h; row+=1

def planks(c,x0,x1,y0,y1,seed):
    rng=random.Random(seed)
    for px in range(x0,x1,4):
        tone=rng.choice([1,2,2,3,2,1])
        base=A[tone]
        for x in range(px,min(px+4,x1)):
            for y in range(y0,y1):
                col=base
                u=x-px
                if u==0: col=sh(base,0.20)
                elif u==3: col=sh(base,-0.40)
                elif u==1: col=sh(base,0.05)
                if (x*3+y*7+seed)%17==0: col=sh(col,-0.12)
                if (x*5+y+seed)%23==0: col=sh(col,0.1)
                c.set(x,y,col)
        # emendas entre tábuas
        for j in range(2):
            jy=y0+rng.randrange(3,max(4,y1-y0-3))
            for x in range(px,min(px+4,x1)): c.set(x,jy,sh(base,-0.5))
        # pregos
        for ny in range(y0+4,y1-2,10): nail(c,px+1,ny) if rng.random()<0.6 else None

def shade_rect(c,x0,y0,x1,y1,amt):
    for y in range(y0,y1):
        for x in range(x0,x1):
            col=c.get(x,y)
            if col[3]: c.set(x,y,sh(col,amt))

def halo(c,cx,cy,r,col=GLOW[1],a=0.22):
    for y in range(int(cy-r),int(cy+r)+1):
        for x in range(int(cx-r),int(cx+r)+1):
            d=math.hypot(x-cx,y-cy)
            if d<r:
                q=c.get(x,y)
                if q[3]: c.set(x,y,mix(q,col,a*(1-d/r)**1.4))

def eave_shadow(c):
    grad=[-0.70,-0.62,-0.52,-0.42,-0.32,-0.22,-0.14,-0.08,-0.04]
    for y,g in enumerate(grad):
        for x in range(32): c.set(x,y,sh(c.get(x,y),g))
    c.rect(0,0,32,1,OUT)
    for x0 in (4,16,28 if False else 26):
        c.rect(x0,3,4,5,W[2]); c.rect(x0,3,1,5,W[4]); c.rect(x0+3,3,1,5,W[0]); c.rect(x0+1,8,2,1,W[0])

def jetty(c):
    """Viga mestra do sobrado (y=26..33 do tile de 96), com mísulas e sombra projetada na pedra."""
    beam_h(c,0,27,32,5)
    c.rect(0,32,32,1,W[0])
    for yy in range(33,46):
        t=(yy-33)/12
        for x in range(32):
            col=c.get(x,yy)
            if col[3]: c.set(x,yy,sh(col,-0.55*(1-t)**1.6))

def window_lower(c,rng):
    # arco de pedra de lintel + moldura + vidro quente + venezianas verde-azuladas
    x0,x1,y0,y1=9,22,50,80
    c.rect(x0-3,y0-4,x1-x0+7,y1-y0+8,OUT)
    # lintel de pedra com chave
    c.rect(x0-3,y0-4,x1-x0+7,4,S[3]); c.rect(x0-3,y0-4,x1-x0+7,1,S[4]); c.rect(x0-3,y0-1,x1-x0+7,1,S[0])
    c.rect(15,y0-4,3,4,S[4]); c.rect(15,y0-4,1,4,S[3]); c.rect(17,y0-4,1,4,S[1])
    # moldura de madeira
    c.rect(x0-2,y0,x1-x0+5,y1-y0+2,W[1]); c.rect(x0-2,y0,1,y1-y0+2,W[3]); c.rect(x1+2,y0,1,y1-y0+2,W[0])
    for y in range(y0+1,y1):
        for x in range(x0-1,x1+2):
            t=(y-y0)/(y1-y0)
            col=mix(GLOW[0],GLOW[2],t**0.8)
            if t>0.8: col=mix(col,GLOW[3],(t-0.8)*2)
            c.set(x,y,col)
    # chumbo: 2 colunas x 4 fileiras + cruz central mais grossa
    for y in range(y0+1,y1):
        for x in range(x0-1,x1+2):
            if (y-y0)%7==0 or (x-x0)%7==0: c.set(x,y,W[1])
    c.rect(15,y0+1,2,y1-y0-1,W[2]); c.rect(x0-1,y0+14,x1-x0+3,2,W[2])
    # silhueta de cortina e luz
    for y in range(y0+1,y0+14):
        for x in range(x0-1,x0+3):
            if x-x0+1<=3-(y-y0)//5: c.set(x,y,C('a83a3a') if (x+y)%3 else C('7a2630'))
    for k in range(4): c.set(x0+1+k,y0+5-k,GLOW[0]); c.set(x0+2+k,y0+5-k,(255,255,255,255)) if k<2 else None
    # peitoril de pedra
    c.rect(x0-4,y1+2,x1-x0+9,3,S[3]); c.rect(x0-4,y1+2,x1-x0+9,1,S[4]); c.rect(x0-4,y1+4,x1-x0+9,1,S[0])
    # venezianas
    for sx,mir in ((x0-9,0),(x1+4,1)):
        for y in range(y0-1,y1+2):
            for x in range(sx,sx+5):
                col=TEAL[2]; m=(y-y0)%3
                if m==0: col=TEAL[0]
                elif m==1: col=TEAL[3]
                if x==sx: col=sh(col,0.2)
                if x==sx+4: col=sh(col,-0.35)
                c.set(x,y,col)
        c.rect(sx,y0-2,5,1,OUT); c.rect(sx,y1+2,5,1,OUT)
        c.set(sx+(3 if not mir else 1),y0+14,G[2])
    # jardineira
    c.rect(x0-3,y1+5,x1-x0+7,5,W[3]); c.rect(x0-3,y1+5,x1-x0+7,1,W[5]); c.rect(x0-3,y1+9,x1-x0+7,1,W[0])
    for i in range(x0-2,x1+4,5): c.rect(i,y1+6,1,3,W[1])
    gs=[C('2a7a3a'),C('3f9a4a'),C('62bc5a')]; fl=[C('ee5060'),C('f8d848'),C('ffffff'),C('d070ee'),C('ff90b8')]
    for k in range(x1-x0+7):
        xx=x0-3+k
        c.set(xx,y1+4,gs[k%3]); c.set(xx,y1+3,gs[(k+1)%3]) if k%2 else None
        if k%2==0: c.set(xx,y1+3,fl[rng.randrange(5)]); 
        if k%4==0: c.set(xx,y1+2,fl[rng.randrange(5)])
    for k in range(5): c.set(x0-2+k*5,y1+10,gs[k%3])
    # luz derramando no peitoril + brilho discreto na parede
    halo(c,15.5,65,17,GLOW[1],0.12)

def window_upper(c):
    x0,x1,y0,y1=11,20,10,22
    c.rect(x0-2,y0-2,x1-x0+5,y1-y0+4,OUT)
    c.rect(x0-1,y0-1,x1-x0+3,y1-y0+3,W[1]); c.rect(x0-1,y0-1,x1-x0+3,1,W[4])
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            t=(y-y0)/(y1-y0)
            c.set(x,y,mix(GLOW[0],GLOW[2],t))
    c.rect(15,y0,1,y1-y0+1,W[2]); c.rect(x0,y0+5,x1-x0+1,1,W[2])
    for k in range(3): c.set(x0+1+k,y0+3-k if k<3 else y0,(255,255,255,255)) if k<3 else None
    c.rect(x0-3,y1+2,x1-x0+7,2,S[3]); c.rect(x0-3,y1+2,x1-x0+7,1,S[4])
    for sx in (x0-6,x1+3):
        for y in range(y0-1,y1+2):
            for x in range(sx,sx+3):
                col=TEAL[2]; m=(y-y0)%3
                if m==0: col=TEAL[0]
                elif m==1: col=TEAL[3]
                if x==sx: col=sh(col,0.2)
                if x==sx+2: col=sh(col,-0.35)
                c.set(x,y,col)

def lantern_post(c):
    # suporte de ferro com volutas e lampião aceso
    c.rect(9,44,3,40,I[1]); c.rect(9,44,1,40,I[3]); c.rect(11,44,1,40,I[0])
    c.rect(9,47,14,2,I[1]); c.rect(9,47,14,1,I[3])
    for k in range(6): c.set(10+k,49+k//2,I[1]); c.set(11+k,50+k//2,I[0])
    c.set(22,47,I[1]); c.set(23,48,I[1]); c.set(23,49,I[1]); c.set(22,50,I[1])        # volta
    c.rect(21,49,1,4,I[1])
    # corpo do lampião
    c.rect(18,53,7,2,I[1]); c.rect(19,52,5,1,I[3])
    c.rect(18,55,7,11,I[0])
    c.rect(19,56,5,9,GLOW[2]); c.rect(20,57,3,7,GLOW[1]); c.rect(21,58,1,5,GLOW[0])
    c.rect(18,66,7,2,I[1]); c.set(21,68,I[1]); c.set(21,69,I[0]); c.set(20,69,I[1]) if False else None
    halo(c,21.5,60,13,GLOW[1],0.26)
    for yy in range(40,47): 
        c.set(10,yy,I[3]) if yy%2 else None
    # peça de fixação inferior
    for k in range(3): c.rect(8+k*0,84-k,5,1,I[1])
    c.rect(8,84,5,2,I[1]); c.rect(8,84,5,1,I[3])

def firewood(c,rng):
    # pilha de lenha encostada à parede, com troncos vistos de topo
    x0,y0=5,66
    c.rect(x0-1,y0,23,26,W[0]); 
    for r in range(4):
        for k in range(5 if r%2==0 else 4):
            cx=x0+2+k*4+(0 if r%2==0 else 2); cy=y0+22-r*5
            c.ell(cx,cy,2.4,2.4,C('7a4a2a'))
            c.ell(cx,cy,1.6,1.6,C('c89252')); c.set(cx,cy,C('e0b070')); c.set(cx-1,cy-1,C('f0d090'))
            c.set(cx+2,cy+1,W[0]); c.set(cx,cy+2,W[0])
    c.rect(x0-1,y0+25,23,1,W[0])
    for y in range(y0,y0+26):
        c.set(x0-2,y,W[3]); c.set(x0+22,y,W[0])
    c.rect(x0-2,y0-1,26,1,W[4])
    # machado cravado
    c.rect(26,y0+8,1,10,W[3]); c.rect(24,y0+7,4,4,I[2]); c.rect(24,y0+7,4,1,I[3])

def barrels(c,rng):
    for bx,by,rr,h in ((9,66,8,24),(24,76,6,14)):
        for y in range(by,by+h):
            t=(y-by)/(h-1)
            half=int(rr*(0.88+0.2*math.sin(math.pi*t)))
            for x in range(bx-half,bx+half+1):
                u=(x-(bx-half))/(2*half+1)
                col=mix(C('d8964c'),C('5a3418'),u**0.9)
                if (x-bx+40)%4==0: col=sh(col,-0.22)
                c.set(x,y,col)
            c.set(bx-half-1,y,OUT); c.set(bx+half+1,y,OUT)
        for yy in (by+h//4,by+h*3//4):
            half=int(rr*(0.88+0.2*math.sin(math.pi*(yy-by)/(h-1))))
            for x in range(bx-half,bx+half+1):
                c.set(x,yy,I[1]); c.set(x,yy+1,I[3] if x%3==0 else I[2])
        c.ell(bx,by,rr*0.95,2.3,C('c8924c')); c.ell(bx,by,rr*0.7,1.4,C('8a5a2c'))
        c.rect(bx-1,by+h//2-2,3,3,G[1]); c.set(bx,by+h//2-1,G[3])
        for x in range(bx-rr-1,bx+rr+2): c.set(x,by+h,OUT)
    # saco de grãos
    c.ell(27,89,5,4,C('c8b080')); c.ell(26,88,4,3,C('e0cc9c')); c.rect(24,84,5,2,C('b89860')); c.set(26,84,W[1])
    # caixote
    c.rect(2,84,10,8,W[3]); c.rect(2,84,10,1,W[5]); c.rect(2,91,10,1,W[0]); c.rect(2,84,1,8,W[4]); c.rect(11,84,1,8,W[0])
    c.line(3,85,10,90,W[1]); c.line(10,85,3,90,W[1])

def tile_lower(kind,edge,frame=0):
    c=Cv(32,64)    # y=32..95 do tile
    return c

def facade(kind,edge,frame=0):
    rng=random.Random(5100+kind*17+edge*5)
    c=Cv(32,96)
    # ---- andar de cima: tábuas
    planks(c,0,32,6,28,300+kind)
    # ---- parte de baixo: pedra lascada
    stone(c,0,32,33,96,seed=11)
    # pedra de base maior e mais escura
    for y in range(90,96):
        for x in range(32):
            col=c.get(x,y)
            if col[3]: c.set(x,y,sh(col,-0.22-(y-90)*0.04))
    for x in range(32): c.set(x,95,OUT)
    # musgo e capim na base
    moss=[C('3e6e2c'),C('58903a'),C('7cb44c')]
    for x in range(32):
        if rng.random()<0.45: c.set(x,94-rng.randrange(3),rng.choice(moss))
        if rng.random()<0.15: c.set(x,93-rng.randrange(2),rng.choice(moss))
    # ---- conteúdo por tipo
    if kind==0:
        beam_v(c,14,6,4,22); brace(c,4,8,13,26); brace(c,19,26,27,8)
        for p in ((8,16),(23,16)): nail(c,*p)
        firewood(c,rng)
    elif kind==1:
        window_upper(c); window_lower(c,rng)
    elif kind==2:
        sign_upper(c); lantern_post(c)
    elif kind==3:
        loft_upper(c); barrels(c,rng)
    # ---- postes de canto (esq. de todo tile) + viga mestra + beiral
    beam_v(c,0,6,3,22); beam_v(c,0,34,3,57)
    for y in range(34,91):
        for x in (3,4):
            col=c.get(x,y)
            if col[3]: c.set(x,y,sh(col,-0.28 if x==3 else -0.14))
    for y in range(6,28):
        col=c.get(3,y); c.set(3,y,sh(col,-0.3))
    jetty(c)
    eave_shadow(c)
    # base de pedra do poste + cunhais nos cantos
    if edge==1:
        beam_v(c,0,6,6,22)
        for k,yy in enumerate(range(34,91,9)):
            w=7 if k%2==0 else 5
            c.rect(0,yy,w,8,S[3]); c.rect(0,yy,w,1,S[4]); c.rect(0,yy,1,8,S[4]); c.rect(w-1,yy,1,8,S[0]); c.rect(0,yy+7,w,1,S[0])
        c.rect(0,88,8,6,S[2]); c.rect(0,88,8,1,S[4]); c.rect(0,93,8,2,S[0])
    elif edge==2:
        beam_v(c,26,6,6,22)
        for k,yy in enumerate(range(34,91,9)):
            w=7 if k%2==0 else 5
            c.rect(32-w,yy,w,8,S[3]); c.rect(32-w,yy,w,1,S[4]); c.rect(32-w,yy,1,8,S[4]); c.rect(31,yy,1,8,S[0]); c.rect(32-w,yy+7,w,1,S[0])
        c.rect(24,88,8,6,S[2]); c.rect(24,88,8,1,S[4]); c.rect(24,93,8,2,S[0])
    return c

def sign_upper(c):
    # placa suspensa por braço de ferro (volutas e correntes)
    shade_rect(c,9,12,31,29,-0.22)
    c.rect(0,9,30,2,I[1]); c.rect(0,9,30,1,I[3]); c.rect(28,9,2,5,I[1])
    c.set(27,12,I[1]); c.set(26,13,I[1]); c.set(25,12,I[1])
    for cx in (7,23):
        for j in range(11,15):
            c.set(cx,j,I[3] if j%2 else I[1]); c.set(cx+1,j,I[1])
    c.rect(4,14,23,14,OUT)
    c.rect(5,15,21,12,W[1]); c.rect(6,16,19,10,A[2])
    for xx in range(6,25):
        if xx%5==0: c.rect(xx,16,1,10,A[0])
        if xx%5==1: c.rect(xx,16,1,10,A[3])
    c.rect(5,15,21,1,G[1]); c.rect(5,26,21,1,G[0]); c.rect(5,15,1,12,G[1]); c.rect(25,15,1,12,G[0])
    c.set(5,15,G[3])
    # caneca
    c.rect(11,19,8,7,C('ecb440')); c.rect(11,19,2,7,G[3]); c.rect(17,19,2,7,G[0])
    c.rect(19,20,3,1,G[2]); c.rect(21,20,1,4,G[2]); c.rect(19,23,3,1,G[1]); c.rect(11,25,8,1,G[0])
    c.rect(10,17,10,3,C('fff6e0')); c.rect(11,16,3,1,C('fff6e0')); c.rect(15,16,3,1,(255,255,255,255)); c.rect(10,19,10,1,C('d8c8a0'))
    for yy in (21,23): c.rect(12,yy,5,1,G[0])

def loft_upper(c):
    shade_rect(c,6,9,30,28,-0.0)
    c.rect(7,10,18,18,OUT)
    for x in range(8,24):
        for y in range(11,28):
            col=A[1]
            if (x-8)%4==0: col=A[0]
            elif (x-8)%4==1: col=A[2]
            c.set(x,y,col)
    c.rect(15,11,2,17,OUT)
    for y in (14,23): c.rect(8,y,16,2,I[1]); c.rect(8,y,16,1,I[2])
    c.rect(8,6,24,3,W[2]); c.rect(8,6,24,1,W[4])
    c.rect(26,9,1,5,I[1]); c.ell(26,15,2,2,I[1]); c.ell(26,15,0.9,0.9,I[3])
    c.line(26,17,26,27,C('c8a468')); c.rect(25,27,3,2,I[1])

# ------------------------------------------------------------------ PORTA 64x96
LO,HI,TOP,DBOT=18,45,42,89
WD=HI-LO+1
CX=(LO+HI+1)/2
SPRING=TOP+WD*0.5

def din(x,y,g):
    fx=x+.5; fy=y+.5; half=WD/2+g; tp=TOP-g
    if fy>=SPRING: return abs(fx-CX)<=half and y<=DBOT
    if fy<tp: return False
    t=(SPRING-fy)/(SPRING-tp)
    return abs(fx-CX)<=half*(1-t**2.0)

def door(frame):
    c=Cv(64,96); o=frame/4
    rng=random.Random(77)
    # fundo: mesma pedra e planks da fachada
    planks(c,0,64,6,28,350); stone(c,0,64,33,96,seed=11)
    beam_v(c,0,6,4,22); beam_v(c,60,6,4,22); beam_v(c,0,34,4,57); beam_v(c,60,34,4,57)
    # sacada com janela em arco (acima da porta)
    x0,x1,y0,y1=24,39,6,24
    c.rect(x0-3,y0-3,x1-x0+7,y1-y0+7,OUT)
    for y in range(y0-1,y1+1):
        for x in range(x0-1,x1+2):
            if y<y0+6 and ((x+.5-(x0+x1+1)/2)**2/((x1-x0+3)/2)**2+((y+.5-(y0+6))/7)**2)>1: continue
            t=(y-y0)/(y1-y0)
            col=mix(GLOW[0],GLOW[2],t)
            if (x-x0)%5==0 or (y-y0)%6==0: col=W[1]
            c.set(x,y,col)
    for k in range(4): c.set(x0+2+k,y0+8-k,(255,255,255,255))
    # cortinas
    for yy in range(y0+3,y1):
        c.rect(x0-1,yy,3,1,C('a83a3a') if yy%2 else C('7a2630')); c.rect(x1-1,yy,3,1,C('a83a3a') if yy%2 else C('7a2630'))
    # grade da sacada
    c.rect(x0-5,y1+1,x1-x0+11,1,I[2]); c.rect(x0-5,y1+5,x1-x0+11,1,I[1])
    for xx in range(x0-4,x1+5,3): c.rect(xx,y1+2,1,3,I[1])
    c.rect(x0-5,y1+6,x1-x0+11,2,W[3]); c.rect(x0-5,y1+6,x1-x0+11,1,W[5])
    halo(c,31.5,15,15,GLOW[1],0.16)
    # viga mestra com mísulas por cima da porta
    beam_h(c,0,27,64,5); c.rect(0,32,64,1,W[0])
    for bx in (14,46):
        for k in range(7):
            c.rect(bx+(k//2 if bx<32 else -(k//2)-3),33+k,5-(k//2) if True else 4,1,W[2] if k%2 else W[3])
    for yy in range(33,48):
        t=(yy-33)/14
        for x in range(64):
            col=c.get(x,yy)
            if col[3]: c.set(x,yy,sh(col,-0.5*(1-t)**1.6))
    # ---- arco de pedra com aduelas
    for y in range(TOP-8,DBOT+3):
        for x in range(LO-8,HI+9):
            if din(x,y,6) and not din(x,y,5): c.set(x,y,OUT)
            if din(x,y,5) and not din(x,y,0):
                col=S[3]
                ang=math.degrees(math.atan2(x+.5-CX,-(y+.5-SPRING)))
                if y<SPRING:
                    seg=int(((ang+90)%180)//15)
                    col=[S[3],S[2],S[3],S[4]][seg%4]
                    if (ang+90)%15<1.3: col=MORT
                else:
                    col=S[3] if ((y//7)%2==0) else S[2]
                    if y%7==0: col=MORT
                if not din(x,y,4): col=sh(col,0.18)
                elif din(x,y,1): col=sh(col,-0.32)
                c.set(x,y,col)
    # chave do arco com caneca dourada
    ix=int(CX)
    c.rect(ix-3,TOP-9,6,8,S[4]); c.rect(ix-3,TOP-9,6,1,(235,225,200,255)); c.rect(ix-3,TOP-2,6,1,S[1])
    c.rect(ix-2,TOP-7,4,4,G[1]); c.rect(ix-2,TOP-7,1,4,G[3]); c.rect(ix-2,TOP-8,4,1,C('fff6e0'))
    # ---- folhas da porta
    wl0=WD//2; wl=round(wl0*(1-0.86*o)); hh=DBOT-TOP+1
    def leaf(lx,ly):
        base=A[[1,2,1,3][(lx//4)%4]]
        col=base
        if lx%4==3: col=sh(base,-0.4)
        elif lx%4==0: col=sh(base,0.16)
        elif (lx*5+ly*2)%9==0: col=sh(base,-0.14)
        col=sh(col,(lx/wl0)*0.10-0.06)
        for sy in (int(hh*.22),int(hh*.55),int(hh*.84)):
            if sy<=ly<=sy+2 and lx<=wl0-5:
                col=I[3] if ly==sy else (I[1] if ly==sy+1 else I[0])
            ex=wl0-4
            if abs(lx-ex)<=1 and sy-1<=ly<=sy+3: col=I[1]
            if lx==ex and ly==sy+1: col=I[3]
        wx0=wl0//2-3
        if wx0<=lx<=wx0+6 and 8<=ly<=15:
            col=I[1] if (lx in (wx0,wx0+6) or ly in (8,15) or (lx-wx0)%2==0) else C('15101a')
            if col!=I[1] and 10<=ly<=11: col=GLOW[1]
        ky=int(hh*.5)+3
        dd=math.hypot(lx+.5-(wl0-6),ly+.5-ky)
        if dd<2.3: col=G[3] if dd<1.1 else G[2]
        elif 3.0<=dd<4.5 and ly>ky-1: col=G[0]
        if ly>hh-4: col=sh(col,-0.3)
        return col
    for y in range(TOP,DBOT+1):
        for x in range(LO,HI+1):
            if not din(x,y,0): continue
            ly=y-TOP; lx=-1; edge=False
            if x<LO+wl: lx=x-LO; edge=(x==LO+wl-1)
            elif x>HI-wl: lx=HI-x; edge=(x==HI-wl+1)
            if lx>=0:
                col=leaf(lx,ly)
                if edge: col=OUT
                if o>0: col=sh(col,-0.14*o)
                c.set(x,y,col)
            else:
                a=int(0xB8*(1-(y-TOP)/50)+0x30); a=max(0,min(255,a))
                c.set(x,y,(0x4a if (x+y)%5==0 else 0x28,0x2a if (x+y)%5==0 else 0x14,0x0a,a))
    # luz quente derramando pelo vão aberto
    if frame>0:
        for y in range(DBOT+1,DBOT+6):
            for x in range(LO+3,HI-2):
                q=c.get(x,y)
                if q[3]: c.set(x,y,mix(q,GLOW[1],0.22*frame/4*(1-(y-DBOT)/6)))
    # ---- degraus de pedra
    for (y,xa,xb,tone) in ((DBOT+1,LO-5,HI+6,3),(DBOT+4,LO-9,HI+10,2)):
        c.rect(xa,y,xb-xa,3,S[tone]); c.rect(xa,y,xb-xa,1,S[4]); c.rect(xa,y+2,xb-xa,1,S[0])
        for xx in range(xa+4,xb,9): c.set(xx,y+1,MORT)
        c.set(xa,y,OUT); c.set(xb-1,y,OUT)
    for x in range(LO-9,HI+10): c.set(x,95,OUT) if c.get(x,95)[3] else None
    # ---- lampiões de cada lado do arco
    for lxx,dirn in ((LO-12,1),(HI+8,-1)):
        c.rect(lxx+2,56,1,22,I[1])
        c.rect(lxx+2,56,4*dirn if dirn>0 else -4,1,I[1]) if False else None
        c.rect(lxx-1,62,7,2,I[1]); c.rect(lxx,61,5,1,I[3])
        c.rect(lxx,64,5,9,I[0]); c.rect(lxx+1,65,3,7,GLOW[2]); c.rect(lxx+2,66,1,5,GLOW[0])
        c.rect(lxx,73,5,1,I[1])
        halo(c,lxx+2.5,68,10,GLOW[1],0.22)
    # vasos e barril na soleira
    c.rect(2,80,7,9,C('b05a30')); c.rect(2,80,7,1,C('d87a40')); c.rect(7,81,1,8,C('7a3a1e')); c.rect(1,79,9,1,C('c8683a'))
    for p,cl in (((3,77),'ee5060'),((5,76),'f8d848'),((6,77),'ffffff'),((4,78),'3e8a3a'),((3,78),'3e8a3a'),((6,78),'3e8a3a'),((7,78),'3e8a3a')):
        c.set(p[0],p[1],C(cl))
    c.rect(54,81,7,8,W[3]); c.rect(54,81,1,8,W[4]); c.rect(54,84,7,1,I[1]); c.rect(54,87,7,1,I[1]); c.rect(54,81,7,1,W[5])
    eave_shadow_door(c)
    return c

def eave_shadow_door(c):
    grad=[-0.70,-0.62,-0.52,-0.42,-0.32,-0.22,-0.14,-0.08,-0.04]
    for y,g in enumerate(grad):
        for x in range(64): c.set(x,y,sh(c.get(x,y),g))
    c.rect(0,0,64,1,OUT)
    for x0 in (4,16,28,40,52):
        c.rect(x0,3,4,5,W[2]); c.rect(x0,3,1,5,W[4]); c.rect(x0+3,3,1,5,W[0])

# ------------------------------------------------------------------ TELHADO 470x222
BW,HGT,MT=470,222,16
YR=111
HIP=18
GCX=235        # centro da porta no telhado
GHW=50         # meia largura da empena central
GAP=YR+22      # ponta da empena central
GBASE=HGT-10

def inside(x,y):
    if y<MT or y>=HGT-1: return False
    inset=int(HIP*(1-(y-MT)/(YR-MT))**1.5+0.5) if y<YR else 0
    return 1+inset<=x<=BW-2-inset

def shingles(c,mask,ys,ye,rh,sw,back,seed,dark=0.0,x0=0,x1=BW):
    nrows=(ye-ys+rh-1)//rh
    for r in range(nrows-1,-1,-1):
        yb=ys+r*rh
        off=(r*3)%sw if True else 0
        for k in range(-1,BW//sw+2):
            h=hsh(r+(300 if back else 0),k,seed)
            sx=k*sw-off+((h>>5)%3)-1
            w=sw+((h>>9)%3)-1
            tone=[2,3,3,2,3,4,3,2][h%8]
            if (h>>12)%41==0: tone=1
            base=R[tone]
            if (h>>16)%31==0: base=mix(base,C('6a7a3a'),0.25)  # telha musgosa
            th=rh+2
            for t in range(th):
                for lx in range(w):
                    x=sx+lx; y=yb+t
                    if not (x0<=x<x1 and 0<=y<HGT) or not mask[y][x]: continue
                    col=sh(base,0.10-0.22*t/th)
                    if lx==0: col=sh(col,-0.2)
                    elif lx==1: col=sh(col,0.14)
                    elif lx==w-1: col=sh(col,-0.2)
                    if t==th-1: col=sh(base,-0.5)               # fresta entre fiadas
                    elif t==th-2: col=sh(col,-0.3)
                    elif t==0: col=sh(col,0.2)
                    if (lx*3+t*5+h)%13==0 and 1<t<th-3: col=sh(col,-0.05)   # veio da madeira
                    # ponta chanfrada
                    if t>=th-2 and (lx==1 or lx==w-1): col=sh(base,-0.35)
                    cx=sx+w/2
                    col=sh(col,0.07-0.18*(cx/BW)+dark)
                    c.set(x,y,col)

def roof():
    c=Cv(BW,HGT)
    rn=random.Random(2027)
    mask=[[inside(x,y) for x in range(BW)] for y in range(HGT)]
    shingles(c,mask,MT,YR,5,7,True,7,-0.20)
    shingles(c,mask,YR,HGT-1,7,8,False,3,0.0)
    # ---- sombra projetada abaixo da cumeeira
    for y in range(YR+3,YR+10):
        for x in range(2,BW-2):
            col=c.get(x,y)
            if col[3]: c.set(x,y,sh(col,-0.28*(1-(y-YR-3)/7)))
    # ---- cumeeira: viga de madeira com telhas de cobertura
    for x in range(2,BW-2):
        rim=x%15
        c.set(x,YR-5,sh(R[4],0.15)); c.set(x,YR-4,R[4]); c.set(x,YR-3,R[3]); c.set(x,YR-2,R[3])
        c.set(x,YR-1,R[2]); c.set(x,YR,R[1]); c.set(x,YR+1,R[0]); c.set(x,YR+2,OUT)
        if rim==14:
            for yy in range(YR-4,YR+1): c.set(x,yy,sh(c.get(x,yy),-0.4))
        if rim==0:
            for yy in range(YR-4,YR-1): c.set(x,yy,sh(c.get(x,yy),0.2))
    # ---- empena central sobre a porta
    gable(c,rn)
    # ---- empenas laterais: tábua de vento
    for y in range(MT,HGT-1):
        for x in range(BW):
            if not mask[y][x]: continue
            d=0
            while d<4 and 0<x-d-1 and x+d+1<BW and mask[y][x-d-1] and mask[y][x+d+1]: d+=1
            if d<3:
                if x<BW//2: col=[W[0],W[2],W[4]][d]
                else: col=[W[4],W[2],W[0]][d] if False else [W[0],W[1],W[2]][d]
                c.set(x,y,col)
    # ---- beiral: tábua de remate + caibros + gotejamento
    for x in range(1,BW-1):
        for dy,col in ((9,W[0]),(8,W[4]),(7,W[3]),(6,W[2]),(5,W[2]),(4,W[1]),(3,W[0]),(2,OUT)):
            c.set(x,HGT-dy,col)
        if x%32 in (0,1,2):
            for dy in range(3,9): c.set(x,HGT-dy,sh(c.get(x,HGT-dy),-0.22))
    for x in range(6,BW-8,32):
        c.rect(x,HGT-10,5,3,W[3]); c.rect(x,HGT-10,5,1,W[5]); c.rect(x+4,HGT-10,1,3,W[0])
        c.set(x+2,HGT-8,I[3])
    # ---- lucarnas laterais
    for fx in (0.15,0.85): dormer(c,int(BW*fx),YR+24)
    for chx in (103,376): chimney(c,chx)
    # ---- ponteiras da cumeeira + biruta
    for fx in (6,BW-7):
        c.rect(fx-2,YR-9,5,4,W[2]); c.rect(fx-2,YR-9,5,1,W[4]); c.rect(fx+2,YR-9,1,4,W[0])
        c.rect(fx,YR-16,1,7,I[1]); c.rect(fx-1,YR-12,3,1,I[1])
        c.set(fx,YR-17,G[3]); c.set(fx,YR-16,G[2]); c.set(fx-1,YR-15,G[2]); c.set(fx+1,YR-15,G[2])
    cx=BW//2+60
    c.rect(cx,YR-28,1,25,I[1]); c.rect(cx-2,YR-29,5,1,G[3]); c.set(cx,YR-31,G[3])
    c.rect(cx+1,YR-26,8,7,G[1]); c.rect(cx+1,YR-26,8,1,G[3]); c.rect(cx+1,YR-26,1,7,G[3])
    c.rect(cx+9,YR-25,2,4,G[1]); c.rect(cx+1,YR-20,8,1,G[0])
    c.rect(cx+1,YR-28,8,2,C('fff6e0')); c.set(cx+3,YR-29,(255,255,255,255)); c.set(cx+6,YR-29,(255,255,255,255))
    # ---- bandeirolas
    cols=[C('c0382f'),C('f2e6cc'),C('2f8c86'),C('e0a640')]
    x=16;k=0
    while x<BW-16:
        if abs(x-GCX)>GHW-4:
            yb=HGT-16+int(2*math.sin(x/17.0))
            c.set(x,yb,W[1])
            c.rect(x+1,yb,3,2,cols[k%4]); c.set(x+2,yb+2,sh(cols[k%4],-0.3))
        x+=6;k+=1
    c.outline(OUT)
    return c

def gable(c,rn):
    cx=GCX; ap=GAP; bs=GBASE; hw=GHW
    # planos laterais da empena (descem para trás até a cumeeira): mais escuros à direita
    def slope_y(x):   # linha do beiral da empena
        return ap+(abs(x-cx)/hw)*(bs-ap)
    mask=[[False]*BW for _ in range(HGT)]
    for x in range(cx-hw-14,cx+hw+15):
        if not (0<=x<BW): continue
        d=abs(x-cx)
        top=ap-26+(d/(hw+14))*18
        for y in range(int(top),int(slope_y(min(max(x,cx-hw),cx+hw)))+1):
            if 0<=y<HGT and not (cx-hw<=x<=cx+hw and y>=slope_y(x)-0): mask[y][x]=True
    # recorta o que fica dentro do rosto da empena (será desenhado depois)
    shingles(c,mask,ap-30,bs,6,7,False,91,-0.10,cx-hw-14,cx+hw+15)
    # rosto da empena: tábuas + enxaimel
    for x in range(cx-hw,cx+hw+1):
        d=abs(x-cx)
        ytop=int(slope_y(x))
        for y in range(ytop,bs):
            tone=[1,2,2,3,2,1][((x-cx+hw)//4)%6]
            base=A[tone]
            u=(x-cx+hw)%4
            col=base
            if u==0: col=sh(base,0.2)
            elif u==3: col=sh(base,-0.38)
            if (x*5+y*3)%19==0: col=sh(col,-0.12)
            c.set(x,y,col)
    # moldura: tábua de vento ao longo da inclinação (dupla, entalhada)
    for x in range(cx-hw-2,cx+hw+3):
        yb=slope_y(min(max(x,cx-hw),cx+hw))
        for k in range(-3,3):
            yy=int(yb)+k
            col=[W[0],W[4],W[3],W[2],W[1],W[0]][k+3]
            if x<cx and k==-2: col=W[5]
            c.set(x,yy,col)
    # madeiramento da empena (desenhado numa camada e recortado pelo triângulo)
    t=Cv(BW,HGT)
    beam_h(t,cx-hw,ap+36,2*hw,4)
    beam_v(t,cx-2,ap+4,4,bs-ap-4)
    beam_v(t,cx-hw+4,ap+4,4,bs-ap)
    beam_v(t,cx+hw-8,ap+4,4,bs-ap)
    brace(t,cx-hw+7,bs-3,cx-5,ap+40,3); brace(t,cx+hw-10,bs-3,cx+3,ap+40,3)
    for x in range(cx-hw,cx+hw+1):
        for y in range(int(slope_y(x))+4,bs):
            q=t.get(x,y)
            if q[3]: c.set(x,y,q)
    # janela redonda quente
    wcx,wcy=cx,ap+22
    for y in range(wcy-10,wcy+11):
        for x in range(wcx-10,wcx+11):
            d=math.hypot(x-wcx+.5,y-wcy+.5)
            if d<=10.5:
                if d>=9: col=W[0]
                elif d>=7: col=W[3] if x<wcx else W[1]
                else:
                    t=(y-wcy+7)/14
                    col=mix(GLOW[0],GLOW[2],max(0,min(1,t)))
                    ang=math.atan2(y-wcy+.5,x-wcx+.5)
                    if abs(x-wcx+.5)<0.8 or abs(y-wcy+.5)<0.8 or d<1.6: col=W[2]
                    if d>6 and (int((ang+math.pi)/(math.pi/4)+0.5)%2==0) and False: col=W[1]
                c.set(x,y,col)
    for k in range(3): c.set(wcx-5+k,wcy-4+k//1 if False else wcy-4-k+3,(255,255,255,255)) if False else None
    c.set(wcx-4,wcy-3,(255,255,255,255)); c.set(wcx-3,wcy-4,(255,255,255,255))
    halo(c,wcx,wcy,15,GLOW[1],0.2)
    # emblema de caneca em baixo
    # ponteira dourada
    c.rect(cx-1,ap-12,2,12,W[1]); c.ell(cx,ap-13,2.2,2.2,G[2]); c.set(cx-1,ap-14,G[3]); c.set(cx,ap-17,G[2]); c.rect(cx,ap-16,1,3,G[1])
    # sombra projetada pela empena sobre o telhado à direita
    for y in range(ap,bs):
        for x in range(cx+hw+1,cx+hw+9):
            col=c.get(x,y)
            if col[3] and y>slope_y(cx+hw)-6: c.set(x,y,sh(col,-0.28*(1-(x-cx-hw)/9)))

def dormer(c,dcx,dy0):
    for yy in range(dy0+6,dy0+34):
        for xx in range(dcx+11,dcx+20):
            col=c.get(xx,yy)
            if col[3] and xx-(dcx+11)<(yy-dy0-4)*0.35+4: c.set(xx,yy,sh(col,-0.3))
    c.rect(dcx-12,dy0+8,25,22,OUT)
    planks(c,dcx-11,dcx+11,dy0+9,dy0+30,dcx)
    c.rect(dcx-12,dy0+8,2,22,W[2]); c.rect(dcx+11,dy0+8,2,22,W[2]); c.rect(dcx-12,dy0+8,1,22,W[4]); c.rect(dcx+12,dy0+8,1,22,W[0])
    c.rect(dcx-7,dy0+12,15,12,OUT)
    for yy in range(dy0+13,dy0+23):
        for xx in range(dcx-6,dcx+7):
            t=(yy-dy0-13)/9
            c.set(xx,yy,mix(GLOW[0],GLOW[2],t))
    c.rect(dcx,dy0+13,1,10,W[2]); c.rect(dcx-6,dy0+17,13,1,W[2])
    c.set(dcx-5,dy0+14,(255,255,255,255)); c.set(dcx-4,dy0+14,(255,255,255,255))
    for sx in (dcx-11,dcx+8):
        for y in range(dy0+12,dy0+24):
            for x in range(sx,sx+3):
                col=TEAL[2]; m=(y-dy0)%3
                if m==0: col=TEAL[0]
                elif m==1: col=TEAL[3]
                if x==sx: col=sh(col,0.2)
                if x==sx+2: col=sh(col,-0.35)
                c.set(x,y,col)
    c.rect(dcx-9,dy0+24,19,2,S[3]); c.rect(dcx-9,dy0+24,19,1,S[4])
    c.rect(dcx-8,dy0+26,17,3,W[3]); c.rect(dcx-8,dy0+26,17,1,W[5])
    fl=[C('ee5060'),C('f8d848'),C('ffffff'),C('ff90b8')]; gs=[C('2a7a3a'),C('3f9a4a'),C('62bc5a')]
    for k in range(17):
        xx=dcx-8+k; c.set(xx,dy0+25,gs[k%3])
        if k%2==0: c.set(xx,dy0+24,fl[(k//2+dcx)%4])
    for yy in range(dy0-9,dy0+8):
        half=int(15*(yy-(dy0-9)+1)/17)
        for xx in range(dcx-half,dcx+half+1):
            r=(yy-dy0+9)//3
            col=[R[3],R[2],R[4]][((xx+(r%2)*2)//4+r)%3]
            if (yy-dy0+9)%3==2: col=sh(col,-0.4)
            elif (xx+(r%2)*2)%4==0: col=sh(col,-0.22)
            else: col=sh(col,0.05)
            if abs(xx-dcx)>=half-1: col=W[3] if xx<dcx else W[0]
            c.set(xx,yy,col)
    c.rect(dcx-14,dy0+7,29,2,W[0]); c.rect(dcx-14,dy0+7,29,1,W[4])
    c.set(dcx,dy0-11,G[3]); c.set(dcx,dy0-10,G[2])
    c.rect(dcx-11,dy0+30,23,2,(0,0,0,70))

def chimney(c,chx):
    ct=YR-34; cb=YR+6; cw=15
    st=S[2]
    for y in range(YR-18,YR+16):
        for x in range(chx+cw,chx+cw+8):
            col=c.get(x,y)
            if col[3] and (x-chx-cw)<(y-(YR-18))*0.25+2 and y>YR+1: c.set(x,y,sh(col,-0.32))
    for y in range(ct,cb):
        for x in range(chx,chx+cw):
            col=st
            if (y-ct)%5==0: col=MORT
            else:
                off=(((y-ct)//5)%2)*5
                if (x-chx+off)%10==0: col=MORT
                else:
                    col=sh(st,[0,0.12,-0.1,0.08][((x-chx+off)//10+(y-ct)//5)%4])
                    if x==chx+1: col=sh(col,0.18)
                    if x>=chx+cw-3: col=sh(col,-0.3)
            if y>ct+12 and (x*3+y)%9==0: col=mix(col,C('2a2a24'),0.3)
            c.set(x,y,col)
    c.rect(chx-2,ct-4,cw+4,4,S[3]); c.rect(chx-2,ct-4,cw+4,1,S[4]); c.rect(chx-2,ct-1,cw+4,1,S[0])
    c.rect(chx+2,ct-3,cw-4,2,C('14100e'))
    for px in (chx+3,chx+9):
        c.rect(px,ct-9,4,5,C('b8603c')); c.rect(px,ct-9,4,1,C('e0804e')); c.rect(px+3,ct-9,1,5,C('7a3a24')); c.rect(px-1,ct-10,6,1,C('c8683e'))
    for x in range(chx-3,chx+cw+3):
        c.set(x,cb,R[0]); 
        if x%2==0: c.set(x,cb-1,I[3])
