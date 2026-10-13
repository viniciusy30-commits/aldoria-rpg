from pa import *
import facade as F
OUT=C('2a1a0e')

def floor(v):
    c=Cv(32,32); rn=random.Random(300+v)
    tones=[C('7a4e2c'),C('6a4224'),C('86582e'),C('5e3a20'),C('7e5230')]
    bh=8
    for r in range(4):
        y=r*bh; x=(rn.randrange(4,28) if True else 0)
        off=[0,17,9,23][(r+v)%4]
        # duas tábuas por fileira com junta em 'off'
        segs=[(0,off),(off,32)] if off>0 else [(0,32)]
        for a,b in segs:
            base=rn.choice(tones)
            for yy in range(y,y+bh):
                for xx in range(a,b):
                    col=base
                    if yy==y: col=sh(base,-0.42)
                    elif yy==y+1: col=sh(base,0.16)
                    elif yy==y+bh-1: col=sh(base,-0.2)
                    elif (xx*3+yy*11+r*7)%17==0: col=sh(base,-0.12)
                    elif (yy-y)%3==2 and (xx+r)%9<5: col=sh(base,-0.06)
                    c.set(xx,yy,col)
            if a>0:
                for yy in range(y,y+bh): c.set(a,yy,sh(base,-0.5))
            # pregos
            c.set(a+2 if a>0 else 2,y+4,C('2a1a12')); c.set(b-3,y+4,C('2a1a12'))
            c.set(a+2 if a>0 else 2,y+3,sh(base,0.3)); c.set(b-3,y+3,sh(base,0.3))
        # nó de madeira
        if rn.random()<0.7:
            kx=rn.randrange(6,26); c.ell(kx,y+4,2,1.3,sh(tones[3],-0.15)); c.set(kx,y+4,sh(tones[3],-0.4))
    # manchas de cerveja
    if v%2==1:
        for i in range(8): c.set(rn.randrange(32),rn.randrange(32),sh(C('5a3a20'),-0.2))
    return c

def backwall(kind,edge,frame):
    c=Cv(32,64)
    PL=C('e6cf9c')
    c.rect(0,0,32,64,PL)
    for y in range(0,64):
        for x in range(32):
            t=(y)/64
            c.set(x,y,mix(C('e8d4a2'),C('c6a874'),t*0.7))
    rn=random.Random(80+kind)
    for i in range(130): c.set(rn.randrange(32),rn.randrange(8,40),sh(PL,rn.choice([-0.1,0.08])))
    # sombra do teto
    for y,g in enumerate([-0.5,-0.4,-0.3,-0.22,-0.15,-0.1,-0.06,-0.03]):
        for x in range(32): c.set(x,y,sh(c.get(x,y),g))
    F.beam_h(c,0,0,32,4)       # viga de teto
    F.beam_h(c,0,6,32,3)       # frisa
    # lambri escuro
    for y in range(40,64):
        for x in range(32):
            base=C('5a361e')
            if (x%8)==0: col=sh(base,-0.4)
            elif (x%8)==1: col=sh(base,0.14)
            else: col=base
            if (y*5+x*3)%17==0: col=sh(col,-0.1)
            c.set(x,y,col)
    for px in (1,17):  # painéis
        c.rect(px,45,14,14,sh(C('5a361e'),-0.3)); c.rect(px+1,46,12,12,C('6e4424'))
        c.rect(px+1,46,12,1,C('8a5a30')); c.rect(px+1,46,1,12,C('7e5028'))
        c.rect(px+1,57,12,1,sh(C('6e4424'),-0.35)); c.rect(px+12,46,1,12,sh(C('6e4424'),-0.35))
        c.set(px+7,52,F.GOLD); 
    F.beam_h(c,0,40,32,3)      # guarda-pó/rodapé alto
    c.rect(0,60,32,4,C('3a2412')); c.rect(0,60,32,1,C('6a4424'))
    F.beam_v(c,0,9,3,32); F.beam_v(c,29,9,3,32)
    if kind==0:
        F.beam_v(c,14,9,4,32)
        F.brace(c,3,10,13,38); F.brace(c,19,38,28,10)
        g=[C('2f7a3a'),C('3f9a4a'),C('8cc24a')]
        for x in range(3,29):
            y=11+int(3*math.sin((x-3)/25*math.pi))
            c.set(x,y,g[x%3]); c.set(x,y+1,g[(x+1)%3])
            if x%5==0: c.set(x,y+2,C('c8e070'))
        for mx in (8,20): 
            c.set(mx,15,F.IRON); c.rect(mx-1,16,4,4,F.GOLD); c.rect(mx-1,16,1,4,F.GOLDH); c.set(mx+3,17,F.GOLD)
    elif kind==1:
        F.window(c,rn,8,23,12,34,False)
        for y in range(12,35):
            for x in range(9,23):
                if c.get(x,y)[2]>180: pass
        # cortinas
        for y in range(12,30):
            for k in range(3):
                col=C('8a2a2a') if k%2==0 else sh(C('8a2a2a'),-0.25)
                c.set(8+k,y,col); c.set(23-k,y,col)
        c.rect(6,11,20,2,F.BMD)
        c.rect(6,35,20,2,C('d0b88a'))
    elif kind==2:
        for y in range(8,42):
            for x in range(2,30):
                d=math.hypot(x-16,y-22)
                if d<13: c.set(x,y,mix(c.get(x,y),C('ffc860'),0.35*(1-d/13)))
        c.rect(12,24,8,2,F.IRON); c.rect(14,26,4,6,F.IRON); c.rect(15,32,2,2,F.IRON)
        c.rect(14,20,4,4,C('f2e8c8')); c.rect(14,20,1,4,(255,255,255,255))
        if frame==0: c.ell(16,17,1.8,2.8,C('ff8a2d'))
        else: c.ell(16.5,16.7,1.6,3,C('ff8a2d'))
        c.ell(16,17.6,0.9,1.6,C('ffe066'))
    elif kind==3:
        # troféu: javali sobre escudo
        c.rect(6,10,20,28,F.BMD); c.rect(7,11,18,26,C('7a2a2a'))
        c.rect(7,11,18,1,F.GOLD); c.rect(7,36,18,1,F.GOLD); c.rect(7,11,1,26,F.GOLD); c.rect(24,11,1,26,F.GOLD)
        c.line(9,14,22,34,C('c8c8d4')); c.line(22,14,9,34,C('c8c8d4'))
        c.line(10,14,23,34,C('8a8a98')); c.line(21,14,8,34,C('8a8a98'))
        c.rect(13,18,7,7,C('6a4a2e')); c.rect(12,21,9,5,C('7a5636')); c.rect(12,21,9,1,C('8a6642'))
        c.set(14,21,C('14101a')); c.set(18,21,C('14101a'))
        c.rect(14,24,5,2,C('3a2418'))
        for p in ((12,26),(20,26),(12,27),(20,27)): c.set(p[0],p[1],C('f2e6cc'))
        c.set(13,17,C('5a3a22')); c.set(19,17,C('5a3a22'))
    if edge==1: F.beam_v(c,0,0,3,64) if False else c.rect(0,0,3,64,C('2c1a10')); 
    if edge==2: c.rect(29,0,3,64,C('2c1a10'))
    return c

# ---------------- móveis 32x32
def outl(c): c.outline(OUT)

def table(v):  # v 0 = metade esquerda, 1 = metade direita
    c=Cv(32,32)
    wood=C('a46c38'); wl=C('c88a4c'); wd=C('6e4424')
    x0=3 if v==0 else 0; x1=32 if v==0 else 29
    for y in range(12,22):
        for x in range(x0,x1):
            col=wood
            if (y-12)%3==2: col=sh(wood,-0.2)
            elif (y-12)%3==0: col=wl
            if (x*7+y)%13==0: col=sh(col,-0.08)
            c.set(x,y,col)
    c.rect(x0,12,x1-x0,1,sh(wl,0.2))
    for y in range(22,27):
        for x in range(x0,x1): c.set(x,y,sh(wood,-0.38 if y<26 else -0.55))
    if v==0:
        c.rect(x0,12,1,15,sh(wl,0.15)); c.rect(4,27,3,5,wd); c.rect(4,27,1,5,sh(wd,0.2))
    else:
        c.rect(x1-1,12,1,15,sh(wd,-0.2)); c.rect(25,27,3,5,wd); c.rect(27,27,1,5,sh(wd,-0.3))
    # objetos
    def mug(x,y):
        c.rect(x,y,4,5,C('c8943c')); c.rect(x,y,1,5,C('e8b858')); c.rect(x+3,y,1,5,C('8a5a22'))
        c.rect(x,y-1,4,2,C('fff6e0')); c.set(x+4,y+1,C('c8943c')); c.set(x+4,y+2,C('c8943c')); c.set(x+1,y-2,(255,255,255,255))
    if v==0:
        mug(8,13); mug(17,16)
        c.ell(25,17,4,2,C('e8e8f0')); c.ell(25,16.5,2.5,1.2,C('d0d0dc'))
        c.rect(23,13,4,3,C('d8a860')); c.rect(23,13,4,1,C('f0c880'))   # pão
    else:
        c.rect(5,12,2,3,C('f2e8c8')); c.set(5,11,C('ffe066')); c.set(5,10,C('ff8a2d')); c.set(6,11,C('ff8a2d'))
        mug(12,16); mug(20,13)
        c.rect(15,13,5,2,C('9a2a2a')); c.rect(15,13,5,1,C('c84a4a'))  # maçã/queijo?
    outl(c); return c

def stool():
    c=Cv(32,32)
    wl=C('b87a40'); wd=C('7a4a24')
    c.ell(16,22,8,4,sh(wd,-0.4))
    for lx in (10,15,21): c.rect(lx,22,2,8,wd); c.rect(lx,22,1,8,wl)
    c.ell(16,18,8.5,5,wd)
    c.ell(16,17,8.5,4.5,wl)
    c.ell(16,17,6,3,sh(wl,0.12))
    for k in range(4): c.set(11+k*2,16+(k%2),sh(wl,-0.25))
    c.rect(9,17,14,1,sh(wl,0.2)) if False else None
    outl(c); return c

def counter(v): # 0 esquerda, 1 meio, 2 direita
    c=Cv(32,32)
    top=C('5a361c'); tl=C('8a5a30'); fr=C('7a4a26')
    x0=2 if v==0 else 0; x1=30 if v==2 else 32
    for y in range(6,14):
        for x in range(x0,x1):
            col=mix(C('6a4222'),C('4a2c16'),(y-6)/8)
            if (y-6)==0: col=sh(tl,0.2)
            elif (x*3+y*5)%11==0: col=sh(col,0.1)
            c.set(x,y,col)
    c.rect(x0,6,x1-x0,1,C('b8884c'))
    for y in range(14,31):
        for x in range(x0,x1):
            col=fr
            if y==14: col=sh(fr,0.25)
            c.set(x,y,col)
    # painéis em arco
    for px in range(x0+2,x1-6,12):
        c.rect(px,17,10,11,sh(fr,-0.4)); c.rect(px+1,18,8,10,sh(fr,0.05))
        for k in range(3): c.set(px+2+k,18,sh(fr,-0.4)) if False else None
        c.rect(px+1,18,8,1,sh(fr,0.22)); c.rect(px+1,18,1,10,sh(fr,0.12))
        c.rect(px+4,22,2,2,F.GOLD)
    c.rect(x0,29,x1-x0,2,C('3a2412')); c.rect(x0,28,x1-x0,1,F.GOLD)   # pedal de latão
    if v==0: c.rect(x0,6,1,25,sh(tl,0.1))
    if v==2: c.rect(x1-1,6,1,25,sh(C('3a2412'),-0.3))
    def mug(x,y):
        c.rect(x,y,4,5,C('c8943c')); c.rect(x,y,1,5,C('e8b858')); c.rect(x,y-1,4,2,C('fff6e0')); c.set(x+4,y+1,C('c8943c'));c.set(x+4,y+2,C('c8943c'))
    if v==0: mug(8,3); 
    if v==1:
        mug(4,3); mug(22,3)
        c.rect(13,0,6,4,C('4a2a14')); c.rect(15,4,2,3,F.GOLD)   # torneira de chope
    if v==2: mug(18,3); c.rect(8,1,3,6,C('3a8a4a')); c.rect(8,1,3,1,C('9ae0a8'))
    outl(c); return c

def cask():
    c=Cv(32,32)
    for y in range(3,29):
        t=(y-3)/25; half=int(11*(0.8+0.28*math.sin(math.pi*t)))
        for x in range(16-half,16+half+1):
            u=(x-(16-half))/(2*half+1)
            col=mix(C('c48848'),C('5a3818'),u)
            if (x-16+40)%5==0: col=sh(col,-0.18)
            c.set(x,y,col)
    for y in (8,15,22):
        t=(y-3)/25; half=int(11*(0.8+0.28*math.sin(math.pi*t)))
        for x in range(16-half,16+half+1): c.set(x,y,C('2e2e38')); c.set(x,y+1,C('6a6a78')) if x%3==0 else None
    c.rect(14,13,4,3,F.GOLD); c.rect(15,16,2,4,F.GOLD); c.rect(14,13,1,3,F.GOLDH)
    c.rect(13,22,6,5,C('c8943c')); c.rect(13,22,6,1,C('fff6e0'))
    c.rect(11,28,10,2,(0,0,0,90))
    outl(c); return c

if __name__=='__main__':
    p=Cv(32*16,32*7)
    for i in range(4):
        for r in range(4): p.blit(floor(i),i*32,r*32) if False else None
    # piso 8x3 variados
    for y in range(3):
        for x in range(8): p.blit(floor((x+y*3)%4),x*32,y*32)
    # móveis
    xs=0
    for sp in (table(0),table(1),stool(),counter(0),counter(1),counter(2),cask()):
        p.blit(sp,xs,96); xs+=32
    for i,(k,e) in enumerate([(0,1),(1,0),(2,0),(3,0),(0,2)]):
        p.blit(backwall(k,e,0),i*32+8*32,0)
    # fileira de bar
    p.blit(counter(0),0,160);p.blit(counter(1),32,160);p.blit(counter(1),64,160);p.blit(counter(2),96,160)
    p.blit(table(0),160,160);p.blit(table(1),192,160);p.blit(stool(),128,160)
    p.save('/home/claude/gen/out/prev_int.png',3)
