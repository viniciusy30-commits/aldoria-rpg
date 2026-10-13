from pa import *
import facade as F

LO,HI,TOP,DBOT=14,49,17,60
WD=HI-LO+1
CX=(LO+HI+1)/2
SPRING=TOP+WD*0.58
STONE=C('8a8076'); MORT=C('3a343a'); OUTL=C('2a1a0e')
OAK=C('7a4426')

def din(x,y,g):
    fx=x+.5; fy=y+.5; half=WD/2+g; tp=TOP-g
    if fy>=SPRING: return abs(fx-CX)<=half and y<=DBOT
    if fy<tp: return False
    t=(SPRING-fy)/(SPRING-tp)
    return abs(fx-CX)<=half*(1-t**1.8)

def leaf(lx,ly,wl,hh):
    # grão de madeira com variação por tábua
    pl=lx//4
    base=sh(OAK,[0.0,-0.08,0.05,-0.04][pl%4])
    c=base
    if lx%4==3: c=sh(base,-0.38)
    elif lx%4==0: c=sh(base,0.14)
    elif (lx*5+ly*2)%9==0: c=sh(base,-0.14)
    # luz vindo da esquerda (dobradiça mais escura, ponta livre mais clara)
    c=sh(c,(lx/wl)*0.08-0.04)
    for sy in (int(hh*.2),int(hh*.58),int(hh*.88)):
        if sy<=ly<=sy+2 and lx<=wl-4:
            c=sh(F.IRON,0.2) if ly==sy else F.IRON
            if lx%4==1 and ly==sy+1: c=F.IRONH
        ex=wl-3
        if abs(lx-ex)<=1 and sy-1<=ly<=sy+3: c=F.IRON
        if lx==ex and ly==sy-2: c=F.IRON
        if lx in (ex-1,ex+1) and ly==sy-1: c=F.IRON
    wx0=wl//2-3
    if wx0<=lx<=wx0+6 and 7<=ly<=14:
        c=F.IRON if (lx in (wx0,wx0+6) or ly in (7,14)) else C('15101a')
        if c!=F.IRON and (lx-wx0)%2==0: c=F.IRON
        if c!=F.IRON and 9<=ly<=10: c=C('f2b040')
    ky=int(hh*.5)+4
    dd=math.hypot(lx+.5-(wl-5),ly+.5-ky)
    if dd<2.2: c=F.GOLDH if dd<1.1 else F.GOLD
    elif 3.0<=dd<4.4 and ly>ky-1: c=sh(F.GOLD,-0.2)
    if ly>20 and 1<lx<wl-6 and lx%4==1 and ly%6==3: c=F.IRONH
    if ly>hh-4: c=sh(c,-0.32)
    return c

def lowerdoor(frame):
    big=Cv(64,64)
    for k in (0,1): big.blit(F.lower(4,0,0),k*32,0)
    o=frame/4
    for y in range(64):
        for x in range(64):
            if din(x,y,5) and not din(x,y,4) and y<=DBOT+1: big.set(x,y,OUTL)
            if din(x,y,4) and not din(x,y,0):
                c=STONE
                if not din(x,y,3): c=sh(STONE,0.24)
                elif din(x,y,1): c=sh(STONE,-0.26)
                if y<SPRING:
                    ang=math.degrees(math.atan2(x+.5-CX,-(y+.5-SPRING)))
                    seg=int(((ang+90)%180)//15)
                    if (ang+90)%15<1.5: c=MORT
                    elif seg%2==0 and din(x,y,3) and not din(x,y,1): c=sh(c,0.07)
                elif y%6==0 or (x+(y//6)*3)%7==0: c=MORT
                big.set(x,y,c)
    for y in range(DBOT+1,63):
        for x in range(LO-6,HI+7):
            big.set(x,y,sh(STONE,0.3) if y==DBOT+1 else (C('241f2a') if y==62 else sh(STONE,-0.08)))
    wl0=WD//2; wl=round(wl0*(1-0.86*o)); hh=DBOT-TOP+1
    for y in range(64):
        for x in range(64):
            if not din(x,y,0): continue
            ly=y-TOP; lx=-1; edge=False
            if x<LO+wl: lx=x-LO; edge=(x==LO+wl-1)
            elif x>HI-wl: lx=HI-x; edge=(x==HI-wl+1)
            if lx>=0:
                c=leaf(lx,ly,wl0,hh)
                if edge: c=C('24140a')
                if o>0: c=sh(c,-0.12*o)
                big.set(x,y,c)
            else:
                # vão aberto: saguão quente (semi-transparente)
                a=int(0xB8*(1-(y-8)/56)+0x28); a=max(0,min(255,a))
                big.set(x,y,(0x3a if (x+y)%5==0 else 0x1a,0x20 if (x+y)%5==0 else 0x0e,0x08,a))
    ix=int(CX)
    # chave do arco com caneca
    big.rect(ix-2,TOP-4,4,5,F.GOLD); big.rect(ix-2,TOP-4,4,1,F.GOLDH)
    big.rect(ix-1,TOP-3,2,3,sh(F.GOLD,-0.4)); big.set(ix+2,TOP-2,F.GOLD)
    # toldo de telhas
    for i in range(11):
        xi,xr=5+i,58-i
        for x in range(xi,xr+1):
            col=C('b04830') if ((x+(i//2)*3)//4)%2==0 else C('c85a3a')
            if i%2==1: col=sh(col,-0.24)
            if i==0: col=sh(col,0.2)
            if x in (xi,xr): col=C('4a1f12')
            big.set(x,i,col)
    big.rect(4,11,56,2,F.BM); big.rect(4,11,56,1,F.BML); big.rect(4,12,56,1,F.BMD)
    for px in (7,54):
        big.rect(px,13,3,4,F.BM)
        big.line(px,17,px+(5 if px<30 else -3),13,F.BML)
    # sombra do toldo sobre a porta
    for y in range(13,18):
        for x in range(LO-4,HI+5):
            col=big.get(x,y)
            if col[3]: big.set(x,y,sh(col,-0.3+ (y-13)*0.05))
    # lampião à esquerda
    big.rect(4,20,1,6,F.IRON); big.rect(2,26,5,1,F.IRON); big.rect(3,27,3,6,C('b8741a'))
    big.rect(3,28,3,4,C('ffe066')); big.set(4,29,C('fffbe0')); big.rect(2,33,5,1,F.IRON)
    for y in range(22,40):
        for x in range(0,12):
            d=math.hypot(x-4.5,y-30)
            col=big.get(x,y)
            if d<8 and col[3] and col!=C('ffe066'): big.set(x,y,mix(col,C('ffc860'),0.3*(1-d/8)))
    # placa de caneca à direita
    sx=55
    big.rect(sx-2,19,9,2,F.IRON); 
    for dx in (0,5): big.set(sx+dx,21,F.IRON); big.set(sx+dx,22,F.IRON)
    big.rect(sx-1,23,8,12,C('5a3a1e')); big.rect(sx-1,23,8,1,C('8a5a2b')); big.rect(sx-1,34,8,1,F.BMD)
    big.rect(sx,24,6,10,C('2e1a0c'))
    big.rect(sx+1,27,4,5,F.GOLD); big.rect(sx+1,27,1,5,F.GOLDH); big.rect(sx+1,32,4,1,sh(F.GOLD,-0.35))
    for yy in (28,29,30): big.set(sx+5,yy,F.GOLD)
    big.rect(sx+1,25,4,2,C('fff6e0')); big.set(sx+2,24,(255,255,255,255)); big.set(sx+4,24,(255,255,255,255))
    # vaso e barrilzinho
    big.rect(LO-11,DBOT-6,6,7,C('b8602e')); big.rect(LO-11,DBOT-6,6,1,C('d87a3e'))
    for p,cl in (((LO-10,DBOT-8),'d84a5a'),((LO-8,DBOT-9),'f4d060'),((LO-7,DBOT-8),'ffffff'),((LO-9,DBOT-7),'3e7a3a'),((LO-8,DBOT-7),'3e7a3a'),((LO-10,DBOT-7),'3e7a3a')):
        big.set(p[0],p[1],C(cl))
    big.rect(HI+5,DBOT-5,6,6,C('8a5a2b')); big.rect(HI+5,DBOT-5,1,6,C('b07a44')); big.rect(HI+5,DBOT-3,6,1,F.IRON)
    return big

def door(frame):
    c=Cv(64,96)
    rn=random.Random(77)
    for k in (0,1): c.blit(F.upper(0,0,random.Random(31+k)),k*32,0)
    # janela-sacada central (arco) por cima da porta
    x0,x1,y0,y1=22,41,7,26
    c.rect(x0-3,y0-2,x1-x0+7,y1-y0+5,F.BMD)
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            t=(y-y0)/(y1-y0)
            if y<y0+6 and ((x+.5-(x0+x1+1)/2)**2/((x1-x0+1)/2)**2+((y+.5-(y0+6))/6)**2)>1: continue
            col=mix(C('c8e6f4'),C('5e9ec6'),t)
            if (x+y)%6==0 or (x-y+200)%6==0: col=C('34343e')
            d=math.hypot(x-31.5,y-23)
            if d<9: col=mix(col,C('ffd878'),0.7*(1-d/9))
            c.set(x,y,col)
    c.rect(x0-1,y0+6,1,y1-y0-5,F.BM); c.rect(x1+1,y0+6,1,y1-y0-5,F.BM)
    for k in range(5): c.set(x0+2+k,y0+10-k,(255,255,255,255))
    # cortinas
    for y in range(y0+4,y1-2):
        for k in range(3):
            col=C('8e2a33') if k%2==0 else sh(C('8e2a33'),-0.25)
            c.set(x0+k,y,col); c.set(x1-k,y,col)
    # sacada de ferro
    c.rect(x0-5,y1+1,x1-x0+11,1,F.IRONH); c.rect(x0-5,y1+2,x1-x0+11,2,F.BM); c.rect(x0-5,y1+2,x1-x0+11,1,F.BML)
    for x in range(x0-4,x1+6,3): c.rect(x,y1-5,1,6,F.IRON)
    c.rect(x0-5,y1-5,x1-x0+11,1,F.IRON)
    # fresta de bandeirolas penduradas na sacada
    cols=[C('b8372f'),C('f2e6cc'),C('2f8c86'),C('d8a640')]
    for i,x in enumerate(range(x0-3,x1+4,5)):
        c.rect(x,y1+4,3,3,cols[i%4]); c.set(x+1,y1+7,cols[i%4])
    c.blit(lowerdoor(frame),0,32)
    return c

if __name__=='__main__':
    row=Cv(64*5+40,96)
    for f in range(5): row.blit(door(f),f*72,0)
    row.save('/home/claude/gen/out/prev_door.png',3)
