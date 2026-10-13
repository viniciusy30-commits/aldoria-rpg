from pa import *

PL=C('ecd8a8'); PLH=C('f8ecc8'); PLS=C('d4b884'); PLD=C('b8946a')
BM=C('4a301c'); BMD=C('2c1a10'); BML=C('6e4a2c'); BMH=C('92683e')
ST=C('7e756c'); IRON=C('26262e'); IRONH=C('8a8a9a')
GOLD=C('d8a640'); GOLDH=C('f6dc7c')
SHUT=C('9a3a2e')

def beam_h(c,x,y,w,t):
    c.rect(x,y,w,t,BM)
    c.rect(x,y,w,1,BML)
    if t>=3: c.rect(x,y+1,w,1,sh(BML,-0.1))
    c.rect(x,y+t-1,w,1,BMD)
    for i in range(0,w,5):
        c.set(x+i+(y%3),y+t//2,sh(BM,-0.25))
    for i in range(3,w,13): c.set(x+i,y+1,BMH)   # pino claro

def beam_v(c,x,y,t,h):
    c.rect(x,y,t,h,BM)
    c.rect(x,y,1,h,BML)
    c.rect(x+t-1,y,1,h,BMD)
    for j in range(0,h,6):
        c.set(x+t//2,y+j+(x%4),sh(BM,-0.25))
    for j in range(4,h,14): c.set(x+1,y+j,BMH)

def brace(c,x0,y0,x1,y1):
    c.line(x0,y0,x1,y1,BM); c.line(x0+1,y0,x1+1,y1,BM)
    c.line(x0-1,y0,x1-1,y1,BMD)
    c.line(x0+2,y0,x1+2,y1,BML) if False else None

def plaster_fill(c,rn,y0,y1):
    for y in range(y0,y1):
        t=(y-y0)/max(1,(y1-y0))
        base=mix(PLH,PLS,t*0.6)
        for x in range(32): c.set(x,y,base)
    for i in range(190):
        x=rn.randrange(32);y=rn.randrange(y0,y1)
        c.set(x,y,sh(PL,rn.choice([0.1,0.14,-0.07,-0.12])))
    for i in range(14):   # marcas de desempenadeira
        x=rn.randrange(26);y=rn.randrange(y0+2,y1-2);l=rn.randrange(3,7)
        for k in range(l): c.set(x+k,y,sh(PL,0.12))
    for k in range(3):    # rachaduras
        x=rn.randrange(4,28);y=rn.randrange(y0+3,y1-12)
        for j in range(rn.randrange(5,10)):
            c.set(x,y+j,PLD); x+=rn.randrange(-1,2)

def exposed(c,x,y,w,h,rn):
    # reboco caído mostrando tijolo/enchimento
    for yy in range(y,y+h):
        for xx in range(x,x+w):
            col=C('b4673c')
            if (yy-y)%3==2: col=C('8a4a2a')
            elif (xx-x+((yy-y)//3)*2)%5==0: col=C('8a4a2a')
            elif (xx+yy)%7==0: col=C('c87a4a')
            c.set(xx,yy,col)
    for xx in range(x-1,x+w+1):
        c.set(xx,y-1,PLD); c.set(xx,y+h,PLD)
    for yy in range(y,y+h): c.set(x-1,yy,PLD); c.set(x+w,yy,PLD)

def plinth(c,rn):
    mort=C('3e3a40')
    c.rect(0,49,32,15,ST)
    y=49; row=0
    while y<64:
        c.rect(0,y,32,1,mort)
        x=-6 if row%2 else 0
        while x<32:
            bw=rn.choice([9,10,11])
            tone=rn.choice([-0.12,0,0.08,0.14,-0.05])
            col=sh(ST,tone)
            for yy in range(y+1,min(64,y+5)):
                for xx in range(x+1,x+bw):
                    c.set(xx,yy,col)
            for xx in range(x+1,x+bw-1): c.set(xx,y+1,sh(col,0.3))
            for yy in range(y+1,y+5): c.set(x+bw-1,yy,sh(col,-0.25))
            for q in range(2): c.set(x+2+rn.randrange(bw-3),y+2+rn.randrange(2),sh(col,rn.choice([-0.2,0.2])))
            c.set(x,y+1,mort);
            x+=bw
        y+=5; row+=1
    c.rect(0,49,32,1,sh(ST,0.45))
    c.rect(0,63,32,1,C('241f2a'))
    # sombra do reboco sobre a pedra
    for x in range(32): c.set(x,50,sh(c.get(x,50),-0.25))
    moss=[C('4a7a34'),C('3a6a2c'),C('5e8a3a'),C('7aa648')]
    for i in range(22):
        x=rn.randrange(32); c.set(x,62-rn.randrange(4),rn.choice(moss))
    for x in range(0,32):
        if rn.random()<0.35: c.set(x,63,rn.choice(moss[:2]))

def eave(c,dark=True):
    # embaixo do beiral do telhado: sombra + beirais com cachorros
    grad=[-0.62,-0.55,-0.45,-0.36,-0.28,-0.2,-0.14,-0.1,-0.06,-0.03,-0.015,0] if dark else [-0.5,-0.42,-0.34,-0.26,-0.18,-0.12,-0.07,-0.04,-0.02,0,0,0]
    for y,g in enumerate(grad):
        for x in range(32): c.set(x,y,sh(c.get(x,y),g))
    if dark:
        c.rect(0,0,32,2,C('1c110a'))
        c.rect(0,2,32,3,BM); c.rect(0,2,32,1,BML); c.rect(0,4,32,1,BMD)
    for x0 in (3,15,27):   # cabeças de viga
        c.rect(x0,5,4,6,BM); c.rect(x0,5,1,6,BML); c.rect(x0+3,5,1,6,BMD); c.rect(x0+1,11,2,1,BMD)
        c.set(x0+1,6,BMH)
    if dark: brace(c,6,7,12,10); brace(c,19,10,25,7)

def window(c,rn,x0=8,x1=23,y0=18,y1=37,flowers=True,oy=0):
    # moldura de madeira + peitoril de pedra
    c.rect(x0-2,y0-2,x1-x0+5,y1-y0+5,BMD)
    c.rect(x0-1,y0-1,x1-x0+3,y1-y0+3,BM)
    c.rect(x0-1,y0-1,x1-x0+3,1,BML)
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            t=(y-y0)/(y1-y0)
            col=mix(C('c8e6f4'),C('5e9ec6'),t)
            c.set(x,y,col)
    # chumbo em losango
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            if (x+y)%6==0 or (x-y+200)%6==0: c.set(x,y,C('34343e'))
    # luz quente lá dentro (lampião)
    for y in range(y0+8,y1+1):
        for x in range(x0+1,x1):
            d=math.hypot(x-16,y-31)
            if d<9 and c.get(x,y)!=C('34343e'):
                c.set(x,y,mix(c.get(x,y),C('ffd878'),0.75*(1-d/9)))
    for k in range(5): c.set(x0+2+k,y0+6-k,(255,255,255,255)); c.set(x0+3+k,y0+6-k,C('e8f6ff'))
    # venezianas
    for sx in (x0-7,x1+3):
        for y in range(y0-1,y1+2):
            for x in range(sx,sx+5):
                col=SHUT
                m=(y-y0)%3
                if m==0: col=sh(SHUT,-0.4)
                elif m==1: col=sh(SHUT,0.16)
                if x==sx: col=sh(col,0.25)
                if x==sx+4: col=sh(col,-0.4)
                c.set(x,y,col)
        c.set(sx+(1 if sx<x0 else 3),y0+9,GOLD)
        c.rect(sx,y0-2,5,1,BMD); c.rect(sx,y1+2,5,1,BMD)
    if not flowers:
        c.rect(x0-3,y1+2,x1-x0+7,2,C('b8a078')); c.rect(x0-3,y1+2,x1-x0+7,1,C('e0cc9c')); c.rect(x0-3,y1+4,x1-x0+7,1,C('4a3a30'))
        return
    # peitoril + jardineira com flores
    c.rect(x0-3,y1+2,x1-x0+7,2,C('b8a078')); c.rect(x0-3,y1+2,x1-x0+7,1,C('e0cc9c'))
    c.rect(x0-2,y1+4,x1-x0+5,6,C('7a4a24')); c.rect(x0-2,y1+4,x1-x0+5,1,C('a46c38')); c.rect(x0-2,y1+9,x1-x0+5,1,BMD)
    for i in range(x0,x1,4): c.rect(i,y1+5,1,4,sh(C('7a4a24'),-0.25))
    gs=[C('2f7a3a'),C('3f9a4a'),C('58b85a'),C('8cc24a')]
    fl=[C('e8505b'),C('f5d547'),C('ffffff'),C('c06ae0'),C('ff8fb7')]
    for k in range(x1-x0+5):
        xx=x0-2+k
        c.set(xx,y1+3,gs[k%4]); c.set(xx,y1+4-1+1 if False else y1+3,gs[(k+1)%4])
        if k%2==0:
            c.set(xx,y1+2,fl[rn.randrange(5)])
            if rn.random()<0.7: c.set(xx,y1+1,fl[rn.randrange(5)])
    for k in range(7):
        xx=x0-1+k*3; c.set(xx,y1+10,gs[k%3]); 
        if k%2==0: c.set(xx,y1+11,gs[1])

def lantern(c,frame):
    for y in range(14,48):
        for x in range(2,30):
            d=math.hypot(x-22,y-28)
            if d<13:
                col=c.get(x,y)
                if col[3]: c.set(x,y,mix(col,C('ffc860'),0.28*(1-d/13)**1.2))
    c.rect(11,17,3,22,IRON); c.rect(11,17,1,22,IRONH)
    c.rect(11,17,12,2,IRON)
    for k in range(5): c.set(12+k,19+k//2,IRON)
    c.rect(21,19,1,3,IRON)
    c.rect(19,22,6,2,IRON); c.rect(20,21,4,1,IRON)
    gl=C('ffe066') if frame==0 else C('ffd04a')
    c.rect(19,24,6,9,C('b8741a'))
    c.rect(20,25,4,7,gl); c.rect(21,26,2,5,C('fffbe0'))
    c.rect(19,24,1,9,IRON); c.rect(24,24,1,9,IRON)
    c.rect(19,33,6,2,IRON); c.set(21,35,IRON); c.set(22,35,IRON)
    for k in range(4): c.set(11+k*3,37,IRON); c.set(12+k*3,38,IRON)
    # peso
    c.rect(11,39,3,2,IRON)

def barrels(c,rn):
    wd=C('94602c')
    for bx,by,rr in ((10,37,8),(23,40,6)):
        for y in range(by,by+14):
            t=(y-by)/13
            half=int(rr*(0.86+0.22*math.sin(math.pi*t)))
            for x in range(bx-half,bx+half+1):
                col=wd
                u=(x-(bx-half))/(2*half+1)
                col=mix(C('c48848'),C('5a3818'),u)
                if (x-bx+40)%4==0: col=sh(col,-0.18)
                c.set(x,y,col)
        for y in (by+3,by+10):
            half=int(rr*(0.86+0.22*math.sin(math.pi*(y-by)/13)))
            for x in range(bx-half,bx+half+1): c.set(x,y,C('3a3a44')); c.set(x,y+1,C('6a6a78')) if x%3==0 else None
        c.ell(bx,by,rr*0.92,2,C('b88444'))
        c.ell(bx,by,rr*0.7,1.2,C('8a5a2c'))
        c.rect(bx-1,by+6,2,3,GOLD)
    gs=[C('2f7a3a'),C('3f9a4a'),C('58b85a')]
    for y in range(15,40):
        if rn.random()<0.85: c.set(26+(1 if rn.random()<0.3 else 0),y,gs[rn.randrange(3)])
    for k in range(18): c.set(22+rn.randrange(9),16+rn.randrange(21),gs[rn.randrange(3)])
    for k in range(5): 
        x=22+rn.randrange(9);y=20+rn.randrange(14); c.rect(x,y,1,2,C('c8e070'))

def lower(kind,edge,frame=0):
    c=Cv(32,64)
    rn=random.Random(900+kind*13+edge*7)
    c.rect(0,0,32,64,PL)
    plaster_fill(c,rn,0,49)
    if kind==0:
        exposed(c,19,38,6,5,rn) if False else None
    # barras
    beam_h(c,0,12,32,3)
    beam_h(c,0,46,32,3)
    eave(c,False)
    beam_v(c,0,15,3,31); beam_v(c,29,15,3,31)
    if kind==0:
        beam_v(c,14,15,4,31)
        brace(c,3,17,13,43); brace(c,19,43,28,17)
        for p in ((8,30),(23,30)): c.set(p[0],p[1],GOLD); c.set(p[0]+1,p[1],sh(GOLD,-0.3))
        exposed(c,4,36,5,5,rn)
    elif kind==1: window(c,rn)
    elif kind==2: lantern(c,frame)
    elif kind==3: barrels(c,rn)
    # AO: escurece o reboco logo à direita/abaixo das vigas
    for y in range(15,46):
        for x in range(32):
            col=c.get(x,y)
            if col[:3] in (PLH[:3],) : pass
    plinth(c,rn)
    if edge==1:
        beam_v(c,0,12,5,37)
        c.rect(0,49,6,15,sh(ST,0.12)); c.rect(0,49,6,1,sh(ST,0.45))
        for y in (53,58): c.rect(0,y,6,1,C('3e3a40'))
        c.rect(5,49,1,15,sh(ST,-0.3))
    elif edge==2:
        beam_v(c,27,12,5,37)
        c.rect(26,49,6,15,sh(ST,-0.02)); c.rect(26,49,6,1,sh(ST,0.35))
        for y in (53,58): c.rect(26,y,6,1,C('3e3a40'))
        c.rect(26,49,1,15,sh(ST,0.2))
    return c


def upper(kind,edge,rn):
    c=Cv(32,32)
    c.rect(0,0,32,32,PL)
    plaster_fill(c,rn,0,32)
    eave(c,True)
    # trava + viga de baixo
    beam_h(c,0,28,32,4)
    beam_v(c,0,6,3,22); beam_v(c,29,6,3,22)
    if kind==0:
        beam_v(c,14,6,4,22)
        brace(c,3,7,13,26); brace(c,19,26,28,7)
    elif kind==1:
        window(c,rn,9,22,9,22,False)
        # veneziana lateral
        pass
    elif kind==2:
        beam_v(c,14,6,4,22)
        # brasão de caneca
        c.rect(7,10,18,16,BMD); c.rect(8,11,16,14,C('7a2a2a'))
        c.rect(8,11,16,1,GOLD); c.rect(8,24,16,1,GOLD); c.rect(8,11,1,14,GOLD); c.rect(23,11,1,14,GOLD)
        c.rect(12,14,7,8,GOLD); c.rect(12,14,1,8,GOLDH); c.rect(19,16,3,4,GOLD); c.rect(20,17,1,2,C('7a2a2a'))
        c.rect(12,12,7,2,C('fff6e0')); c.set(14,11,(255,255,255,255)); c.set(17,11,(255,255,255,255))
    elif kind==3:
        # porta de celeiro (sótão) com viga de içar e polia
        c.rect(8,10,16,18,BMD)
        for x in range(9,23):
            for y in range(11,28):
                col=C('7a4a2a')
                if x%4==0: col=sh(col,-0.25)
                if (x+1)%4==0: col=sh(col,0.12)
                c.set(x,y,col)
        c.rect(15,11,2,17,BMD)
        for y in (14,23): c.rect(9,y,13,2,IRON)
        c.rect(8,6,24,3,BM); c.rect(8,6,24,1,BML)  # viga de içar projetando
        c.rect(26,9,1,5,IRON); c.ell(26,15,2,2,IRON); c.ell(26,15,0.9,0.9,IRONH)
        c.line(26,17,26,27,C('b89860')); c.rect(25,27,3,2,IRON)
    for i in range(8): c.set(4+i*4,5,BMD) if False else None
    # postes de canto
    if edge==1: beam_v(c,0,6,5,22)
    if edge==2: beam_v(c,27,6,5,22)
    return c

def facade(kind,edge,frame=0):
    rn=random.Random(5500+kind*17+edge*5)
    c=Cv(32,96)
    c.blit(upper(kind,edge,rn),0,0)
    c.blit(lower(kind,edge,frame),0,32)
    return c

if __name__=='__main__':
    # preview: linha de fachada
    kinds=[3,0,1,2,1,0,1,2,0,1,3]
    W=len(kinds)*32
    row=Cv(W,96)
    for i,k in enumerate(kinds):
        e=1 if i==0 else (2 if i==len(kinds)-1 else 0)
        row.blit(facade(k,e),i*32,0)
    row.save('/home/claude/gen/out/prev_facade.png',3)
