# Taverna v4 - telhado, parede de fundo (andar de cima), piso, balaustrada, escadas e camas
from lib import *
from ext3 import crop

RW,RH=630,222
def roof():
    c=Cv(RW,RH)
    x0,x1=10,RW-10
    for y in range(2,RH-3):
        for x in range(x0,x1):
            row=(y-2)//9; off=(row%2)*7
            tx=(x-x0+off)%14; ty=(y-2)%9
            k=2+((H((x-x0+off)//14,row,5))%3)-1
            col=RT[max(0,min(4,k))]
            if ty==0: col=sh(col,0.32)
            if ty>=7: col=sh(col,-0.35-0.1*(ty-7))
            if tx==0: col=sh(col,-0.3)
            if 4<=ty<=6 and (tx<2 or tx>11): col=sh(col,-0.18)
            t=y/RH; col=sh(col,0.10-0.28*t)
            c.set(x,y,col)
    # cumeeira (topo)
    for x in range(x0-2,x1+2):
        c.set(x,2,RT[0]); c.rect(x,3,1,5,RT[4] if x%12<6 else RT[3]); c.set(x,8,RT[0])
    # beiral inferior com sombra e telhas de borda
    for x in range(x0-4,x1+4):
        for y in range(RH-8,RH-2): c.set(x,y,mix(RT[1],RT[0],(y-(RH-8))/5.0))
        c.set(x,RH-3,RT[0]); c.set(x,RH-2,(34,22,58,255))
    for x in range(x0-4,x1+4,14): c.rect(x,RH-9,13,2,sh(RT[3],0.0)); c.rect(x,RH-9,13,1,RT[4])
    # laterais (empena) em madeira
    for y in range(2,RH-2):
        for x in range(2,x0+2):
            c.set(x,y,WD[2] if x>3 else WD[0]); c.set(RW-1-x,y,WD[2] if x>3 else WD[0])
    # empena central com janela redonda e cata-vento
    gx=RW//2; gw=100; gtop=112
    for y in range(gtop,RH-8):
        half=int((y-gtop)*gw/(RH-8-gtop)*0.9)
        for x in range(gx-half,gx+half+1):
            t=(y-gtop)/(RH-8-gtop)
            col=mix(PL[3],PL[1],t*0.6)
            if H(x//2,y//2,2)%13==0: col=sh(col,-0.09)
            c.set(x,y,col)
        c.set(gx-half,y,WD[0]); c.set(gx-half+1,y,WD[2]); c.set(gx-half+2,y,WD[3]); c.set(gx+half,y,WD[0]); c.set(gx+half-1,y,WD[2]); c.set(gx+half-2,y,WD[3])
    for k in range(0,gw):
        yy=gtop+int(k*(RH-8-gtop)/gw/0.9) 
    # vigas da empena
    c.rect(gx-1,gtop-6,3,RH-8-gtop+6,WD[1])
    c.rect(gx-gw//2,182,gw,3,WD[2]); c.rect(gx-gw//2,182,gw,1,WD[3])
    c.ell(gx,156,12,12,WD[0]); c.ell(gx,156,10,10,WD[3]); c.ell(gx,156,8,8,GLASS[2]); c.ell(gx,155,4,4,GLASS[3])
    c.rect(gx-8,156,17,1,WD[1]); c.rect(gx,148,1,17,WD[1])
    # cata-vento em forma de caneca
    c.rect(gx,gtop-26,1,22,IRON[1]); c.rect(gx-3,gtop-14,7,1,IRON[2])
    c.rect(gx+1,gtop-24,10,9,GOLD[2]); c.rect(gx+1,gtop-24,10,2,GOLD[3]); c.rect(gx+11,gtop-22,3,5,GOLD[1]); c.rect(gx+1,gtop-16,10,1,GOLD[0])
    c.ell(gx,gtop-27,2,2,GOLD[3])
    # lucarnas
    for dx in (60,202,RW-202,RW-60):
        c.poly([(dx-18,182),(dx+18,182),(dx,150)],RT[3]); c.line(dx-18,182,dx,150,RT[0]); c.line(dx+18,182,dx,150,RT[0])
        c.rect(dx-14,182,28,22,PL[2]); c.rect(dx-14,182,28,1,WD[0])
        c.rect(dx-10,185,20,15,GLASS[2]); c.rect(dx-10,185,20,3,GLASS[3]); c.rect(dx-1,185,2,15,WD[1]); c.rect(dx-10,192,20,2,WD[1])
        c.rect(dx-12,183,2,19,WD[2]); c.rect(dx+10,183,2,19,WD[2])
        c.rect(dx-12,204,24,4,WD[0])
    # chamines de pedra (fumaca nasce em y=96)
    for cx_ in (145,511):
        c.rect(cx_-12,110,24,80,MT)
        for yy in range(112,190,6):
            for k,xx in enumerate(range(cx_-11,cx_+11,8)):
                xo=xx+(4 if (yy//6)%2 else 0)
                col=ST[2] if (k+yy//6)%2 else ST[1]
                c.rect(max(cx_-11,xo),yy,min(7,cx_+11-max(cx_-11,xo)),5,col); c.rect(max(cx_-11,xo),yy,min(7,cx_+11-max(cx_-11,xo)),1,ST[3])
        c.rect(cx_-14,106,28,5,ST[3]); c.rect(cx_-14,106,28,1,ST[4]); c.rect(cx_-14,110,28,1,ST[0])
        c.rect(cx_-8,104,16,2,ST[0]); c.rect(cx_-6,105,12,1,(20,12,10,255))
        c.rect(cx_-12,110,2,80,ST[3]); c.rect(cx_+10,110,2,80,ST[0])
    # bandeirinhas da cumeeira
    for xx in range(30,RW-30,26):
        if abs(xx-RW//2)<80: continue
        col=[RED[2],TEAL[2],GOLD[2],C('c060d0')][(xx//26)%4]
        c.poly([(xx,9),(xx+9,9),(xx+4,19)],col)
    return c

BW=608; BH=96
def backstrip(fr):
    c=Cv(BW,BH)
    # viga do teto
    for x in range(BW):
        for y in range(0,12):
            c.set(x,y,sh(WD[1] if y<9 else WD[0],0.0)); 
    wood(c,0,0,BW,10,True,3,2); c.rect(0,10,BW,2,WD[0])
    for x in range(0,BW,32): c.rect(x+12,2,8,6,WD[3]); c.rect(x+12,2,8,1,WD[4])
    # reboco superior
    plaster(c,0,BW,12,66,seed=11)
    for x in range(BW):
        for y in range(12,20): c.set(x,y,sh(c.get(x,y),-0.28+0.03*(y-12)))
    # lambril de madeira
    for x in range(BW):
        for y in range(66,BH):
            col=WD[2] if (x//8)%2 else WD[1]
            if x%8==0: col=WD[0]
            if y<69: col=WD[3]
            if y>=BH-4: col=WD[0]
            c.set(x,y,col)
    c.rect(0,64,BW,3,WD[4]); c.rect(0,66,BW,1,WD[0])
    # postes a cada 5 colunas
    for rel in (0,4,8,10,14,18):
        px=rel*32+(0 if rel==0 else (24 if rel==18 else 14)); beam(c,px,12,8,54,seed=rel)
    def X(rel): return rel*32+16
    # janelas altas em arco (c3 e c11) com luar azulado
    for rel in (3,15):
        cx_=X(rel); 
        c.rect(cx_-14,22,28,42,WD[0]); c.rect(cx_-13,23,26,40,ST[3])
        for yy in range(26,62):
            for xx in range(cx_-10,cx_+10):
                t=(yy-26)/36; c.set(xx,yy,mix(C('b0d0ff'),C('4a6aa8'),t))
        c.ell(cx_,30,10,6,C('d8e8ff'))
        c.rect(cx_-1,24,2,38,WD[1]); c.rect(cx_-10,42,20,2,WD[1])
        c.rect(cx_-16,60,32,5,ST[4]); c.rect(cx_-16,64,32,1,ST[0])
        for sx in (cx_-19,cx_+13): 
            c.rect(sx,22,6,42,TEAL[1])
            for yy in range(23,63,3): c.rect(sx+1,yy,4,1,TEAL[2])
            c.rect(sx,22,1,42,TEAL[0]); c.rect(sx+5,22,1,42,TEAL[0])
    # portas dos quartos (c5 e c9) com placa
    for rel in (6,12):
        cx_=X(rel)
        c.rect(cx_-12,24,24,42,WD[0]); c.rect(cx_-11,25,22,41,WD[2])
        for k in range(4): c.rect(cx_-10+k*5,26,1,40,WD[1])
        c.rect(cx_-11,36,22,2,IRON[1]); c.rect(cx_-11,54,22,2,IRON[1])
        c.ell(cx_+6,46,2,2,GOLD[2])
        c.rect(cx_-5,16,10,6,GOLD[1]); c.rect(cx_-5,16,10,1,GOLD[3]); c.rect(cx_-3,18,6,2,WD[0])   # plaquinha do quarto
        c.rect(cx_-14,22,28,3,WD[3]); 
    # estandarte central (c7)
    cx_=X(9)
    c.rect(cx_-12,16,24,2,IRON[1]); c.rect(cx_-9,18,18,40,RED[1]); c.rect(cx_-9,18,18,3,RED[0])
    for k in range(9): c.rect(cx_-9+k*2,58,2,6-abs(k-4),RED[1] if k%2 else RED[0])
    c.rect(cx_-9,18,2,40,RED[2]); c.rect(cx_+7,18,2,40,RED[0])
    c.rect(cx_-5,28,10,12,GOLD[2]); c.rect(cx_-5,28,10,3,GOLD[3]); c.rect(cx_+5,31,4,6,GOLD[1]); c.rect(cx_-5,38,10,2,GOLD[0])
    # tochas animadas (c1 e c13) 
    for rel in (1,17):
        cx_=X(rel)
        c.rect(cx_-2,40,5,12,IRON[1]); c.rect(cx_-4,38,9,3,IRON[2]); c.rect(cx_-1,52,3,6,IRON[0])
        f=fr
        c.poly([(cx_-3,38),(cx_+3,38),(cx_+(1 if f else -1),26-f*2)],C('ff7a1a'))
        c.poly([(cx_-2,38),(cx_+2,38),(cx_+(1 if f else -1),30-f*2)],C('ffc83a'))
        c.rect(cx_-1,33,2,5,C('fff4b0'))
        for dx in range(-9,10):
            for dy in range(-12,10):
                d=math.hypot(dx,dy*0.9)
                if d<10 and c.get(cx_+dx,34+dy)[3]>0 and c.get(cx_+dx,34+dy)!=C('ff7a1a'):
                    c.set(cx_+dx,34+dy,mix(c.get(cx_+dx,34+dy),C('ffd070'),0.28*(1-d/10)))
    # escudos/trofeus: cabeca de javali pequena? -> escudos pendurados c2,c12 ... c4,c10
    for rel in (7,):
        pass
    return c

def floor_hall(v):
    c=Cv(32,32)
    pl=[0,11,22]; 
    for i,(y0,h) in enumerate(((0,11),(11,10),(21,11))):
        for y in range(y0,y0+h):
            for x in range(32):
                xo=((x+(v*9+i*13))%32)
                col=WD[3] if i%2==0 else mix(WD[3],WD[2],0.5)
                n=H(x//3,y//2,v*7+i)%9
                if n==0: col=sh(col,-0.1)
                if y==y0: col=sh(col,-0.35)
                elif y==y0+1: col=sh(col,0.15)
                c.set(x,y,col)
        jx=(v*11+i*17)%28+2
        for y in range(y0,y0+h): c.set(jx,y,sh(WD[3],-0.4))
    return c

def floor_loft(v):
    c=Cv(32,32)
    for i,(y0,h) in enumerate(((0,8),(8,8),(16,8),(24,8))):
        for y in range(y0,y0+h):
            for x in range(32):
                col=WD[2] if (i+v)%2==0 else mix(WD[2],WD[1],0.5)
                n=H(x//2,y,v*5+i)%10
                if n==0: col=sh(col,-0.12)
                elif n==1: col=sh(col,0.08)
                if y==y0: col=sh(col,-0.4)
                c.set(x,y,col)
        jx=(v*7+i*19)%26+3
        for y in range(y0,y0+h): c.set(jx,y,sh(WD[2],-0.45))
    for x in range(32): c.set(x,31,sh(c.get(x,31),-0.2))
    return c

def rail(kind):
    """balaustrada 32x48 (parte de baixo 32px e' o quadrado); 0 ponta esq., 1 meio, 2 ponta dir., 3 meio c/ lampiao"""
    c=Cv(32,48)
    # faixa/rodape do piso (fachada da plataforma) na base
    wood(c,0,40,32,8,True,kind+3,1); c.rect(0,40,32,1,WD[3]); c.rect(0,47,32,1,WD[0])
    # corrimao
    wood(c,0,18,32,4,True,kind,3); c.rect(0,18,32,1,WD[4]); c.rect(0,21,32,1,WD[0])
    for x in range(4,30,5):
        c.rect(x,22,3,18,WD[2]); c.rect(x,22,1,18,WD[3]); c.rect(x+2,22,1,18,WD[0])
        c.ell(x+1.5,30,2.2,2.2,WD[3]); c.set(x+1,29,WD[4])
    c.rect(0,38,32,3,WD[1]); c.rect(0,38,32,1,WD[3])
    if kind in (0,2):
        px=0 if kind==0 else 26
        c.rect(px,10,6,32,WD[1]); c.rect(px,10,6,32,WD[2]); c.rect(px,10,1,32,WD[3]); c.rect(px+5,10,1,32,WD[0])
        c.rect(px-1,8,8,3,WD[3]); c.rect(px-1,8,8,1,WD[4]); c.ell(px+3,6,3,3,GOLD[2]); c.set(px+2,5,GOLD[3])
    if kind==3:
        c.rect(14,10,4,8,WD[1]); lantern(c,15,12,True) if False else None
        # lampiao pendurado na viga
        c.rect(15,0,2,8,IRON[1]); lantern(c,16,6,True)
        for yy in range(0,3): pass
    return c

def stairs(side,part):
    """escada subindo para o norte, 32x48. side 0 = parede na esquerda, 1 = na direita. part 0 base,1 meio,2 topo"""
    c=Cv(32,48)
    # degraus (4 por quadrado)
    for k in range(4):
        y=16+k*8
        top=sh(WD[3],0.1+0.04*part)
        c.rect(0,y,32,5,mix(WD[4],WD[3],0.4)); c.rect(0,y,32,1,sh(WD[4],0.2)); 
        c.rect(0,y+5,32,3,WD[1]); c.rect(0,y+7,32,1,WD[0])
        for x in range(0,32,7): c.set(x+(k*3)%5,y+2,sh(WD[3],-0.18))
    # laterais (longarinas)
    for (x0,w) in ((0,4),(28,4)):
        c.rect(x0,16,w,32,WD[1]); c.rect(x0,16,1,32,WD[3]); c.rect(x0+w-1,16,1,32,WD[0])
    # corrimao no lado de dentro do salao
    ix=28 if side==0 else 0
    for y in range(8,44,8):
        c.rect(ix+1,y,2,12,WD[2])
    wood(c,ix,6 if part!=2 else 4,4,40,False,part+5,3)
    c.rect(ix-1,2,6,4,WD[3]); c.ell(ix+2,2,2.5,2.5,GOLD[2]) if part!=1 else None
    # sombra do fim do degrau no topo
    if part==2:
        c.rect(0,40,32,8,WD[2]); c.rect(0,40,32,1,WD[0])
    return c

def bed(kind):
    c=Cv(32,48)
    blank=[RED,[C('1d2c52'),C('2f4c8c'),C('4a7ac8'),C('8ab0f0')]][kind]
    c.rect(1,6,30,40,WD[0]); c.rect(2,7,28,38,WD[2])
    wood(c,1,2,30,10,True,kind+9,1); c.rect(1,2,30,1,WD[4]); c.rect(1,11,30,1,WD[0])
    c.ell(7,3,2.5,2.5,WD[4]); c.ell(24,3,2.5,2.5,WD[4])
    c.rect(4,12,24,32,C('e9e2d0'))
    c.rect(5,13,10,7,C('ffffff')); c.rect(17,13,10,7,C('ffffff')); c.rect(5,19,10,1,C('c8c0b0')); c.rect(17,19,10,1,C('c8c0b0'))
    c.rect(4,22,24,22,blank[1]); c.rect(4,22,24,3,blank[3]); c.rect(4,22,24,1,blank[3])
    for y in range(26,44,6): c.rect(4,y,24,1,blank[0])
    for x in range(8,28,8): c.rect(x,25,1,19,blank[2])
    c.rect(1,40,30,6,WD[1]); c.rect(1,40,30,1,WD[3]); c.rect(1,45,30,1,WD[0])
    c.rect(2,44,3,3,WD[0]); c.rect(27,44,3,3,WD[0])
    return c

if __name__=='__main__':
    r=roof(); r.save('/home/claude/out/roof.png',2)
    for fr in (0,1): backstrip(fr).save(f'/home/claude/out/bk{fr}.png',2)
    P=Cv(32*10,48*2)
    for i in range(4): P.blit(rail(i),i*32,0)
    for s in (0,1):
        for p in range(3): P.blit(stairs(s,p),(4+p)*32+(0 if s==0 else 0),0 if s==0 else 48)
    for k in range(2): P.blit(bed(k),(8+k)*32,0)
    for i in range(4): P.blit(floor_loft(i),i*32,48+0) if False else None
    P.save('/home/claude/out/parts.png',3)
