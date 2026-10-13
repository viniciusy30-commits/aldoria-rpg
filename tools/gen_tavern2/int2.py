# Interior v3: parede de fundo contínua (448x64), lareira de pedra, prateleiras, balcão, mesas, tonéis.
import ext as E
from ext import *
import interior as OI      # piso (texturas aprovadas) vem de gen_tavern
OUT2=C('24140c')
WOODS=[C('3a2212'),C('5a361c'),C('7a4a26'),C('9a6234'),C('bc8446'),C('d8a45c')]
BRASS=[C('7a5a1a'),C('c8962c'),C('f0c850'),C('fff0a0')]

def shadow(c,cx,cy,rx,ry,a=80):
    for y in range(int(cy-ry)-1,int(cy+ry)+2):
        for x in range(int(cx-rx)-1,int(cx+rx)+2):
            d=((x+.5-cx)/rx)**2+((y+.5-cy)/ry)**2
            if d<=1 and c.get(x,y)[3]==0: c.set(x,y,(18,8,4,int(a*(1-d*0.5))))

def woodgrain(c,x0,y0,w,h,base,seed=0,horiz=True):
    for y in range(y0,y0+h):
        for x in range(x0,x0+w):
            t=(y-y0)/max(1,h-1)
            col=sh(base,0.18-0.34*t)
            n=hsh(x//3,y,seed)%9
            if n==0: col=sh(col,-0.12)
            elif n==1: col=sh(col,0.08)
            c.set(x,y,col)

def mug(c,x,y,foam=True):
    c.rect(x,y,5,6,C('c8943c')); c.rect(x,y,1,6,C('f0c060')); c.rect(x+4,y,1,6,C('8a5a22'))
    c.rect(x,y+1,5,1,C('7a4a18')); c.rect(x,y+4,5,1,C('7a4a18'))
    if foam: c.rect(x,y-1,5,2,C('fff6e0')); c.set(x+1,y-2,(255,255,255,255)); c.set(x+3,y-2,C('fff6e0'))
    c.set(x+5,y+1,C('c8943c')); c.set(x+6,y+2,C('c8943c')); c.set(x+6,y+3,C('c8943c')); c.set(x+5,y+4,C('c8943c'))

def bottle(c,x,y,col,h=8):
    c.rect(x,y+3,3,h-3,col); c.rect(x,y+3,1,h-3,sh(col,0.35)); c.rect(x+2,y+3,1,h-3,sh(col,-0.4))
    c.rect(x+1,y,1,4,sh(col,-0.2)); c.set(x+1,y-1,C('b8844c')); c.set(x+1,y+h-1,sh(col,-0.5))
    c.set(x,y+4,(255,255,255,255) if h>6 else sh(col,0.5))

# ------------------------------------------------------------------ parede de fundo 448x64
def backstrip(fr):
    c=Cv(448,64)
    plaster(c,0,448,8,48,77)
    # sombra do teto e viga de teto
    beam_h(c,0,0,448,5)
    for y,g in enumerate([-0.45,-0.35,-0.25,-0.17,-0.1,-0.05]):
        for x in range(448): c.set(x,6+y,sh(c.get(x,6+y),g))
    # pilares de madeira
    for px in range(0,448,64): beam_v(c,px if px<444 else 444,5,4,45)
    beam_v(c,444,5,4,45)
    # lambri de carvalho nos 16 px de baixo
    for y in range(48,64):
        for x in range(448):
            base=C('5a361e'); u=x%8
            col=sh(base,-0.4) if u==0 else (sh(base,0.14) if u==1 else base)
            if (y*5+x*3)%17==0: col=sh(col,-0.1)
            c.set(x,y,col)
    beam_h(c,0,46,448,3); c.rect(0,60,448,4,C('2e1c0e')); c.rect(0,60,448,1,C('6a4424'))
    # janelas arqueadas grandes (centradas fora das prateleiras)
    for cx in (80,368):
        c.rect(cx-14,12,29,34,OUT2)
        for y in range(13,46):
            for x in range(cx-13,cx+14):
                if y<13+10 and ((x+.5-cx)**2/(13**2)+((y+.5-23)/10)**2)>1: continue
                col=mix(C('9fc4e8'),C('ffe6b0'),(y-13)/33)
                if (x-cx)%9==0 or (y-13)%9==0: col=W[1]
                c.set(x,y,col)
        c.rect(cx-16,44,33,3,S[3]); c.rect(cx-16,44,33,1,S[4])
        for yy in range(14,44):
            cc=C('a83a3a') if yy%2 else C('7a2630')
            for k in range(3+(yy-14)//9): c.set(cx-13+k,yy,cc); c.set(cx+13-k,yy,cc)
        c.rect(cx-17,11,35,2,W[1]); c.rect(cx-17,11,35,1,W[4])
    # lareira: peito de chaminé em pedra
    E.stone_wide(c,186,262,6,64,seed=31,dark=-0.05)
    for y in range(6,64):
        for x in (186,187,260,261): c.set(x,y,OUT2)
    for y in range(6,16):
        for x in range(186,262): c.set(x,y,sh(c.get(x,y),-0.5*(1-(y-6)/10)))
    c.rect(180,34,88,3,W[3]); c.rect(180,34,88,1,W[5]); c.rect(180,36,88,1,W[0])
    c.rect(180,37,88,2,W[1]); c.rect(180,39,88,1,OUT2)
    for bx in (186,254): c.rect(bx,39,6,4,W[2]); c.rect(bx,39,6,1,W[4])
    # em cima da lareira: escudo com espadas cruzadas e cabeça de javali
    c.line(205,12,243,31,C('c8c8d4')); c.line(243,12,205,31,C('c8c8d4')); c.line(206,12,244,31,C('8a8a98')); c.line(242,12,204,31,C('8a8a98'))
    c.rect(214,14,20,17,OUT2); c.rect(215,15,18,15,C('7a2a2a')); c.rect(215,15,18,1,BRASS[2]); c.rect(215,29,18,1,BRASS[1])
    c.rect(215,15,1,15,BRASS[2]); c.rect(232,15,1,15,BRASS[1])
    c.rect(221,19,6,6,C('6a4a2e')); c.rect(220,22,8,5,C('7a5636')); c.set(222,22,C('14101a')); c.set(226,22,C('14101a'))
    c.rect(222,25,4,1,C('3a2418')); c.set(220,26,C('f2e6cc')); c.set(227,26,C('f2e6cc'))
    # velas e canecas sobre a cornija
    for vx in (192,252): c.rect(vx,28,3,6,C('f2e8c8')); c.set(vx+1,26,C('ffe066')); c.set(vx+1,27,C('ff8a2d'))
    mug(c,202,29,False); mug(c,238,29,False)
    # arandelas com tocha animada
    for tx in (160,288):
        c.rect(tx-1,20,3,20,I[1]); c.rect(tx-1,20,1,20,I[3])
        c.rect(tx-3,28,7,2,I[1]); c.rect(tx-3,28,7,1,I[3])
        c.rect(tx-2,22,5,6,C('4a2a14')); c.rect(tx-2,22,5,1,I[3])
        fl=1 if fr else 0
        c.ell(tx+0.5+fl*0.4,19-fl,2.2,3.4+fl*0.4,C('ff8a2d')); c.ell(tx+0.5,19.6,1.2,2.2,C('ffe066'))
        halo(c,tx+0.5,20,22,GLOW[1],0.34)
    # troféus nas laterais: tapeçarias
    for bx in (136,296):
        c.rect(bx,10,16,2,I[1])
        for yy in range(12,40):
            cc=C('2f6a60') if (yy//4)%2 else C('27564e')
            for xx in range(bx+1,bx+15): c.set(xx,yy,cc)
        c.rect(bx+1,12,14,1,BRASS[2]); c.rect(bx+4,18,8,8,BRASS[1]); c.rect(bx+4,18,2,8,BRASS[3]); c.rect(bx+12,20,2,4,BRASS[2])
        for xx in range(bx+2,bx+15,3): c.set(xx,40,BRASS[2])
    # teia no canto, musgo no rodapé
    for k in range(8):
        c.set(8+k,8,C('d8d8e0')); c.set(8,8+k,C('d8d8e0'))
    c.line(8,8,15,15,C('b8b8c4')); c.line(8,12,12,12,C('c8c8d4'))
    return c

# ------------------------------------------------------------------ lareira (64x48 -> metades 32x48)
def hearth(frame):
    c=Cv(64,48)
    # base de pedra
    E.stone_wide(c,0,64,6,48,seed=47)
    c.rect(0,2,64,4,W[3]); c.rect(0,2,64,1,W[5]); c.rect(0,5,64,1,W[0])
    # boca da lareira em arco achatado
    x0,x1,y0,y1=9,55,15,43
    for y in range(y0-3,y1+1):
        for x in range(x0-3,x1+4):
            inside=(x0<=x<=x1 and y>=y0+ (0 if x0+6<=x<=x1-6 else 6-min(x-x0,x1-x)))
            if inside: c.set(x,y,C('0e0a0a'))
            elif x0-3<=x<=x1+3 and y0-3<=y<=y1 and not (x0<=x<=x1 and y>=y0): c.set(x,y,S[3] if (x+y)%7 else S[2])
    for x in range(x0-3,x1+4): c.set(x,y0-3,S[4]); c.set(x,y0-2,S[3])
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            q=c.get(x,y)
            if q==C('0e0a0a'):
                d=(x-32)/20
                glow=max(0,1-(y1-y)/20-(d*d)*0.5)
                c.set(x,y,mix(q,C('c4501c'),glow*0.8))
    # lenha e brasas
    for lx,ly,ang in ((16,40,0),(26,38,1),(36,40,0),(44,38,1)):
        c.rect(lx,ly,10,3,W[1]); c.rect(lx,ly,10,1,W[3]); c.rect(lx+9,ly,1,3,C('c8a060'))
    for x in range(14,50): c.set(x,41,C('e84a1a') if (x+frame)%3 else C('ffb040'))
    # chamas
    import math as m
    flames=[(24,5.5,17),(32,7,24),(40,5.5,18),(28,4,12),(36,4,13)]
    for fx,fw,fh in flames:
        fh2=fh+(3 if frame else 0)*(1 if fx%8 else -1)
        for y in range(int(y1-fh2),y1-1):
            t=(y1-y)/fh2
            half=fw*(1-t)**0.7*(1+0.15*m.sin(y*0.8+frame*2+fx))
            for x in range(int(fx-half),int(fx+half)+1):
                core=abs(x-fx)<half*0.5 and t<0.7
                col=C('ffe066') if core and t<0.45 else (C('ff9a2d') if core else C('e8531a'))
                if t>0.8: col=C('b8301a')
                c.set(x,y,col)
    for ex,ey in ((20,34),(44,33),(33,30),(27,36)):
        c.set(ex+(frame*2%3),ey-frame,C('ffd070'))
    # pedra de lareira e apoio de ferro
    c.rect(x0-5,43,x1-x0+11,5,S[4]); c.rect(x0-5,43,x1-x0+11,1,(235,225,200,255)); c.rect(x0-5,47,x1-x0+11,1,S[0])
    for xx in range(x0-4,x1+6,9): c.set(xx,45,MORT)
    c.rect(12,38,2,5,I[1]); c.rect(50,38,2,5,I[1]); c.set(12,37,I[3]); c.set(50,37,I[3])
    halo(c,32,30,34,GLOW[1],0.20)
    c.outline(OUT2)
    return c

# ------------------------------------------------------------------ prateleiras 32x48 (3 tipos)
def shelf(kind):
    c=Cv(32,48)
    c.rect(0,0,32,48,C('1c120a'))
    for y in range(0,48):
        for x in range(32):
            c.set(x,y,mix(C('2a1a10'),C('140c08'),y/48))
    for sy in (14,30,46):
        woodgrain(c,0,sy,32,3,WOODS[3],sy); c.rect(0,sy,32,1,WOODS[5]); c.rect(0,sy+2,32,1,WOODS[0])
    c.rect(0,0,32,3,WOODS[4]); c.rect(0,0,32,1,WOODS[5])
    for px in (0,30):
        c.rect(px,0,2,48,WOODS[2]); c.rect(px,0,1,48,WOODS[4]); c.rect(px+1,0,1,48,WOODS[0])
    rn=random.Random(kind*7+1)
    cols=[C('3a8a4a'),C('c8742c'),C('b02a3a'),C('3a5ab0'),C('7a3a9a'),C('d8c050')]
    if kind==0:   # canecas, pratos e jarras
        for x in range(3,29,6): mug(c,x,8,False) if (x//6)%2==0 else None
        for x in range(4,28,7): c.ell(x+2,26,3.2,1.2,C('d8d8e4')); c.ell(x+2,25,3,1,C('f0f0f8')) if x%2 else None
        for x in range(3,28,9):
            c.rect(x,19,6,9,C('9a5a2c')); c.rect(x,19,2,9,C('c88444')); c.rect(x+4,19,2,9,C('6a3a1a')); c.rect(x+1,18,4,1,C('3a2412'))
        for x in range(3,29,6): mug(c,x,40,x%2==0)
    elif kind==1: # garrafas coloridas
        for sy in (14,30,46):
            x=3
            while x<28:
                col=rn.choice(cols); h=rn.choice([8,9,10]); bottle(c,x,sy-h,col,h); x+=rn.choice([4,5])
    else:         # barris pequenos, queijos, pão e enchidos
        for x in (4,16):
            c.ell(x+5,8,5,4,C('c8944c')); c.rect(x,6,11,6,C('b87c3c')); c.rect(x,6,11,1,C('d8a45c')); c.rect(x,9,11,1,C('2e2e38')); c.rect(x+4,8,3,3,BRASS[2])
        for x in (4,17):
            c.ell(x+5,24,6,3.5,C('e8c050')); c.rect(x,20,11,5,C('e0b43c')); c.rect(x,20,11,1,C('f8de78')); c.set(x+3,23,C('b88a1c')); c.set(x+8,22,C('b88a1c'))
        for x in range(3,29,8):
            c.ell(x+3,42,3.4,2.4,C('d8a860')); c.rect(x,40,7,3,C('d8a860')); c.rect(x,40,7,1,C('f0c880'))
        for x in (10,22):
            c.rect(x,30-14,1,3,C('3a2418')); c.ell(x,20,2,3.2,C('c0583c')); c.ell(x,19,1,2,C('e08060'))
    for sy in (14,30):
        for x in range(2,30): c.set(x,sy+3,sh(c.get(x,sy+3),-0.5)) if c.get(x,sy+3)[3] else None
    c.outline(OUT2)
    return c

# ------------------------------------------------------------------ pilha de tonéis 32x48
def casks(kind):
    c=Cv(32,48); shadow(c,16,44,14,3,90)
    def barrel_side(cx,cy,rx,ry):
        for x in range(cx-rx,cx+rx+1):
            t=abs(x-cx)/rx; hh=int(ry*(1-0.18*t*t))
            for y in range(cy-hh,cy+hh+1):
                u=(x-(cx-rx))/(2*rx+1)
                col=mix(C('d8964c'),C('5a3418'),u**0.9)
                if (y-cy+60)%5==0: col=sh(col,-0.2)
                c.set(x,y,col)
        for rr in (0.62,0.2,-0.2,-0.62):
            x=int(cx+rx*rr)
            hh=int(ry*(1-0.18*rr*rr))
            for y in range(cy-hh,cy+hh+1): c.set(x,y,I[1]); c.set(x+1,y,I[3]) if y%3==0 else c.set(x+1,y,I[2])
        c.rect(cx-2,cy-1,4,3,BRASS[1]); c.set(cx-2,cy-1,BRASS[3])
    if kind==0:
        c.rect(1,36,30,3,WOODS[1]); c.rect(1,36,30,1,WOODS[4]); c.rect(3,39,3,6,WOODS[0]); c.rect(26,39,3,6,WOODS[0])
        barrel_side(16,27,14,9); barrel_side(10,10,9,7) if False else None
        c.rect(14,35,4,4,BRASS[1]); c.rect(15,39,2,4,BRASS[2]); c.set(15,44,C('ffe08a'))
        for k in range(3): c.rect(11+k*5,16,4,1,WOODS[0]) if False else None
        barrel_side(10,10,9,7); barrel_side(23,12,8,6)
    else:
        for (cx,cy) in ((16,36),):
            c.ell(cx,cy+1,12,8,C('3a2412'))
            for y in range(cy-8,cy+9):
                t=(y-(cy-8))/16; hf=int(12*(0.92+0.1*math.sin(math.pi*t)))
                for x in range(cx-hf,cx+hf+1):
                    u=(x-(cx-hf))/(2*hf+1); col=mix(C('d8964c'),C('5a3418'),u**0.9)
                    if (x-cx+40)%4==0: col=sh(col,-0.2)
                    c.set(x,y,col)
            for yy in (cy-5,cy+4): 
                for x in range(cx-12,cx+13): c.set(x,yy,I[1]); c.set(x,yy+1,I[3] if x%3==0 else I[2])
            c.rect(cx-1,cy-1,3,3,BRASS[2])
            c.ell(cx,cy-8,11,3,C('c8924c')); c.ell(cx,cy-8,8,2,C('8a5a2c')); c.ell(cx,cy-8,2,0.9,C('3a2412'))
        c.rect(6,2,20,0,OUT2)
        c.rect(11,14,10,12,C('b87c3c')); c.rect(11,14,10,1,C('d8a45c')); c.rect(11,19,10,1,I[1]); c.rect(15,18,2,3,BRASS[2]);
        c.ell(16,14,5,2,C('c8924c'))
    c.outline(OUT2)
    return c

# ------------------------------------------------------------------ balcão 32x40 (end-esq, meio, end-dir, torneiras, canecas)
def counter(part,deco=0):
    c=Cv(32,40); x0=3 if part==0 else 0; x1=29 if part==2 else 32
    shadow(c,16,38,15,2,70)
    # tampo (visto de cima/frente) y 14..22
    for y in range(14,23):
        for x in range(x0,x1):
            t=(y-14)/8
            col=mix(WOODS[4],WOODS[2],t)
            if y==14: col=WOODS[5]
            if hsh(x//4,y//2,5)%8==0: col=sh(col,-0.1)
            if (x+3)%16==0: col=sh(col,-0.25)
            c.set(x,y,col)
    c.rect(x0,22,x1-x0,1,WOODS[0])
    # frente com painéis
    for y in range(23,37):
        for x in range(x0,x1):
            c.set(x,y,sh(WOODS[2],0.12-0.3*(y-23)/14))
    for px in range(x0+2,x1-9,13):
        c.rect(px,25,10,10,sh(WOODS[2],-0.42)); c.rect(px+1,26,8,8,sh(WOODS[2],0.0))
        c.rect(px+1,26,8,1,sh(WOODS[2],0.28)); c.rect(px+1,26,1,8,sh(WOODS[2],0.16))
        c.rect(px+4,29,2,2,BRASS[2]); c.set(px+4,29,BRASS[3])
    c.rect(x0,35,x1-x0,3,C('2e1c0e')); c.rect(x0,35,x1-x0,1,BRASS[1]); c.rect(x0,34,x1-x0,1,sh(BRASS[1],-0.3))
    if part==0: c.rect(x0,14,2,24,WOODS[5])
    if part==2: c.rect(x1-2,14,2,24,WOODS[0])
    # itens em cima
    if deco==1:   # torre de chopes
        c.rect(10,4,12,10,C('3a2412')); c.rect(10,4,12,1,WOODS[4]); c.rect(10,4,1,10,WOODS[3])
        c.rect(9,3,14,2,BRASS[1]); c.rect(9,3,14,1,BRASS[3])
        for tx in (12,16,20): c.rect(tx,8,2,3,BRASS[2]); c.rect(tx,11,2,3,BRASS[1]); c.set(tx,11,BRASS[3])
        mug(c,2,9); mug(c,24,9)
    elif deco==2:  # canecas e uma garrafa
        mug(c,4,9); mug(c,12,9,False); bottle(c,21,4,C('3a8a4a'),10); mug(c,26,9) if x1==32 else None
    elif deco==3:  # livro-caixa e vela
        c.rect(6,9,12,5,C('6a2a2a')); c.rect(6,9,12,1,C('9a4a4a')); c.rect(6,13,12,1,C('3a1212')); c.rect(7,10,10,1,BRASS[2])
        c.rect(23,6,3,8,C('f2e8c8')); c.set(24,4,C('ffe066')); c.set(24,5,C('ff8a2d')); c.rect(21,13,7,1,I[1])
    elif part==0: mug(c,10,9)
    elif part==2: mug(c,15,9)
    c.outline(OUT2); return c

# ------------------------------------------------------------------ mesa comprida 32x32 (metades)
def table(v,variant=0):
    c=Cv(32,32); x0=3 if v==0 else 0; x1=32 if v==0 else 29
    shadow(c,16,28,15,3,80)
    # cavaletes
    lx=(6,22) if v==0 else (9,25)
    for l in lx:
        c.rect(l,22,4,8,WOODS[1]); c.rect(l,22,1,8,WOODS[3]); c.rect(l+3,22,1,8,WOODS[0])
    c.rect(x0+2,26,x1-x0-4,2,WOODS[1]); c.rect(x0+2,26,x1-x0-4,1,WOODS[3])
    # tampo
    for y in range(11,22):
        for x in range(x0,x1):
            t=(y-11)/10
            col=mix(WOODS[4],WOODS[2],t**0.8)
            if (y-11)%4==3: col=sh(col,-0.28)
            elif (y-11)%4==0: col=sh(col,0.14)
            if hsh(x//5,y,v*3+variant)%9==0: col=sh(col,-0.1)
            c.set(x,y,col)
    c.rect(x0,11,x1-x0,1,WOODS[5]); c.rect(x0,21,x1-x0,2,WOODS[0]); c.rect(x0,19,x1-x0,2,sh(WOODS[1],0.05))
    if v==0: c.rect(x0,11,1,12,WOODS[5])
    else: c.rect(x1-1,11,1,12,WOODS[0])
    # toalha de mesa vermelha ao centro
    c.rect(x0+ (0 if v==1 else 6),13,x1-x0-(6 if v==0 else 6),5,C('9a2a2a')) if False else None
    for x in range(x0 if v==1 else x0+3,x1 if v==0 else x1-3):
        c.set(x,14,C('b83a3a')); c.set(x,15,C('9a2a2a')); c.set(x,16,C('7a1e22')); c.set(x,17,C('c8a040') if x%4==0 else C('7a1e22'))
    # itens
    if variant==0:
        if v==0: mug(c,9,7); mug(c,18,9,False); c.ell(25,12,4,2,C('e8e8f0')); c.rect(22,9,6,3,C('d8a860')); c.rect(22,9,6,1,C('f0c880'))
        else: c.rect(5,5,3,7,C('f2e8c8')); c.set(6,3,C('ffe066')); c.set(6,4,C('ff8a2d')); c.rect(3,11,7,1,I[1]); mug(c,13,8); c.ell(21,12,3.5,1.6,C('e8e8f0')); c.rect(18,10,6,2,C('d8c040'))
    else:
        if v==0: c.ell(10,11,4,2,C('e8e8f0')); c.rect(6,8,8,3,C('c0583c')); c.rect(6,8,8,1,C('e08060')); mug(c,19,7); mug(c,25,9,False)
        else: mug(c,6,8); c.rect(11,4,6,8,C('3a8a4a')); c.rect(11,4,2,8,C('7ac88a')); c.rect(13,2,2,3,C('3a8a4a')); c.ell(23,12,3.5,1.6,C('e8e8f0')); c.rect(20,9,6,3,C('d8a860'))
    c.outline(OUT2); return c

def stool():
    c=Cv(32,32); shadow(c,16,27,9,3,85)
    for lx in (10,15,20):
        c.rect(lx,20,2,8,WOODS[1]); c.rect(lx,20,1,8,WOODS[3])
    c.ell(16,18,9,5.5,WOODS[1]); c.ell(16,17,9,5,WOODS[4]); c.ell(16,16.5,6.5,3.4,WOODS[5])
    for k in range(5): c.set(11+k*2,15+(k%2),sh(WOODS[4],-0.25))
    c.ell(16,17,9,5,(0,0,0,0)) if False else None
    c.outline(OUT2); return c

if __name__=='__main__':
    import io
    P=Cv(448+40,64+48*3+40*2+40)
    P.blit(backstrip(0),0,0)
    P.blit(hearth(0),192,64); P.blit(hearth(1),192+64,64)
    for i in range(3): P.blit(shelf(i),i*32,64)
    for i in range(2): P.blit(casks(i),100+i*32,64)
    P.blit(counter(0),0,124); P.blit(counter(1,1),32,124); P.blit(counter(1,0),64,124); P.blit(counter(1,2),96,124); P.blit(counter(1,3),128,124); P.blit(counter(2),160,124)
    P.blit(table(0),200,130); P.blit(table(1),232,130); P.blit(table(0,1),270,130); P.blit(table(1,1),302,130); P.blit(stool(),340,130)
    P.save('/home/claude/out/int2.png',3)
