# Taverna v4 - exterior: fachada 480x192 (15 colunas), porta 96x192, corte baixo e telhado
from lib import *

FW,FH=608,192
UPW=[46,120,194]+[608-46,608-120,608-194]     # centros das janelas do andar de cima
GW=[35,109,477,551]                              # x das janelas do terreo
DX0,DX1=256,352           # vao da porta (3 quadrados)
ARCH_TOP=100; SPRING=132  # topo e inicio do arco
CXD=(DX0+DX1)/2
def in_arch(x,y,pad=0):
    if y<SPRING: 
        r=(DX1-DX0)/2-pad
        return (x+.5-CXD)**2/(r*r)+((y+.5-SPRING)/(SPRING-ARCH_TOP-pad))**2<=1 if y>=ARCH_TOP+pad else False
    return DX0+pad<=x<DX1-pad

def facade_base():
    c=Cv(FW,FH)
    # --- andar de cima: reboco + enxaimel
    plaster(c,0,FW,8,72,seed=7)
    # beiral/sombra do telhado
    for x in range(FW):
        for y in range(8,14): c.set(x,y,sh(c.get(x,y),-0.35+0.05*(y-8)))
    beam(c,0,0,FW,8,seed=2)
    for x in range(0,FW,24): c.rect(x+10,2,2,2,WD[3])
    # postes e travessas
    for px in (0,84,156,232,368,444,516,FW-8): beam(c,px,8,8,64,seed=px)
    beam(c,0,38,FW,4,seed=4)                       # travessa media
    for (a,b) in ((8,84),(92,156),(164,232),(376,444),(452,516),(524,FW-8)):
        # cruz de Sao Andre nos paineis baixos (y 42..70)
        for k in range(0,b-a-8):
            yy=42+int(k*(70-42)/max(1,(b-a-8)))
            c.set(a+4+k,yy,WD[1]); c.set(a+4+k,yy+1,WD[2])
    # janelas do andar de cima
    for cxw in UPW: window_rect(c,cxw-11,16,22,20,seed=cxw)
    # janelas pequenas nos paineis baixos (frestas/postigos)
    # --- viga do jetty (balanco) com mísulas
    beam(c,0,72,FW,8,seed=9)
    for bx in range(10,FW,38):
        if DX0-6<bx<DX1+6: continue
        for k in range(8): c.rect(bx-k//2,80+k,8+k//1,1,WD[1+(k%3==0)]) if False else None
        c.poly([(bx,80),(bx+8,80),(bx,92)],WD[2]); c.line(bx,80,bx,92,WD[0]); c.line(bx+8,80,bx,92,WD[1])
    # --- terreo: pedra
    stone(c,0,FW,80,184,seed=5)
    # sombra do jetty sobre a pedra
    for x in range(FW):
        for y in range(80,90): c.set(x,y,sh(c.get(x,y),-0.5+0.045*(y-80)))
    # pilastras de quina
    for (qx) in (0,FW-14):
        for k,yy in enumerate(range(80,184,13)):
            c.rect(qx,yy,14,12,sh(ST[3],0.0 if k%2 else -0.08)); c.rect(qx,yy,14,1,ST[4]); c.rect(qx,yy+11,14,1,ST[0]); c.rect(qx,yy,1,12,ST[4]); c.rect(qx+13,yy,1,12,ST[0])
    # janelas de pedra em arco com grade e luz
    for wx in GW:
        x0=wx-4; w=30; top=110; bot=156
        c.rect(x0-3,top-3,w+6,bot-top+8,ST[0]); c.rect(x0-2,top-2,w+4,bot-top+6,ST[3])
        for yy in range(top,bot):
            for xx in range(x0,x0+w):
                t=(yy-top)/(bot-top); col=mix(GLASS[3],GLASS[1],t*0.85)
                c.set(xx,yy,col)
        c.ell(x0+w/2,top,w/2,8,GLASS[3]); c.ell(x0+w/2,top+2,w/2-1,8,mix(GLASS[3],GLASS[2],0.4))
        for gx in range(x0+4,x0+w,7): c.rect(gx,top-6,2,bot-top+6,IRON[1]); c.rect(gx,top-6,1,bot-top+6,IRON[2])
        c.rect(x0,top+16,w,2,IRON[1]); c.rect(x0,top+16,w,1,IRON[2])
        c.rect(x0-4,bot+2,w+8,4,ST[4]); c.rect(x0-4,bot+5,w+8,1,ST[0])
        c.rect(x0-5,bot+6,w+10,7,WD[2]); c.rect(x0-5,bot+6,w+10,1,WD[3]); c.rect(x0-5,bot+12,w+10,1,WD[0])
        for k in range(w+8):
            c.set(x0-4+k,bot+5,[C('3f8a3a'),C('5aa84a'),C('2e6a30')][k%3])
            if k%4==1: c.set(x0-4+k,bot+4,[RED[2],GOLD[2],C('f0f0ff'),C('c060d0')][(k//4)%4])
        for sx in (x0-9,x0+w+3):
            c.rect(sx,top-4,6,bot-top+8,TEAL[1])
            for yy in range(top-3,bot+3,3): c.rect(sx+1,yy,4,1,TEAL[2]); c.rect(sx+1,yy+1,4,1,TEAL[0])
            c.rect(sx,top-4,1,bot-top+8,TEAL[0]); c.rect(sx+5,top-4,1,bot-top+8,TEAL[0])
    # embasamento
    for x in range(FW):
        for y in range(176,192):
            c.set(x,y,sh(ST[2] if (x//16+y//8)%2 else ST[1],-0.12-0.03*(y-176)))
    for x in range(0,FW,16): c.rect(x,176,1,16,MT)
    c.rect(0,176,FW,2,ST[3]); c.rect(0,191,FW,1,ST[0])
    return c

def portal_surround(c):
    """moldura de pedra em arco ao redor do vao"""
    cx=CXD
    rx=(DX1-DX0)/2.0; ry=float(SPRING-ARCH_TOP)
    for y in range(ARCH_TOP-16,SPRING+1):
        for x in range(DX0-16,DX1+16):
            u=(x+.5-cx)/rx; v=(SPRING-y-.5)/ry
            if v<0: continue
            r=math.hypot(u,v)
            if r<1.0 or r>1.30: continue
            ang=math.degrees(math.atan2(v,u))
            k=int(ang//15)
            col=ST[3] if k%2 else ST[2]
            col=sh(col,0.16-(r-1.0)*0.7)
            if abs(ang-round(ang/15)*15)<1.2: col=MT
            if r<1.04: col=sh(col,-0.35)
            if r>1.27: col=sh(col,-0.25)
            c.set(x,y,col)
    # pilastras laterais
    for (px) in (DX0-14,DX1):
        for k,yy in enumerate(range(SPRING,184,12)):
            c.rect(px,yy,14,11,sh(ST[3],0.05 if k%2 else -0.05)); c.rect(px,yy,14,1,ST[4]); c.rect(px,yy+10,14,1,ST[0]); c.rect(px,yy,1,11,ST[4]); c.rect(px+13,yy,1,11,ST[0])
        c.rect(px-2,SPRING-4,18,5,ST[4]); c.rect(px-2,SPRING,18,1,ST[0])   # capitel
    # fecho (keystone) com caneca dourada
    c.rect(int(cx)-8,ARCH_TOP-20,16,17,ST[4]); c.rect(int(cx)-8,ARCH_TOP-20,16,1,WHITE) if False else None
    c.rect(int(cx)-8,ARCH_TOP-20,1,17,ST[3]); c.rect(int(cx)+7,ARCH_TOP-20,1,17,ST[0]); c.rect(int(cx)-8,ARCH_TOP-4,16,1,ST[0])
    c.rect(int(cx)-4,ARCH_TOP-16,7,9,GOLD[2]); c.rect(int(cx)-4,ARCH_TOP-16,7,2,GOLD[3]); c.rect(int(cx)+3,ARCH_TOP-14,3,5,GOLD[1]); c.rect(int(cx)-4,ARCH_TOP-8,7,1,GOLD[0])

def facade_full():
    c=facade_base(); portal_surround(c)
    # entalhe do vao (sera preenchido pela porta): escurece
    for y in range(ARCH_TOP-2,184):
        for x in range(DX0,DX1):
            if in_arch(x,y): c.set(x,y,(20,12,8,255))
    # degraus de pedra diante da porta
    for k,(xa,xb,ya) in enumerate(((DX0-18,DX1+18,176),(DX0-10,DX1+10,182))):
        c.rect(xa,ya,xb-xa,5 if k==0 else 6,ST[3]); c.rect(xa,ya,xb-xa,1,ST[4]); c.rect(xa,ya+(4 if k==0 else 5),xb-xa,1,ST[0])
    # SACADA do andar de cima sobre a porta + portas-janela envidracadas
    bx0,bx1=DX0-18,DX1+18
    c.rect(bx0+8,16,bx1-bx0-16,56,(30,18,12,255))
    for yy in range(18,70):
        for xx in range(bx0+10,bx1-10):
            t=(yy-18)/52; col=mix(GLASS[3],GLASS[1],t*0.85)
            if H(xx,yy,3)%11==0: col=sh(col,-0.07)
            c.set(xx,yy,col)
    # arco da porta de vidro, caixilhos em losango
    for xx in range(bx0+10,bx1-10,12):
        c.rect(xx,18,2,52,WD[1])
    for yy in range(26,70,12): c.rect(bx0+10,yy,bx1-bx0-20,2,WD[1])
    c.rect(bx0+10,14,bx1-bx0-20,5,WD[0]); c.rect(bx0+10,14,bx1-bx0-20,1,WD[3])
    # cortinas
    for (cx_) in (bx0+10,bx1-22):
        c.rect(cx_,18,12,52,RED[1]); 
        for yy in range(18,70,4): c.rect(cx_+2,yy,1,3,RED[2]); c.rect(cx_+8,yy,1,3,RED[0])
    c.rect(bx0+10,18,bx1-bx0-20,3,RED[0])
    # guarda-corpo de ferro forjado
    c.rect(bx0-6,66,bx1-bx0+12,6,WD[3]); c.rect(bx0-6,66,bx1-bx0+12,1,WD[4]); c.rect(bx0-6,71,bx1-bx0+12,1,WD[0])
    for xx in range(bx0-4,bx1+6,5):
        c.rect(xx,50,1,16,IRON[1]); c.set(xx,49,IRON[3])
    c.rect(bx0-6,48,bx1-bx0+12,2,IRON[2]); c.rect(bx0-6,48,bx1-bx0+12,1,IRON[3])
    for xx in range(bx0-4,bx1+6,10):
        c.ell(xx+2.5,57,2.5,2.5,IRON[2]); c.set(xx+2,56,IRON[3])
    # lampioes da sacada
    lantern(c,bx0-2,22); lantern(c,bx1+2,22)
    # PLACAS da taverna penduradas em bracos de ferro dos dois lados do portal
    def hang(bx,by,w,h,kind):
        c.rect(bx-6,by-8,w+12,3,IRON[1]); c.rect(bx-6,by-8,w+12,1,IRON[3])
        for k in range(10): c.rect(bx+w+4-k//2,by-8+k,1,1,IRON[1])        # suporte curvo
        for cxh in (bx+5,bx+w-5):
            for yy in range(by-5,by): c.set(cxh,yy,IRON[3] if yy%2 else IRON[1])
        c.rect(bx-1,by-1,w+2,h+2,WD[0]); c.rect(bx,by,w,h,WD[2])
        for yy in range(by+1,by+h-1):
            for xx in range(bx+1,bx+w-1):
                n=H(xx,yy//3,kind)%9
                c.set(xx,yy,WD[3] if (yy-by)%7 in (1,2) else (WD[1] if n==0 else WD[2]))
        c.rect(bx,by,w,1,WD[4]); c.rect(bx,by+h-1,w,1,WD[0]); c.rect(bx,by,1,h,WD[3]); c.rect(bx+w-1,by,1,h,WD[0])
        for (nx,ny) in ((bx+2,by+2),(bx+w-4,by+2),(bx+2,by+h-4),(bx+w-4,by+h-4)): nail(c,nx,ny)
        c.rect(bx+3,by+3,w-6,1,GOLD[1]); c.rect(bx+3,by+h-5,w-6,1,GOLD[1])
    # esquerda: nome da taverna em letras douradas
    from PIL import ImageDraw, ImageFont
    hang(158,108,66,50,1)
    d=ImageDraw.Draw(c.im); d.fontmode="1"
    try: f=ImageFont.load_default(size=9)
    except Exception: f=ImageFont.load_default()
    for (txt,ty) in (("CANECA",112),("DOURADA",126)):
        tw=d.textlength(txt,font=f); tx=158+(66-tw)/2
        d.text((tx+1,ty+1),txt,font=f,fill=(40,20,8,255)); d.text((tx,ty),txt,font=f,fill=GOLD[3])
    # caneca dourada pequena embaixo do nome
    c.rect(184,141,10,8,GOLD[2]); c.rect(184,141,10,2,GOLD[3]); c.rect(194,143,3,4,GOLD[1]); c.rect(185,148,8,1,GOLD[0])
    # direita: emblema entalhado (caneca grande + espigas)
    hang(384,108,66,50,2)
    c.rect(405,122,16,22,GOLD[2]); c.rect(405,122,16,4,GOLD[3]); c.rect(421,126,6,12,GOLD[1]); c.rect(423,129,2,6,WD[2]); c.rect(405,142,16,2,GOLD[0])
    c.rect(407,119,12,4,C('fff8e0')); c.set(410,118,C('ffffff')); c.set(415,118,C('ffffff'))
    for sxx,dirr in ((392,1),(444,-1)):
        for k in range(7): c.set(sxx+dirr*(k//2),150-k*3,GOLD[1]); c.set(sxx+dirr*(k//2)+dirr,148-k*3,GOLD[2])
    # ESTANDARTES vermelhos nos cantos do balcao
    for bx in (212,372):
        c.rect(bx-2,20,30,2,IRON[1]); c.rect(bx+2,22,22,40,RED[1]); c.rect(bx+2,22,22,3,RED[0])
        for k in range(11): c.rect(bx+2+k*2,62,2,6-abs(k-5),RED[1] if k%2 else RED[0])
        c.rect(bx+2,22,2,40,RED[2]); c.rect(bx+22,22,2,40,RED[0])
        c.rect(bx+8,34,10,12,GOLD[2]); c.rect(bx+8,34,10,3,GOLD[3]); c.rect(bx+18,37,4,6,GOLD[1]); c.rect(bx+9,48,8,2,GOLD[0])
    # bandeirolas penduradas na viga
    for k,xx in enumerate(range(24,FW-20,28)):
        if DX0-30<xx<DX1+30 or any(abs(xx+5-cw)<26 for cw in UPW) or 200<xx<400: continue
        col=[RED[2],TEAL[2],GOLD[2],C('c060d0')][k%4]
        c.poly([(xx,42),(xx+10,42),(xx+5,52)],col); c.line(xx,42,xx+10,42,IRON[0])
    # placa menor "QUARTOS" nao tem texto; ferradura da sorte sobre a viga
    return c

def door_frame(fr):
    """porta dupla 96x192 (frame 0 fechada .. 4 aberta). Fundo transparente fora do arco; dentro: folhas ou interior."""
    d=Cv(96,192)
    ox=DX0
    # interior visivel (usado quando aberta)
    for y in range(ARCH_TOP-2,184):
        for x in range(DX0,DX1):
            if not in_arch(x,y): continue
            ty=(y-ARCH_TOP)/(184-ARCH_TOP)
            col=mix(C('3a2314'),C('c97a2a'),ty**1.4*0.85)
            if y>150: col=mix(col,C('ffd27a'),(y-150)/34*0.5)   # chao iluminado
            d.set(x-ox,y,col)
    # lareira ao fundo (brilho)
    for dx in range(-14,15):
        for dy in range(-10,11):
            if math.hypot(dx/1.4,dy)<10:
                d.set(48+dx,150+dy,mix(d.get(48+dx,150+dy),C('ffb03a'),0.55*(1-math.hypot(dx/1.4,dy)/10)))
    # folhas
    k=fr/4.0
    lw=int(round(46*(1-0.86*k)))        # largura de cada folha
    for side in (0,1):
        for y in range(ARCH_TOP-2,184):
            for x in range(DX0,DX1):
                if not in_arch(x,y): continue
                if side==0: inside=(x-DX0)<lw
                else: inside=(DX1-1-x)<lw
                if not inside: continue
                lx=(x-DX0) if side==0 else (DX1-1-x)
                t=lx/max(1,lw)
                col=WD[2]
                n=H(lx//1,y//5,side+4)%9
                if n==0: col=WD[1]
                if lx%8==0: col=WD[1]
                if lx%8==1: col=WD[3]
                d.set(x-ox,y,col)
        # tiras de ferro e tachas
        for ys in (ARCH_TOP+14,SPRING+4,SPRING+30,SPRING+52):
            for x in range(DX0,DX1):
                if not in_arch(x,ys): continue
                lx=(x-DX0) if side==0 else (DX1-1-x)
                if lx<lw: d.set(x-ox,ys,IRON[1]); d.set(x-ox,ys+1,IRON[1]); d.set(x-ox,ys+2,IRON[0]); d.set(x-ox,ys,IRON[2])
                if lx<lw and lx%6==3: d.set(x-ox,ys+1,IRON[3])
        # borda interna escura
        for y in range(ARCH_TOP-2,184):
            for x in range(DX0,DX1):
                if not in_arch(x,y): continue
                lx=(x-DX0) if side==0 else (DX1-1-x)
                if lx==lw-1 and lw>2: d.set(x-ox,y,WD[0])
    # aldrava de leao e macaneta (so fechada/semiaberta)
    if fr<=2:
        kx=int(46*(1-0.86*k))
        for (x,y) in ((CXD-ox-kx+5,164),(CXD-ox+kx-6,164)):
            d.ell(x,y,3,3,GOLD[1]); d.set(int(x),int(y)-1,GOLD[3])
        mx=int(CXD-ox)
        d.ell(mx,150,6,6,GOLD[2]); d.ell(mx,150,4,4,GOLD[1]); d.ell(mx,150,2,2,GOLD[3]); d.rect(mx-1,156,3,5,GOLD[1])
    # dobradicas de ferro nas laterais do vao
    # soleira
    d.rect(DX0-ox,181,DX1-DX0,3,ST[3]); d.rect(DX0-ox,181,DX1-DX0,1,ST[4])
    return d

def cut_full():
    """quando voce esta dentro: murinho baixo (embasamento) + postes da porta"""
    c=Cv(FW,FH)
    for x in range(FW):
        for y in range(168,192):
            c.set(x,y,sh(ST[2] if (x//16+y//8)%2 else ST[1],-0.1-0.02*(y-168)))
    for x in range(0,FW,16): c.rect(x,168,1,24,MT)
    c.rect(0,168,FW,2,ST[3]); c.rect(0,191,FW,1,ST[0])
    # vao da porta fica livre e com soleira
    for x in range(DX0,DX1):
        for y in range(168,192): c.set(x,y,(0,0,0,0))
    c.rect(DX0,184,DX1-DX0,8,ST[3]); c.rect(DX0,184,DX1-DX0,1,ST[4]); c.rect(DX0,191,DX1-DX0,1,ST[0])
    for (px) in (DX0-14,DX1):
        for k,yy in enumerate(range(120,184,12)):
            c.rect(px,yy,14,11,sh(ST[3],0.05 if k%2 else -0.05)); c.rect(px,yy,14,1,ST[4]); c.rect(px,yy+10,14,1,ST[0])
    # luz quente do lampiao nos postes
    return c

def crop(cv,x,w,h=None):
    h=h or cv.h; o=Cv(w,h)
    for yy in range(h):
        for xx in range(w): o.p[xx,yy]=cv.get(x+xx,yy)
    return o

if __name__=='__main__':
    import os; os.makedirs('/home/claude/out',exist_ok=True)
    f=facade_full(); 
    # compoe: fachada + porta aberta/fechada
    for fr,name in ((0,'closed'),(4,'open')):
        im=Cv(FW,FH); im.blit(f,0,0); im.blit(door_frame(fr),DX0,0); im.save(f'/home/claude/out/fac_{name}.png',2)
    cut_full().save('/home/claude/out/cut.png',2)
