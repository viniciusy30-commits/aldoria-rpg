# Taverna v3 - fachada coerente (448x96), porta recuada, corte interno e peças novas.
import sys, math, random
sys.path.insert(0,'.')
from pa import *
import tav as T
from tav import W,A,I,G,TEAL,R,GLOW,S,MORT,OUT,hsh,beam_h,beam_v,brace,nail,halo,planks,mix,sh

PL=[C('a88a5c'),C('c9ab78'),C('e2cb9a'),C('f2e2b8')]   # reboco
FW=448; FH=96
POSTS=[0,64,128,192,256,320,384,444]
LO,HI,TOP,DBOT=208,239,42,83
WD=HI-LO+1; CX=(LO+HI+1)/2; SPRING=TOP+WD*0.5
BASE=84   # linha do chão da parede (abaixo: soleira/degraus)

def stone_wide(c,x0,x1,y0,y1,seed=11,dark=0.0,big=False):
    rng=random.Random(seed)
    c.rect(x0,y0,x1-x0,y1-y0,MORT)
    y=y0
    while y<y1:
        h=rng.choice([8,9,10] if big else [7,8,9])
        if y1-y-h<5: h=y1-y
        x=x0-rng.randrange(0,12)
        while x<x1:
            w=rng.choice([11,13,15,18] if big else [9,11,13,15])
            sx=x; x+=w
            if sx+w<=x0 or sx>=x1: continue
            base=S[rng.choice([1,2,2,3,2,1,3])]
            if rng.choice([0,0,1]): base=mix(base,C('b08a60'),0.22)
            base=sh(base,dark)
            for yy in range(y+1,y+h):
                for xx in range(sx+1,sx+w):
                    if x0<=xx<x1 and y0<=yy<y1:
                        c.set(xx,yy,sh(base,0.10-0.26*(yy-y)/h))
            for xx in range(sx+1,sx+w):
                if x0<=xx<x1: c.set(xx,y+1,sh(base,0.34)); c.set(xx,y+h-1,sh(base,-0.42))
            for yy in range(y+1,y+h):
                if y0<=yy<y1:
                    if x0<=sx+1<x1: c.set(sx+1,yy,sh(base,0.22))
                    if x0<=sx+w-1<x1: c.set(sx+w-1,yy,sh(base,-0.34))
            for (cx_,cy_) in ((sx+1,y+1),(sx+w-1,y+1),(sx+1,y+h-1),(sx+w-1,y+h-1)):
                if x0<=cx_<x1 and y0<=cy_<y1: c.set(cx_,cy_,MORT)
            for k in range(rng.randrange(2,5)):
                px=sx+2+rng.randrange(max(1,w-4)); py=y+2+rng.randrange(max(1,h-4))
                if x0<=px<x1 and y0<=py<y1: c.set(px,py,sh(base,rng.choice([-0.18,0.16,-0.1])))
            if rng.random()<0.16:
                px=sx+3+rng.randrange(max(1,w-6))
                for j in range(rng.randrange(2,4)):
                    if x0<=px+(j%2)<x1 and y0<=y+2+j<y1: c.set(px+(j%2),y+2+j,sh(base,-0.38))
        y+=h

def plaster(c,x0,x1,y0,y1,seed):
    rng=random.Random(seed)
    for y in range(y0,y1):
        for x in range(x0,x1):
            t=(y-y0)/max(1,y1-y0-1)
            col=mix(PL[3],PL[1],t*0.75)
            n=hsh(x//2,y//2,seed)%13
            if n==0: col=sh(col,-0.09)
            elif n==1: col=sh(col,0.06)
            c.set(x,y,col)
    for k in range(max(1,(x1-x0)//18)):      # remendos de reboco caído mostrando a pedra
        if rng.random()<0.35:
            px=x0+3+rng.randrange(max(1,x1-x0-9)); py=y1-8+rng.randrange(5)
            for j in range(5):
                for i in range(rng.randrange(3,6)): c.set(px+i,py+j,sh(S[2],-0.1+0.05*j))

def eave_shadow_w(c,w):
    for y,g in enumerate([-0.70,-0.62,-0.52,-0.42,-0.32,-0.22,-0.14,-0.08,-0.04]):
        for x in range(w): c.set(x,y,sh(c.get(x,y),g))
    c.rect(0,0,w,1,OUT)
    for x0 in range(4,w-4,32):
        c.rect(x0,3,4,5,W[2]); c.rect(x0,3,1,5,W[4]); c.rect(x0+3,3,1,5,W[0]); c.rect(x0+1,8,2,1,W[0])

def win_upper(c,cx,y0=11,y1=25):
    x0=cx-8; x1=cx+8
    c.rect(x0-2,y0-2,x1-x0+5,y1-y0+4,OUT)
    c.rect(x0-1,y0-1,x1-x0+3,y1-y0+3,W[1]); c.rect(x0-1,y0-1,x1-x0+3,1,W[4])
    for y in range(y0,y1+1):
        for x in range(x0,x1+1): c.set(x,y,mix(GLOW[0],GLOW[2],(y-y0)/(y1-y0)))
    c.rect(cx,y0,1,y1-y0+1,W[2]); c.rect(x0,y0+6,x1-x0+1,1,W[2])
    c.set(x0+2,y0+3,(255,255,255,255)); c.set(x0+3,y0+2,(255,255,255,255)); c.set(x0+1,y0+4,(255,255,255,255))
    for yy in range(y0+1,y1+1):                      # cortina
        c.set(x0,yy,C('a83a3a') if yy%2 else C('7a2630')); c.set(x1,yy,C('a83a3a') if yy%2 else C('7a2630'))
    for sx in (x0-7,x1+3):                           # venezianas
        for y in range(y0-1,y1+2):
            for x in range(sx,sx+5):
                col=[TEAL[0],TEAL[3],TEAL[2]][(y-y0)%3]
                if x==sx: col=sh(col,0.2)
                if x==sx+4: col=sh(col,-0.35)
                c.set(x,y,col)
        c.rect(sx,y0-2,5,1,OUT); c.rect(sx,y1+2,5,1,OUT)
    c.rect(x0-4,y1+2,x1-x0+9,3,S[3]); c.rect(x0-4,y1+2,x1-x0+9,1,S[4]); c.rect(x0-4,y1+4,x1-x0+9,1,S[0])
    gs=[C('2a7a3a'),C('3f9a4a'),C('62bc5a')]; fl=[C('ee5060'),C('f8d848'),C('ffffff'),C('ff90b8')]
    for k in range(x1-x0+7):
        xx=x0-3+k; c.set(xx,y1+1,gs[k%3])
        if k%2==0: c.set(xx,y1,fl[(k*3+cx)%4])
    halo(c,cx,(y0+y1)//2,16,GLOW[1],0.10)

def sign_board(c,ox,oy):
    t=Cv(32,40)
    T.sign_upper(t)
    sh_=Cv(32,40)
    for y in range(40):
        for x in range(32):
            if t.get(x,y)[3]>128: sh_.set(x+2,y+2,(30,16,10,90))
    c.blit(sh_,ox,oy); c.blit(t,ox,oy)

def banner(c,x,y):
    c.rect(x-1,y,14,2,I[1]); c.rect(x-1,y,14,1,I[3]); c.set(x-2,y,G[2]); c.set(x+13,y,G[2])
    for yy in range(y+2,y+26):
        w=12 if yy<y+20 else 12-(yy-(y+20))*2
        off=(12-w)//2
        for xx in range(x+off,x+off+w):
            col=C('b83a30') if (xx+yy//4)%3 else C('9a2a28')
            if xx==x+off: col=sh(col,0.2)
            if xx==x+off+w-1: col=sh(col,-0.35)
            c.set(xx,yy,col)
    c.rect(x,y+2,12,1,G[1]); c.rect(x+3,y+7,6,5,G[1]); c.rect(x+3,y+7,1,5,G[3]); c.rect(x+9,y+8,2,3,G[2])
    c.rect(x+3,y+6,6,1,C('fff6e0'))
    for k in range(4): c.set(x+1+k*3,y+24-k%2 if False else y+20+2,G[2])

def lantern_wall(c,x,y):
    c.rect(x-1,y-12,2,16,I[1]); c.rect(x-1,y-12,1,16,I[3])      # braço vertical na parede
    c.rect(x-1,y-12,9,2,I[1]); c.rect(x-1,y-12,9,1,I[3])        # braço horizontal
    c.set(x+7,y-10,I[1]); c.set(x+7,y-9,I[1])
    c.rect(x+3,y-8,9,2,I[1]); c.rect(x+4,y-9,7,1,I[3])
    c.rect(x+3,y-6,9,12,I[0]); c.rect(x+4,y-5,7,10,GLOW[2]); c.rect(x+5,y-4,5,8,GLOW[1]); c.rect(x+7,y-3,1,6,GLOW[0])
    c.rect(x+3,y+6,9,2,I[1]); c.set(x+7,y+8,I[1])
    halo(c,x+7.5,y,16,GLOW[1],0.30)

def door_into(c,frame):
    o=frame/4
    def din(x,y,g):
        fx=x+.5; fy=y+.5; half=WD/2+g; tp=TOP-g
        if fy>=SPRING: return abs(fx-CX)<=half and y<=DBOT
        if fy<tp: return False
        t=(SPRING-fy)/(SPRING-tp)
        return abs(fx-CX)<=half*(1-t**2.0)
    # arco de pedra com aduelas
    for y in range(TOP-9,DBOT+3):
        for x in range(LO-9,HI+10):
            if din(x,y,6) and not din(x,y,5): c.set(x,y,OUT)
            if din(x,y,5) and not din(x,y,0):
                if y<SPRING:
                    ang=math.degrees(math.atan2(x+.5-CX,-(y+.5-SPRING)))
                    seg=int(((ang+90)%180)//15)
                    col=[S[3],S[2],S[3],S[4]][seg%4]
                    if (ang+90)%15<1.3: col=MORT
                else:
                    col=S[3] if ((y//7)%2==0) else S[2]
                    if y%7==0: col=MORT
                if not din(x,y,4): col=sh(col,0.18)
                elif din(x,y,1): col=sh(col,-0.32)
                c.set(x,y,col)
    ix=int(CX)
    c.rect(ix-3,TOP-9,6,8,S[4]); c.rect(ix-3,TOP-9,6,1,(235,225,200,255)); c.rect(ix-3,TOP-2,6,1,S[1])
    c.rect(ix-2,TOP-7,4,4,G[1]); c.rect(ix-2,TOP-7,1,4,G[3]); c.rect(ix-2,TOP-8,4,1,C('fff6e0'))
    wl0=WD//2; wl=round(wl0*(1-0.86*o)); hh=DBOT-TOP+1
    def leaf(lx,ly):
        base=A[[1,2,1,3][(lx//4)%4]]; col=base
        if lx%4==3: col=sh(base,-0.4)
        elif lx%4==0: col=sh(base,0.16)
        elif (lx*5+ly*2)%9==0: col=sh(base,-0.14)
        col=sh(col,(lx/wl0)*0.10-0.06)
        for sy in (int(hh*.18),int(hh*.5),int(hh*.82)):
            if sy<=ly<=sy+2 and lx<=wl0-5: col=I[3] if ly==sy else (I[1] if ly==sy+1 else I[0])
            ex=wl0-4
            if abs(lx-ex)<=1 and sy-1<=ly<=sy+3: col=I[1]
        wx0=wl0//2-3
        if wx0<=lx<=wx0+6 and 7<=ly<=14:
            col=I[1] if (lx in (wx0,wx0+6) or ly in (7,14) or (lx-wx0)%2==0) else C('15101a')
            if col!=I[1] and 9<=ly<=10: col=GLOW[1]
        ky=int(hh*.52)
        dd=math.hypot(lx+.5-(wl0-5),ly+.5-ky)
        if dd<2.3: col=G[3] if dd<1.1 else G[2]
        elif 3.0<=dd<4.5 and ly>ky-1: col=G[0]
        if ly>hh-3: col=sh(col,-0.3)
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
                a=max(0,min(255,int(0xC8*(1-(y-TOP)/50)+0x38)))
                c.set(x,y,(0x30,0x1c,0x0c,a))
    # degraus de pedra em leque
    for (y,xa,xb,tone) in ((BASE,LO-6,HI+7,3),(BASE+4,LO-11,HI+12,2),(BASE+8,LO-16,HI+17,1)):
        c.rect(xa,y,xb-xa,4,S[tone]); c.rect(xa,y,xb-xa,1,S[4]); c.rect(xa,y+3,xb-xa,1,S[0])
        for xx in range(xa+5,xb,11): c.set(xx,y+1,MORT); c.set(xx,y+2,MORT)
        c.set(xa,y,OUT); c.set(xb-1,y,OUT)
    if frame>0:
        for y in range(BASE,BASE+10):
            for x in range(LO+2,HI-1):
                q=c.get(x,y)
                if q[3]: c.set(x,y,mix(q,GLOW[1],0.26*frame/4*(1-(y-BASE)/10)))

def facade(frame=0):
    c=Cv(FW,FH); rng=random.Random(5)
    # ---- andar de cima em enxaimel
    plaster(c,0,FW,6,28,41)
    for i in range(7):
        x0=POSTS[i]+4; x1=POSTS[i+1]
        if i in (0,6):
            brace(c,x0+2,10,x1-8,26,3) if i==0 else brace(c,x0+6,26,x1-4,10,3)
        elif i in (2,4):
            brace(c,x0+2,25,x1-8,9,3) if i==2 else brace(c,x0+2,9,x1-8,25,3)
    beam_h(c,0,6,FW,4)
    for px in POSTS: beam_v(c,px,6,4,22)
    for cx in (98,350): win_upper(c,cx)
    # balcão central com janela em arco
    x0,x1,y0,y1=210,238,9,24
    c.rect(x0-4,y0-3,x1-x0+9,y1-y0+7,OUT)
    for y in range(y0-2,y1+1):
        for x in range(x0-2,x1+3):
            if y<y0+8 and ((x+.5-224)**2/(15**2)+((y+.5-(y0+8))/10)**2)>1: continue
            col=mix(GLOW[0],GLOW[2],(y-y0)/(y1-y0))
            if (x-224)%7==0 or (y-y0)%8==0: col=W[1]
            c.set(x,y,col)
    for k in range(4): c.set(216+k,y0+6-k,(255,255,255,255))
    for yy in range(y0+2,y1+1):
        for k in range(3):
            col=C('a83a3a') if (yy+k)%2 else C('7a2630'); c.set(x0-2+k,yy,col); c.set(x1+2-k,yy,col)
    c.rect(x0-8,y1+1,x1-x0+17,1,I[2]); c.rect(x0-8,y1+5,x1-x0+17,1,I[1])
    for xx in range(x0-7,x1+9,3): c.rect(xx,y1+2,1,3,I[1])
    c.rect(x0-8,y1+6,x1-x0+17,2,W[3]); c.rect(x0-8,y1+6,x1-x0+17,1,W[5])
    for k in range(10):
        xx=x0-6+k*5; c.set(xx,y1,[C('ee5060'),C('f8d848'),C('ff90b8')][k%3]); c.set(xx+1,y1,C('3f9a4a'))
    halo(c,224,16,22,GLOW[1],0.16)
    sign_board(c,138,9); banner(c,284,9)
    c.rect(284,9,0,0,I[1])
    # ---- viga mestra (sobrado) com mísulas
    beam_h(c,0,27,FW,5); c.rect(0,32,FW,1,W[0])
    for px in POSTS[1:-1]:
        for k in range(8): c.rect(px-3+(k//3),33+k,10-2*(k//3),1,W[2] if k%2 else W[3])
    # ---- andar de baixo em pedra + soleira
    stone_wide(c,0,FW,33,BASE,seed=11)
    for yy in range(33,48):
        t=(yy-33)/14
        for x in range(FW):
            q=c.get(x,yy)
            if q[3]: c.set(x,yy,sh(q,-0.55*(1-t)**1.6))
    stone_wide(c,0,FW,BASE,FH,seed=23,dark=-0.20,big=True)
    c.rect(0,BASE,FW,1,sh(S[4],0.05)); c.rect(0,BASE+1,FW,1,MORT)
    for x in range(FW):
        c.set(x,FH-1,OUT)
        if rng.random()<0.5: c.set(x,FH-2-rng.randrange(3),rng.choice([C('3e6e2c'),C('58903a'),C('7cb44c')]))
    # cunhais nos cantos
    for k,yy in enumerate(range(34,BASE,9)):
        w=9 if k%2==0 else 6
        for (xa,xb) in ((0,w),(FW-w,FW)):
            c.rect(xa,yy,xb-xa,8,S[3]); c.rect(xa,yy,xb-xa,1,S[4]); c.rect(xa,yy+7,xb-xa,1,S[0])
            c.rect(xa if xa==0 else xb-1,yy,1,8,S[4] if xa==0 else S[0])
    # janelas do térreo (reaproveita a janela da v2, subindo 8 px)
    for cx in (48,144,304,400):
        t=Cv(32,96); T.window_lower(t,random.Random(cx)); 
        tt=Cv(32,96)
        for y in range(8,96):
            for x in range(32): tt.set(x,y-8,t.get(x,y))
        c.blit(tt,cx-16,0)
    lantern_wall(c,172,58); lantern_wall(c,272,58)
    door_into(c,frame)
    # vasos na soleira junto aos degraus
    for (px) in (176,264):
        pass
    for px in POSTS: pass
    for px in (0,444): beam_v(c,px,6,4,22)
    eave_shadow_w(c,FW)
    return c

def crop(c,x,w):
    o=Cv(w,c.h)
    for y in range(c.h):
        for xx in range(w): o.set(xx,y,c.get(x+xx,y))
    return o

if __name__=='__main__':
    f=facade(0); f.save('/home/claude/out/fac0.png',3)
    facade(4).save('/home/claude/out/fac4.png',3)
