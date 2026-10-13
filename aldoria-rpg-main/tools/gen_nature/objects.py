from pa import *

SHD=(24,16,34,86)
def gshadow(c,cx,cy,rx,ry,a=86):
    for y in range(int(cy-ry)-1,int(cy+ry)+2):
        for x in range(int(cx-rx)-1,int(cx+rx)+2):
            d=((x+.5-cx)/rx)**2+((y+.5-cy)/ry)**2
            if d<=1:
                al=a if d<0.7 else int(a*0.55)
                if c.get(x,y)[3]==0: c.set(x,y,(24,16,34,al))

def pal5(mid):
    return dict(hi=sh(mid,0.40),light=sh(mid,0.18),mid=mid,dark=sh(mid,-0.24),deep=sh(mid,-0.46))

def leafy(c,blobs,pal,rnd,lit=1.0):
    """Copa de folhas: bolhas pintadas de trás para frente, com luz de cima-esquerda e vincos escuros entre as massas."""
    order=sorted(blobs,key=lambda b:(b[1]+b[2]*0.2))
    seed=rnd.random()*6
    for (cx,cy,r) in order:
        for y in range(int(cy-r)-2,int(cy+r)+3):
            for x in range(int(cx-r)-2,int(cx+r)+3):
                dx=x+.5-cx; dy=y+.5-cy
                ang=math.atan2(dy,dx)
                re=r*(1+0.09*math.sin(ang*6+seed)+0.05*math.sin(ang*11+seed*2))
                d=math.hypot(dx,dy)
                if d>re: continue
                t=(dx*0.55+dy*0.83)/re              # +1 = canto inferior direito
                tone=('hi' if t<-0.62 else 'light' if t<-0.15 else 'mid' if t<0.38 else 'dark' if t<0.74 else 'deep')
                # textura de folhas: pontinhos claros nas partes iluminadas, escuros nas sombras
                k=(x*5+y*3+int(seed*7))%9
                if k==0 and t<0.4: tone={'deep':'dark','dark':'mid','mid':'light','light':'hi','hi':'hi'}[tone]
                elif k==4 and t>-0.2: tone={'hi':'light','light':'mid','mid':'dark','dark':'deep','deep':'deep'}[tone]
                col=pal[tone]
                # vinco ao redor da massa (lado de sombra) e brilho no aro iluminado
                if d>re-1.1:
                    if t>0.05: col=pal['deep']
                    elif t<-0.5: col=pal['hi']
                c.set(x,y,col)
    # folhinhas soltas em relevo
    for i in range(34):
        cx,cy,r=rnd.choice(blobs)
        a=rnd.uniform(0,6.28); d=rnd.uniform(0.1,0.8)*r
        x=int(cx+math.cos(a)*d); y=int(cy+math.sin(a)*d)
        if c.get(x,y)[3]==0: continue
        t=((x-cx)*0.55+(y-cy)*0.83)/r
        if t<0.1:
            c.set(x,y,pal['hi']); c.set(x+1,y,pal['light']) if c.get(x+1,y)[3] else None
            c.set(x,y+1,pal['mid']) if c.get(x,y+1)[3] else None
        else:
            c.set(x,y,pal['deep']); c.set(x-1,y,pal['dark']) if c.get(x-1,y)[3] else None

def bark(c,pts_fn,x0,x1,y0,y1,base,rnd):
    pass

OAKP=[C('3d8a34'),C('4b9a3a'),C('357f2e')]
def oak(v):
    rnd=random.Random(2000+v*31)
    c=Cv(32,48)
    gshadow(c,16,45.5,12.5,3.0)
    bk=C(['6b4423','6e4626','644022'][v%3]); bp=pal5(bk)
    # tronco: coluna com alargamento nas raízes
    for y in range(24,46):
        t=(y-24)/21
        hw=3.0+2.0*t**2.6 + (1.5 if y>=41 else 0)
        for x in range(int(16-hw-2),int(16+hw+3)):
            u=(x+.5-16)/hw
            if abs(u)>1.0: continue
            col=bp['light'] if u<-0.55 else (bp['mid'] if u<0.1 else (bp['dark'] if u<0.62 else bp['deep']))
            if (x*3+y//4)%5==0 and -0.8<u<0.8: col=bp['dark'] if u>-0.3 else bp['mid']      # sulcos da casca
            if (x+y*2)%13==0: col=bp['deep']
            if u<-0.85: col=bp['hi']
            c.set(x,y,col)
    # raízes que se espalham no chão
    for sx,dx in ((-1,-1),(1,1)):
        for k,(rx,ry) in enumerate(((7,44),(9,45),(11,46))):
            xx=16+sx*rx; 
            c.set(xx,ry,bp['mid'] if sx<0 else bp['dark']); c.set(xx+sx,ry,bp['dark']); c.set(xx,ry+1,bp['deep'])
    for k in range(3): c.set(15+k,45,bp['deep'])
    # nó na casca + musgo
    c.set(15,36,bp['deep']); c.set(16,36,bp['dark']); c.set(15,37,bp['deep'])
    for y in range(38,43):
        if rnd.random()<0.8: c.set(12+rnd.randrange(2),y,C('4e7a30'))
    # copa
    tones=[OAKP[v%3]]
    pal=pal5(OAKP[v%3])
    blobs=[(16,9,8.5),(8,14,7.5),(24,14,7.5),(16,17,10),(5,22,5.5),(27,22,5.5),(11,26,6.5),(21,26,6.5),(16,4,5)]
    blobs=[(16+(x-16)*0.93+rnd.uniform(-1,1),y+rnd.uniform(-1,1),r*0.95+rnd.uniform(-0.5,0.5)) for x,y,r in blobs]
    # sombra sob a copa no tronco
    leafy(c,blobs,pal,rnd)
    for y in range(30,37):
        for x in range(10,23):
            q=c.get(x,y)
            # só no tronco (pixels marrons)
            if q[3] and abs(q[0]-q[1])>12 and q[0]>q[1]: c.set(x,y,sh(q,-0.42+ (y-30)*0.05))
    if v==1:
        for (ax,ay) in ((9,13),(21,11),(14,22),(24,22),(7,24)):
            c.set(ax,ay,C('d8332c')); c.set(ax+1,ay,C('a82020')); c.set(ax,ay+1,C('a82020')); c.set(ax,ay-1,C('ff8a70')) if False else None
    c.outline(C('16301a'))
    return c

def pine(v):
    rnd=random.Random(2200+v*7)
    c=Cv(32,48)
    gshadow(c,16,45.5,11,2.8)
    base=C('2c6a3a') if v==0 else C('2c6a58')
    pal=pal5(base)
    bk=pal5(C('5a3a1e'))
    for y in range(38,46):
        for x in range(14,19):
            u=x-14
            c.set(x,y,[bk['light'],bk['mid'],bk['mid'],bk['dark'],bk['deep']][u])
        if y%3==0: c.set(15,y,bk['dark'])
    c.set(13,45,bk['dark']); c.set(19,45,bk['deep']); c.set(12,46,bk['deep']) if False else None
    tiers=[(1,10,6),(8,18,9),(15,26,12),(22,34,14.5),(29,41,15.5)]
    for ti in range(len(tiers)-1,-1,-1):
        y0,y1,hw=tiers[ti]
        for y in range(y0,y1+1):
            t=(y-y0)/(y1-y0)
            half=hw*(0.08+0.92*t)
            # borda inferior em dentes
            edge_y=y1-int(abs(((y*0+0))))
            for x in range(int(16-half-1),int(16+half+2)):
                u=(x+.5-16)/max(1,half)
                if abs(u)>1: continue
                # dentes na barra
                tooth=2.2*math.sin((x+ti*3)*1.5)
                if y>y1-3+tooth: continue
                col=pal['hi'] if u<-0.7 else pal['light'] if u<-0.3 else pal['mid'] if u<0.2 else pal['dark'] if u<0.62 else pal['deep']
                # agulhas: traços diagonais
                if (x+y*2+ti*5)%6==0 and u<0.4: col=pal['light'] if u<-0.1 else pal['mid']
                if (x*2+y+ti)%7==3 and u>-0.4: col=pal['deep']
                # faixa de sombra projetada pela fiada de cima
                if t<0.14 and ti>0: col=sh(col,-0.28)
                c.set(x,y,col)
        # sombra sob cada fiada (a de baixo fica mais escura logo abaixo)
        for x in range(int(16-hw),int(16+hw)):
            for yy in (y1-1,y1):
                q=c.get(x,yy)
                if q[3]: c.set(x,yy,sh(q,-0.38))
    c.set(16,0,pal['light']); c.set(16,1,pal['mid'])
    if v==1:                                   # neve nas pontas
        for (x,y) in ((10,24),(22,26),(8,32),(24,33),(12,16),(20,17)): c.set(x,y,C('f0f8ff')); c.set(x+1,y,C('d0e4f4'))
    c.outline(C('10281e'))
    return c

BUSHP=[C('3f8f38'),C('4a9a3a'),C('37822f'),C('449438')]
def bush(v,flowers):
    rnd=random.Random(2700+v*9+(100 if flowers else 0))
    c=Cv(32,32)
    gshadow(c,16,28.5,12,3.0)
    pal=pal5(BUSHP[v&3])
    blobs=[(16,19,8.5),(9,22,6.5),(23,22,6.5),(16,14,6.5),(11,16,5.5),(21,16,5.5),(16,24,7)]
    blobs=[(x+rnd.uniform(-.8,.8),y+rnd.uniform(-.8,.8),r) for x,y,r in blobs]
    leafy(c,blobs,pal,rnd)
    if flowers:
        cols=[(C('ff6a9a'),C('ffc0d8')),(C('ffffff'),C('ffe890')),(C('f8d040'),C('fff4b0')),(C('e8483c'),C('ffa090'))][v&3]
        for k in range(11):
            x=7+rnd.randrange(18); y=9+rnd.randrange(16)
            if c.get(x,y)[3]==0: continue
            c.set(x,y,cols[0]); c.set(x+1,y,cols[0]); c.set(x,y-1,cols[1]); c.set(x-1,y,cols[1]) if False else None
            c.set(x,y+1,sh(cols[0],-0.3))
    else:
        for k in range(6):                         # frutinhas
            x=8+rnd.randrange(16); y=12+rnd.randrange(12)
            if c.get(x,y)[3]: c.set(x,y,C('c8362c')); c.set(x,y-1,C('f08a70'))
    c.outline(C('16301a'))
    return c

def rock(v):
    rnd=random.Random(2900+v*13)
    c=Cv(32,32)
    gshadow(c,16,27,13,3.2)
    base=C('8e8e98'); pal=pal5(base)
    cx,cy,rx,ry=16+(v-1)*0.8,22,11.5-v*0.5,7.5+v*0.3
    seeds=[(cx+rnd.uniform(-rx*.7,rx*.7),cy+rnd.uniform(-ry*.7,ry*.5),rnd.uniform(-0.35,0.35)) for _ in range(6)]
    for y in range(int(cy-ry)-1,int(cy+ry)+2):
        for x in range(int(cx-rx)-1,int(cx+rx)+2):
            dx=(x+.5-cx)/rx; dy=(y+.5-cy)/ry
            rho=dx*dx+dy*dy*1.1
            wob=1+0.07*math.sin(math.atan2(dy,dx)*5+v*2)
            if rho>wob: continue
            hz=math.sqrt(max(0,1-rho))
            lum=(-dx*0.55-dy*0.8)*0.6+hz*0.5          # luz de cima-esquerda
            # faceta mais próxima
            bi=min(range(len(seeds)),key=lambda i:(x-seeds[i][0])**2+(y-seeds[i][1])**2)
            lum+=seeds[bi][2]
            tone='hi' if lum>0.62 else 'light' if lum>0.36 else 'mid' if lum>0.12 else 'dark' if lum>-0.12 else 'deep'
            col=pal[tone]
            if (x*5+y*7+v)%11==0: col=sh(col,-0.08)
            if rho>0.8 and (dx+dy)>0: col=pal['deep'] if rho>0.9 else pal['dark']
            c.set(x,y,col)
    # rachaduras
    for k in range(2):
        x=int(cx-4+rnd.randrange(9)); y=int(cy-3+rnd.randrange(3))
        for j in range(5): c.set(x+(j//2),y+j,pal['deep'])
    # musgo no topo
    mo=[C('3f6e2c'),C('5a9a3c'),C('7cc04c')]
    for k in range(7):
        x=int(cx-6+rnd.randrange(12)); y=int(cy-ry+2+rnd.randrange(3))
        if c.get(x,y)[3]: c.set(x,y,rnd.choice(mo)); c.set(x+1,y,rnd.choice(mo[:2])) if c.get(x+1,y)[3] else None
    c.outline(C('2a2630'))
    return c

# ------------------------------------------------------------------ CERCA
WP=[C('3a2410'),C('6a4424'),C('8e6234'),C('b08044'),C('d0a468'),C('e8c488')]
def plank_h(c,x0,x1,y,t):
    for x in range(x0,x1+1):
        for k in range(t):
            col=WP[4] if k==0 else (WP[3] if k<t-1 else WP[1])
            if k==t-1: col=WP[1]
            if (x*3+k*7)%9==0 and 0<k<t-1: col=WP[2]
            c.set(x,y+k,col)
def fence(mask):
    c=Cv(32,32)
    N_=mask&1; E=mask&2; S_=mask&4; Wd=mask&8
    run=bool(E or Wd)
    gshadow(c,16,30.2,8+(7 if run else 0),1.8,80)
    if run:
        x0=0 if Wd else 12; x1=31 if E else 19
        for y,t in ((14,3),(21,3)): plank_h(c,x0,x1,y,t)
        # pregos nas pontas e ranhuras de madeira
        for x in range(x0,x1+1):
            if x%7==3: 
                c.set(x,15,WP[2]); 
            if x%11==5: c.set(x,22,WP[2])
        # estacas pontiagudas (alinhadas à grade global: x = 2, 10(*), 18(*), 26)
        for px in (2,26,10,18):
            if px+3<x0 or px>x1: continue
            if not (12<=px<=19 or 12<=px+3<=19 or (px<12<px+3)):
                pass
            if px+3>=12 and px<=19 and px not in (2,26): continue
            for y in range(10,29):
                top=y-10
                for k in range(4):
                    xx=px+k
                    if xx<x0 or xx>x1: continue
                    if top==0 and k in (0,3): continue
                    if top<=1 and k in (0,3) and False: continue
                    col=WP[4] if k==0 else (WP[3] if k==1 else (WP[2] if k==2 else WP[1]))
                    if top==0: col=WP[5]
                    if (xx*5+y)%13==0: col=sh(col,-0.12)
                    if y>=27: col=sh(col,-0.2)
                    c.set(xx,y,col)
            c.set(px+1,10,WP[5]); c.set(px+1,17+0,WP[3]) if False else None
            # sombra da estaca sobre o trilho
            for y in (15,16,22,23):
                if x0<=px+4<=x1: c.set(px+4,y,sh(c.get(px+4,y),-0.35))
            for y in (14,21): c.set(px+1,y+1,WP[2]); c.set(px+2,y+1,WP[1])
    if N_ or S_:
        ya=0 if N_ else 16; yb=15 if N_ else 29
        if N_: ya,yb=0,15
        if S_: ya2,yb2=16,29
        for (a,b,on) in ((0,15,N_),(16,29,S_)):
            if not on: continue
            for y in range(a,b+1):
                for k in range(4):
                    xx=14+k
                    col=WP[4] if k==0 else (WP[3] if k==1 else (WP[2] if k==2 else WP[1]))
                    if (y*3+k)%7==0: col=sh(col,-0.15)
                    c.set(xx,y,col)
                if y%6==2:                         # travessas vistas de topo
                    for xx in range(12,20): c.set(xx,y,WP[3] if xx<16 else WP[2]); c.set(xx,y+1,WP[1])
                    c.set(12,y,WP[5]); c.set(13,y,WP[4])
    # poste central com chapéu chanfrado
    for y in range(9,30):
        for x in range(13,19):
            u=x-13
            col=[WP[4],WP[4],WP[3],WP[2],WP[1],WP[0]][u] if True else WP[3]
            if u==1: col=WP[3]
            if (y*2+u)%9==0: col=sh(col,-0.18)
            if y>=27: col=sh(col,-0.25)
            c.set(x,y,col)
    for x in range(12,20): c.set(x,8,WP[3] if x<16 else WP[2])
    for x in range(13,19): c.set(x,7,WP[5] if x<15 else WP[3])
    c.set(14,6,WP[5]); c.set(15,6,WP[4]); c.set(16,6,WP[3]); c.set(15,5,WP[5])
    for y in (12,13): c.set(14,y,I_:=C('4a4a58')) if False else None
    c.set(15,14,C('8a889c')); c.set(16,14,C('34323e'))                       # prego/ferragem
    c.set(15,23,C('8a889c')); c.set(16,23,C('34323e'))
    # capim ao pé
    for x in (11,19,20):
        c.set(x,30,C('3f7a2e')); c.set(x,29,C('5aa044'))
    c.outline(C('2a1808'))
    return c

if __name__=='__main__':
    from PIL import Image
    items=[oak(0),oak(1),oak(2),pine(0),pine(1),bush(0,False),bush(1,True),bush(2,True),rock(0),rock(2)]
    sc=6
    im=Image.new('RGBA',(sum(t.w for t in items)*sc,48*sc),(70,120,60,255))
    x=0
    for t in items:
        im.alpha_composite(t.im.resize((t.w*sc,t.h*sc),Image.NEAREST),(x,(48-t.h)*sc)); x+=t.w*sc
    im.save('/home/claude/prev/obj.png')
    fs=[fence(m) for m in (0,2,10,8,5,1,4,15)]
    im=Image.new('RGBA',(32*len(fs)*sc,32*sc),(70,120,60,255))
    for k,t in enumerate(fs): im.alpha_composite(t.im.resize((32*sc,32*sc),Image.NEAREST),(k*32*sc,0))
    im.save('/home/claude/prev/fence.png')
