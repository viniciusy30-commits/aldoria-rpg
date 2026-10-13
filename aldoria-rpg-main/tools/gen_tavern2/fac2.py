# Fachada de 4 quadrados de altura (448x128): andar de cima alto, pedra embaixo, porta recuada.
import ext as E
from ext import *
E.FH=128; E.BASE=116; E.TOP=74; E.DBOT=115; E.SPRING=E.TOP+E.WD*0.5
FH=128; BASE=116
UP0,UP1=10,50      # painéis do andar de cima
JB=51              # viga mestra (sobrado)

def win_up(c,cx,y0=17,y1=40,hw=9):
    x0=cx-hw; x1=cx+hw
    c.rect(x0-2,y0-2,x1-x0+5,y1-y0+4,OUT)
    c.rect(x0-1,y0-1,x1-x0+3,y1-y0+3,W[1]); c.rect(x0-1,y0-1,x1-x0+3,1,W[4])
    for y in range(y0,y1+1):
        for x in range(x0,x1+1): c.set(x,y,mix(GLOW[0],GLOW[2],(y-y0)/(y1-y0)))
    c.rect(cx,y0,1,y1-y0+1,W[2])
    for yy in range(y0+7,y1,8): c.rect(x0,yy,x1-x0+1,1,W[2])
    for k in range(3): c.set(x0+2+k,y0+4-k,(255,255,255,255))
    for yy in range(y0+1,y1+1):
        cc=C('a83a3a') if yy%2 else C('7a2630')
        for k in range(2+(yy-y0)//12): c.set(x0+k,yy,cc); c.set(x1-k,yy,cc)
    for sx in (x0-8,x1+3):
        for y in range(y0-1,y1+2):
            for x in range(sx,sx+5):
                col=[TEAL[0],TEAL[3],TEAL[2]][(y-y0)%3]
                if x==sx: col=sh(col,0.2)
                if x==sx+4: col=sh(col,-0.35)
                c.set(x,y,col)
        c.rect(sx,y0-2,5,1,OUT); c.rect(sx,y1+2,5,1,OUT)
        c.set(sx+(3 if sx<cx else 1),(y0+y1)//2,G[2])
    c.rect(x0-4,y1+2,x1-x0+9,3,S[3]); c.rect(x0-4,y1+2,x1-x0+9,1,S[4]); c.rect(x0-4,y1+4,x1-x0+9,1,S[0])
    gs=[C('2a7a3a'),C('3f9a4a'),C('62bc5a')]; fl=[C('ee5060'),C('f8d848'),C('ffffff'),C('ff90b8')]
    c.rect(x0-3,y1+5,x1-x0+7,4,W[3]); c.rect(x0-3,y1+5,x1-x0+7,1,W[5]); c.rect(x0-3,y1+8,x1-x0+7,1,W[0])
    for k in range(x1-x0+7):
        xx=x0-3+k; c.set(xx,y1+4,gs[k%3])
        if k%2==0: c.set(xx,y1+3,fl[(k*3+cx)%4])
    halo(c,cx,(y0+y1)//2,20,GLOW[1],0.10)

def sign_big(c,ox,oy):
    # braço de ferro com volutas, correntes e placa dourada com caneca
    s=Cv(44,44)
    s.rect(0,0,44,3,I[1]); s.rect(0,0,44,1,I[3]); s.rect(0,2,44,1,I[0])
    for k in range(6): s.set(4+k,3+k//2,I[1]); s.set(5+k,4+k//2,I[0])
    s.set(40,3,I[1]); s.set(41,4,I[1]); s.set(40,5,I[1]); s.set(39,4,I[1])
    for cx in (7,35):
        for j in range(3,9): s.set(cx,j,I[3] if j%2 else I[1]); s.set(cx+1,j,I[1])
    s.rect(3,9,38,26,OUT)
    s.rect(4,10,36,24,W[1]); s.rect(5,11,34,22,A[2])
    for xx in range(5,39):
        if xx%6==0: s.rect(xx,11,1,22,A[0])
        if xx%6==1: s.rect(xx,11,1,22,A[3])
    s.rect(4,10,36,1,G[2]); s.rect(4,33,36,1,G[0]); s.rect(4,10,1,24,G[2]); s.rect(39,10,1,24,G[0]); s.set(4,10,G[3])
    for p in ((6,12),(37,12),(6,31),(37,31)): s.set(*p,G[3])
    # caneca
    s.rect(15,17,13,13,C('ecb440')); s.rect(15,17,3,13,G[3]); s.rect(25,17,3,13,G[0])
    for yy in (21,25): s.rect(16,yy,11,1,G[0])
    s.rect(28,19,5,2,G[2]); s.rect(31,19,2,8,G[2]); s.rect(28,25,5,2,G[1])
    s.rect(14,13,15,5,C('fff6e0')); s.rect(16,12,4,1,C('fff6e0')); s.rect(22,11,5,2,(255,255,255,255)); s.rect(14,17,15,1,C('d8c8a0'))
    s.rect(15,29,13,1,G[0])
    for y in range(44):
        for x in range(44):
            if s.get(x,y)[3]>128 and y>6:
                q=c.get(ox+x+3,oy+y+3)
                if q[3]: c.set(ox+x+3,oy+y+3,sh(q,-0.38))
    c.blit(s,ox,oy)

def banner2(c,x,y):
    c.rect(x-2,y,18,2,I[1]); c.rect(x-2,y,18,1,I[3]); c.set(x-3,y,G[2]); c.set(x+16,y,G[2])
    for yy in range(y+2,y+36):
        w=14 if yy<y+28 else 14-(yy-(y+28))*2
        off=(14-w)//2
        for xx in range(x+off,x+off+w):
            col=C('b83a30') if (xx+yy//5)%3 else C('9a2a28')
            if xx==x+off: col=sh(col,0.2)
            if xx==x+off+w-1: col=sh(col,-0.35)
            c.set(xx,yy,col)
    c.rect(x,y+2,14,1,G[1]); c.rect(x,y+4,14,1,G[0])
    c.rect(x+3,y+10,8,8,G[1]); c.rect(x+3,y+10,2,8,G[3]); c.rect(x+11,y+12,2,4,G[2]); c.rect(x+3,y+9,8,1,C('fff6e0')); c.rect(x+3,y+17,8,1,G[0])
    for k in range(5): c.set(x+1+k*3,y+28+(0 if k%2 else 1) if False else y+30-abs(k-2)*2+0,G[2])

def facade2(frame=0):
    c=Cv(448,FH); rng=random.Random(5)
    plaster(c,0,448,6,JB,41)
    for i in range(7):
        x0=POSTS[i]+4; x1=POSTS[i+1]
        if i==0: brace(c,x0+2,14,x1-8,44,3)
        elif i==6: brace(c,x0+6,44,x1-4,14,3)
        elif i==2: pass
        elif i==4: pass
    beam_h(c,0,6,448,4); beam_h(c,0,JB-6,448,3)
    for px in POSTS: beam_v(c,px,6,4,JB-6)
    for cx in (98,350): win_up(c,cx)
    for px0,flip in ((128,0),(256,1)):         # travessas decorativas nos painéis do sinal
        brace(c,px0+5,JB-9,px0+14,JB-18,2) if not flip else brace(c,px0+55,JB-9,px0+46,JB-18,2)
    # balcão em arco sobre a porta
    x0,x1,y0,y1=208,240,14,38
    c.rect(x0-4,y0-3,x1-x0+9,y1-y0+8,OUT)
    for y in range(y0-2,y1+1):
        for x in range(x0-2,x1+3):
            if y<y0+10 and ((x+.5-224)**2/(17**2)+((y+.5-(y0+10))/12)**2)>1: continue
            col=mix(GLOW[0],GLOW[2],(y-y0)/(y1-y0))
            if (x-224)%8==0 or (y-y0)%9==0: col=W[1]
            c.set(x,y,col)
    for k in range(5): c.set(214+k,y0+8-k,(255,255,255,255))
    for yy in range(y0+3,y1+1):
        for k in range(3+(yy-y0)//10):
            col=C('a83a3a') if (yy+k)%2 else C('7a2630'); c.set(x0-2+k,yy,col); c.set(x1+2-k,yy,col)
    c.rect(x0-10,y1+1,x1-x0+21,1,I[2])
    for xx in range(x0-9,x1+11,3): c.rect(xx,y1+2,1,5,I[1])
    c.rect(x0-10,y1+7,x1-x0+21,1,I[1]); c.rect(x0-10,y1+8,x1-x0+21,3,W[3]); c.rect(x0-10,y1+8,x1-x0+21,1,W[5]); c.rect(x0-10,y1+10,x1-x0+21,1,W[0])
    for k in range(12):
        xx=x0-8+k*5; c.set(xx,y1+1,[C('ee5060'),C('f8d848'),C('ff90b8')][k%3]); c.set(xx+1,y1+1,C('3f9a4a')); c.set(xx,y1,[C('f8d848'),C('ff90b8'),C('ee5060')][k%3])
    halo(c,224,26,26,GLOW[1],0.16)
    sign_big(c,134,12); banner2(c,296,12)
    # viga mestra
    beam_h(c,0,JB,448,5); c.rect(0,JB+5,448,1,W[0])
    for px in POSTS[1:-1]:
        for k in range(9): c.rect(px-4+(k//3),JB+6+k,11-2*(k//3),1,W[2] if k%2 else W[3])
    # térreo em pedra
    S0=JB+6
    E.stone_wide(c,0,448,S0,BASE,seed=11)
    for yy in range(S0,S0+16):
        t=(yy-S0)/15
        for x in range(448):
            q=c.get(x,yy)
            if q[3]: c.set(x,yy,sh(q,-0.55*(1-t)**1.6))
    E.stone_wide(c,0,448,BASE,FH,seed=23,dark=-0.20,big=True)
    c.rect(0,BASE,448,1,sh(S[4],0.05)); c.rect(0,BASE+1,448,1,MORT)
    for x in range(448):
        c.set(x,FH-1,OUT)
        if rng.random()<0.5: c.set(x,FH-2-rng.randrange(3),rng.choice([C('3e6e2c'),C('58903a'),C('7cb44c')]))
    for k,yy in enumerate(range(S0,BASE-4,9)):
        w=9 if k%2==0 else 6
        for (xa,xb) in ((0,w),(448-w,448)):
            c.rect(xa,yy,xb-xa,8,S[3]); c.rect(xa,yy,xb-xa,1,S[4]); c.rect(xa,yy+7,xb-xa,1,S[0])
            c.rect(xa if xa==0 else xb-1,yy,1,8,S[4] if xa==0 else S[0])
    for cx in (48,144,304,400):
        t=Cv(32,96); T.window_lower(t,random.Random(cx))
        tt=Cv(32,FH)
        for y in range(8,96):
            for x in range(32): tt.set(x,y+24,t.get(x,y))     # sobe/ desce para alinhar com a porta
        c.blit(tt,cx-16,0)
    E.lantern_wall(c,170,88); E.lantern_wall(c,274,88)
    E.door_into(c,frame)
    for px in (0,444): beam_v(c,px,6,4,JB-6)
    eave_shadow_w(c,448)
    return c

def cut2(open_=True):
    """Corte para quando você está dentro: só um murinho de pedra baixo, com o vão da porta livre."""
    c=Cv(448,FH)
    y0=BASE-14
    E.stone_wide(c,0,448,y0+5,FH,seed=23,dark=-0.12,big=True)
    # tampo do murinho (visto de cima) e sombra na face
    for x in range(448):
        for k,col in enumerate([sh(S[4],0.2),S[4],S[3],S[3],S[2]]): c.set(x,y0+k,col)
        for yy in range(y0+5,y0+11): c.set(x,yy,sh(c.get(x,yy),-0.30*(1-(yy-y0-5)/6)))
        c.set(x,FH-1,OUT)
    # vão da porta: recorta e põe soleira de madeira
    for y in range(y0-1,FH):
        for x in range(E.LO-6,E.HI+7): c.set(x,y,(0,0,0,0))
    for (xa,xb) in ((E.LO-9,E.LO-5),(E.HI+6,E.HI+10)):       # batentes de pedra
        c.rect(xa,y0-14,xb-xa,FH-(y0-14),S[3]); c.rect(xa,y0-14,xb-xa,1,S[4]); c.rect(xa,y0-14,1,FH-(y0-14),S[4]); c.rect(xb-1,y0-14,1,FH-(y0-14),S[0])
        for yy in range(y0-14,FH,7): c.rect(xa,yy,xb-xa,1,MORT)
    # folhas da porta abertas para dentro (vistas de lado)
    for xa in (E.LO-5,E.HI+5):
        c.rect(xa,y0-12,2,12,A[1]); c.rect(xa,y0-12,1,12,A[3]); c.rect(xa+1,y0-12,1,12,A[0])
    for y in range(FH-9,FH):
        for x in range(E.LO-5,E.HI+6):
            u=(x-E.LO)%8
            col=A[2] if u else sh(A[2],-0.4)
            if y==FH-9: col=sh(col,0.25)
            if y==FH-1: col=OUT
            c.set(x,y,col)
    c.outline(OUT)
    return c

if __name__=='__main__':
    facade2(0).save('/home/claude/out/fac2_0.png',3)
    facade2(4).save('/home/claude/out/fac2_4.png',3)
    cut2().save('/home/claude/out/cut2.png',3)
