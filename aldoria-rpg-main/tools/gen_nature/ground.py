from pa import *

N=32
def make_noise(cell,seed):
    n=N//cell; rnd=random.Random(seed); g=[[rnd.random() for _ in range(n)] for _ in range(n)]
    def f(x,y):
        fx=x/cell; fy=y/cell; ix=int(fx)%n; iy=int(fy)%n; tx=fx-int(fx); ty=fy-int(fy)
        tx=tx*tx*(3-2*tx); ty=ty*ty*(3-2*ty)
        a=g[iy][ix]; b=g[iy][(ix+1)%n]; c=g[(iy+1)%n][ix]; d=g[(iy+1)%n][(ix+1)%n]
        return (a*(1-tx)+b*tx)*(1-ty)+(c*(1-tx)+d*tx)*ty
    return f

def ws(c,x,y,col): c.p[x%N,y%N]=col
def wg(c,x,y): return c.p[x%N,y%N]
BAY=[[0,2],[3,1]]
def dith(x,y): return (BAY[y&1][x&1]/4.0-0.375)*0.10

def new(): return Cv(N,N)

# ------------------------------------------------------------------ GRAMA
GM=['4f9d37','52a038','4c9a35','50a037']
def pebble(c,x,y,base=C('a8a8b2')):
    for dx,dy in ((0,0),(1,0),(2,0),(0,1),(1,1),(2,1),(3,1),(1,2),(2,2),(3,2)):
        ws(c,x+dx,y+dy,base)
    ws(c,x,y,sh(base,0.4)); ws(c,x+1,y,sh(base,0.3)); ws(c,x,y+1,sh(base,0.12))
    ws(c,x+3,y+2,sh(base,-0.3)); ws(c,x+2,y+2,sh(base,-0.2)); ws(c,x+3,y+1,sh(base,-0.2))
    for dx in range(1,5): ws(c,x+dx,y+3,sh(base,-0.5) if dx<4 else sh(base,-0.35))

def tuft(c,bx,by,pal,rnd,big=False):
    dark,mid,light,hi,deep=pal
    blades=[(-1,3,light),(0,4 if not big else 5,mid),(1,3,dark)]
    for lean,L,col in blades:
        for j in range(L):
            x=bx+lean*(1+j//2)*(1 if lean else 0)+(0 if lean else (j//3)*rnd.choice([0,0,1]))
            y=by-j
            cc=col
            if j==0: cc=deep
            elif j==L-1: cc=hi if lean<=0 else light
            elif j>=L-2: cc=sh(col,0.12)
            ws(c,x,y,cc)
    ws(c,bx+1,by+1,sh(dark,-0.25)); ws(c,bx+2,by+1,sh(dark,-0.15))

def grass(v):
    rnd=random.Random(100+v*17)
    mid=C(GM[v&3]); pal=(sh(mid,-0.18),mid,sh(mid,0.15),sh(mid,0.34),sh(mid,-0.42))
    dark,midc,light,hi,deep=pal
    n8=make_noise(8,300+v); n4=make_noise(4,400+v); n16=make_noise(16,500+v)
    c=new()
    for y in range(N):
        for x in range(N):
            nv=0.5*n16(x,y)+0.3*n8(x,y)+0.2*n4(x,y)+dith(x,y)
            col=dark if nv<0.40 else (midc if nv<0.62 else light)
            c.p[x,y]=col
    for i in range(22): ws(c,rnd.randrange(N),rnd.randrange(N),rnd.choice([dark,light,sh(light,0.1)]))
    for i in range(4): ws(c,rnd.randrange(N),rnd.randrange(N),C('6a4a28'))     # grãos de terra
    for i in range(15): tuft(c,rnd.randrange(N),rnd.randrange(N),pal,rnd,big=(i%5==0))
    if v==4:
        for i in range(3): pebble(c,3+rnd.randrange(24),3+rnd.randrange(24))
    elif v==5:
        for k in range(4):
            x=4+rnd.randrange(24); y=5+rnd.randrange(22)
            pet=rnd.choice([C('ffffff'),C('ffffff'),C('fff2a8'),C('ffb8d0')])
            ws(c,x,y+2,deep); ws(c,x,y+3,mid); ws(c,x+1,y+3,dark)           # haste e folha
            for dx,dy in ((0,-1),(-1,0),(1,0),(0,1)): ws(c,x+dx,y+dy,pet)
            ws(c,x,y,C('f2c030')); ws(c,x-1,y-1,sh(pet,-0.0) if False else pet) if False else None
        for k in range(3):
            x=2+rnd.randrange(28); y=4+rnd.randrange(24)
            ws(c,x,y,C('5a8af0')); ws(c,x,y-1,C('9ab8ff')); ws(c,x,y+1,deep)
    elif v==6:
        for k in range(4):
            x=3+rnd.randrange(25); y=3+rnd.randrange(25)
            for dx,dy in ((0,0),(1,0),(0,1),(1,1)): ws(c,x+dx,y+dy,sh(mid,-0.3))
            for dx,dy in ((2,-1),(3,-1),(2,0),(3,0)): ws(c,x+dx,y+dy,sh(mid,-0.3))
            for dx,dy in ((2,2),(3,2),(2,3),(3,3)): ws(c,x+dx,y+dy,sh(mid,-0.3))
            ws(c,x,y,mid); ws(c,x+2,y-1,mid); ws(c,x+2,y+2,mid); ws(c,x+1,y,light); ws(c,x+3,y-1,light)
    elif v==7:
        for k in range(12):
            x=rnd.randrange(N); y=rnd.randrange(N)
            for j in range(4): ws(c,x+(j//3),y-j,[C('9a8a38'),C('b8aa48'),C('d4c868'),C('ece08a')][j])
        for k in range(3): ws(c,rnd.randrange(N),rnd.randrange(N),C('c4472c'))
    return c

# ------------------------------------------------------------------ TERRA
def dirt(v):
    rnd=random.Random(800+v*11)
    base=C(['9c7348','a57a4d','94693f','a07648'][v&3])
    dark=sh(base,-0.2); light=sh(base,0.14); deep=sh(base,-0.38)
    n8=make_noise(8,610+v); n4=make_noise(4,620+v); n16=make_noise(16,630+v)
    c=new()
    for y in range(N):
        for x in range(N):
            nv=0.45*n16(x,y)+0.35*n8(x,y)+0.2*n4(x,y)+dith(x,y)
            c.p[x,y]=dark if nv<0.38 else (base if nv<0.64 else light)
    for i in range(70): ws(c,rnd.randrange(N),rnd.randrange(N),rnd.choice([dark,deep,light,sh(light,0.2)]))
    for i in range(4):                    # pedrinhas com luz e sombra
        pebble(c,2+rnd.randrange(26),3+rnd.randrange(24),base=mix(C('c4a67e'),C('8a8078'),rnd.random()*0.6))
    for i in range(3):                    # rachaduras
        x=rnd.randrange(N); y=rnd.randrange(N)
        for j in range(rnd.randrange(4,8)):
            ws(c,x,y,deep); ws(c,x+1,y+1,sh(base,0.12)) if False else None
            x+=rnd.choice([1,1,0]); y+=rnd.choice([0,1,1])
    for i in range(2):                    # tufos de capim teimoso
        x=rnd.randrange(N); y=rnd.randrange(N)
        ws(c,x,y,C('3f7a2e')); ws(c,x,y-1,C('5aa044')); ws(c,x+1,y,C('2f6224')); ws(c,x-1,y-1,C('6cb450'))
    if v==3:                              # sulcos de carroça (horizontais, repetem sem emenda)
        for row,ph in ((8,0.0),(21,1.7)):
            for x in range(N):
                yy=row+int(round(math.sin(2*math.pi*x/32+ph)*1.2))
                ws(c,x,yy,deep); ws(c,x,yy+1,sh(base,-0.24)); ws(c,x,yy-1,sh(base,0.2)); ws(c,x,yy+2,sh(base,0.1))
    return c

# ------------------------------------------------------------------ AREIA
def sand(v):
    rnd=random.Random(900+v*5)
    base=C(['e4cd8c','e8d294','dfc684','e6cf8e'][v&3])
    dark=sh(base,-0.14); light=sh(base,0.2); deep=sh(base,-0.3)
    n8=make_noise(8,710+v); n4=make_noise(4,720+v)
    c=new()
    for y in range(N):
        for x in range(N):
            nv=0.6*n8(x,y)+0.4*n4(x,y)+dith(x,y)
            c.p[x,y]=dark if nv<0.38 else (base if nv<0.66 else light)
    for r in range(3):                    # ondulações do vento
        y0=5+r*10+(v*3)%4; ph=r*1.9+v
        for x in range(N):
            yy=y0+int(round(math.sin(2*math.pi*x/32*(1+(r%2))+ph)*1.4))
            ws(c,x,yy,sh(base,0.34)); ws(c,x,yy+1,sh(base,-0.2)); ws(c,x,yy+2,sh(base,-0.08) if x%2 else base)
    for i in range(70): ws(c,rnd.randrange(N),rnd.randrange(N),rnd.choice([deep,light,sh(light,0.3),dark]))
    for i in range(2): pebble(c,3+rnd.randrange(24),3+rnd.randrange(24),base=mix(C('b8a890'),C('c8b098'),rnd.random()))
    if v==3:                              # conchinha
        x,y=18,19
        for dx,dy in ((0,0),(1,0),(2,0),(-1,1),(0,1),(1,1),(2,1),(3,1),(0,2),(1,2),(2,2)): ws(c,x+dx,y+dy,C('f4d8dc'))
        for dx in (0,1,2): ws(c,x+dx,y,C('ffffff'))
        for dx,dy in ((0,1),(2,1),(1,2)): ws(c,x+dx,y+dy,C('d49aa8'))
        ws(c,x+3,y+2,sh(base,-0.4)); ws(c,x+2,y+3,sh(base,-0.4)); ws(c,x+1,y+3,sh(base,-0.3))
    return c

# ------------------------------------------------------------------ CALÇADA DE PEDRAS
def cobble(v):
    rnd=random.Random(1000+v*17)
    n=16; seeds=[]
    for i in range(n):
        seeds.append(((i%4)*8+4+rnd.uniform(-2.6,2.6),(i//4)*8+4+rnd.uniform(-2.6,2.6)))
    tones=[]
    for i in range(n):
        warm=rnd.random()
        col=mix(C('8f8a92'),C('a39484'),warm)
        tones.append(sh(col,rnd.uniform(-0.12,0.14)))
    mort=C('3a343c'); c=new()
    n8=make_noise(8,810+v)
    for y in range(N):
        for x in range(N):
            ds=[]
            for i,(sx,sy) in enumerate(seeds):
                dx=min(abs(x+.5-sx),N-abs(x+.5-sx)); dy=min(abs(y+.5-sy),N-abs(y+.5-sy))
                # sinal do deslocamento (para saber o lado iluminado)
                ox=(x+.5-sx+16)%32-16; oy=(y+.5-sy+16)%32-16
                ds.append((math.hypot(ox,oy*1.05),i,ox,oy))
            ds.sort()
            d1,i1,ox,oy=ds[0]; d2=ds[1][0]
            gap=d2-d1
            base=tones[i1]
            if gap<1.25: col=mort if gap<0.8 else sh(mort,0.1)
            else:
                col=sh(base,0.10-0.22*(d1/6.0))             # abaulada: mais clara no centro
                rim=gap<2.7
                if rim:
                    lit=(-ox-oy)>0                           # luz de cima-esquerda
                    col=sh(base,0.34) if lit else sh(base,-0.36)
                    if lit and gap<1.9: col=sh(base,0.22)
                elif (x*7+y*13+i1)%11==0: col=sh(col,-0.07)
                if n8(x,y)>0.78 and (x+y)%2==0: col=sh(col,-0.05)
            c.p[x,y]=col
    for i in range(10):                    # musgo nas juntas
        x=rnd.randrange(N); y=rnd.randrange(N)
        if wg(c,x,y)[:3]==mort[:3]:
            ws(c,x,y,C('4a7a34'))
            if rnd.random()<0.5: ws(c,x+1,y,C('6aa044'))
    for i in range(8):
        x=rnd.randrange(N); y=rnd.randrange(N); ws(c,x,y,sh(wg(c,x,y),rnd.choice([-0.3,0.3])))
    return c

# ------------------------------------------------------------------ ÁGUA
def water(frame,shallow):
    P=(dict(a='3a9ed0',b='4cb2dc',c='62c6e6',d='2a84b8',hi='ecfaff',sand='d8c590') if shallow
       else dict(a='1d52a8',b='2462c0',c='2e78d6',d='173f88',hi='d4ecff',sand=None))
    a,b,cc,d,hi=C(P['a']),C(P['b']),C(P['c']),C(P['d']),C(P['hi'])
    rnd=random.Random(1350 if shallow else 1300)
    n8=make_noise(8,910+shallow); n4=make_noise(4,920+shallow)
    c=new(); th=2*math.pi/32; ph=frame*math.pi/2
    for y in range(N):
        for x in range(N):
            nv=0.55*n8(x,y)+0.45*n4(x,y)+dith(x,y)
            col=d if nv<0.30 else (a if nv<0.52 else (b if nv<0.74 else cc))
            # rede de cáustica (periódica: laços perfeitos nos 4 quadros)
            s=(math.sin(th*(2*x+y)+ph)+math.sin(th*(-x+3*y)+ph*1.0+1.3)+math.sin(th*(3*x-2*y)-ph+0.7))/3
            if s>0.80: col=sh(col,0.28) if shallow else sh(col,0.20)
            elif s<-0.84: col=sh(col,-0.14)
            c.p[x,y]=col
    if shallow:                            # areia e pedrinhas vistas através da água
        sd=C(P['sand'])
        for i in range(34): ws(c,rnd.randrange(N),rnd.randrange(N),mix(sd,wg(c,0,0),0.35))
        for i in range(3):
            x=rnd.randrange(N); y=rnd.randrange(N)
            for dx,dy in ((0,0),(1,0),(0,1),(1,1)): ws(c,x+dx,y+dy,mix(C('b8b4b0'),b,0.45))
            ws(c,x,y,mix(C('e0dcd8'),b,0.35))
    # cristas de onda que andam 8px/quadro (laço de 32px)
    rw=random.Random(1409 if shallow else 1400)
    for i in range(7):
        wy=3+i*4+rw.randrange(2); wx=rw.randrange(32); ln=7+rw.randrange(5)
        x0=(wx+frame*8)%32
        for k in range(ln):
            x=(x0+k)%32
            arc=(k//3) if k<ln//2 else (ln-k)//3
            yy=wy-arc
            ws(c,x,yy+1,d if not shallow else sh(a,-0.12))
            ws(c,x,yy,cc if k<2 or k>=ln-2 else sh(cc,0.18))
            if 2<=k<ln-2: ws(c,x,yy-1,hi)
    rs=random.Random(1500+frame*3+(7 if shallow else 0))
    for i in range(4):
        x=rs.randrange(N); y=rs.randrange(N)
        ws(c,x,y,hi)
        if i<2: ws(c,x-1,y,cc); ws(c,x+1,y,cc)
    return c

def seam_sheet(tiles,path,scale=3):
    from PIL import Image
    w=len(tiles)
    im=Image.new('RGBA',(N*2*w*scale//1,N*2*scale),(0,0,0,255))
    for k,t in enumerate(tiles):
        for ty in range(2):
            for tx in range(2):
                im.paste(t.im.resize((N*scale,N*scale),Image.NEAREST),((k*2+tx)*N*scale,ty*N*scale))
    im.save(path)

if __name__=='__main__':
    T=[grass(v) for v in range(8)]
    seam_sheet(T,'/home/claude/prev/grass.png',2)
    seam_sheet([dirt(v) for v in range(4)]+[sand(v) for v in range(4)],'/home/claude/prev/dirtsand.png',2)
    seam_sheet([cobble(v) for v in range(4)]+[water(f,False) for f in range(2)]+[water(f,True) for f in range(2)],'/home/claude/prev/stonewater.png',2)
