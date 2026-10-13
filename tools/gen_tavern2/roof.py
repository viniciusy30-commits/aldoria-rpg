from pa import *
import facade as F

BW,HGT,MT=470,222,16
YR=111
TONES=[C('c24e30'),C('d05c38'),C('b4452a'),C('c8553a'),C('a83c26')]
def tile_col(r,idx,back):
    c=TONES[idx%5]
    return c

def roof():
    c=Cv(BW,HGT)
    rn=random.Random(2026)
    fascia=C('3a1a10')
    yTop=MT
    for y in range(yTop,HGT-1):
        back=y<YR
        rh=5 if back else 6
        ys=yTop if back else YR
        r=(y-ys)//rh; oy=(y-ys)%rh
        inset=max(0,8-(y-yTop)) if back else 0
        for x in range(1+inset,BW-1-inset):
            sw=10
            cx=x+(r%2)*5
            col_i=cx//sw
            idx=((r*374761393+col_i*668265263)>>13)&7
            base=TONES[idx%5]
            lx=cx%sw
            col=base
            # forma de telha: cantos de baixo arredondados
            corner=(oy>=rh-2 and (lx==0 or lx==sw-1))
            if back: col=sh(col,-0.22)
            else:
                gx=(x/BW-0.5)
                col=sh(col,0.06-0.16*(y-YR)/(HGT-YR)-0.04*gx)
            if oy==0: col=sh(col,-0.3)
            elif oy==1: col=sh(col,-0.1)
            elif oy==rh-2: col=sh(col,0.1)
            elif oy==rh-1: col=sh(col,0.24)
            if lx==0: col=sh(col,-0.28)
            elif lx==sw-1: col=sh(col,-0.12)
            if corner: col=sh(col,-0.35)
            if (lx*3+oy*5+r)%11==0 and 0<oy<rh-1: col=sh(col,-0.06)
            if x<=2+inset or x>=BW-3-inset: col=fascia
            c.set(x,y,col)
    # reparos e musgo
    mossc=[C('4a7a34'),C('86b35a'),C('6a9a44')]
    for k in range(90):
        mx=8+rn.randrange(BW-16); my=YR+6+rn.randrange(HGT-YR-18)
        if rn.random()<0.5 and k%3: continue
        for j in range(rn.randrange(2,5)):
            c.set(mx+j,my,rn.choice(mossc)); 
            if rn.random()<0.5: c.set(mx+j,my+1,rn.choice(mossc[:2]))
    for k in range(7):  # telhas de reparo mais escuras
        mx=10+rn.randrange(BW-30); my=YR+8+rn.randrange(80)
        c.rect(mx,my,10,5,C('8a3a28')); c.rect(mx,my,10,1,C('5a2418')); c.rect(mx,my+4,10,1,C('a85038'))
    # cumeeira grossa
    for x in range(1,BW-1):
        c.set(x,YR-3,C('f08a58')); c.set(x,YR-2,C('c8603a')); c.set(x,YR-1,C('b0482c'))
        c.set(x,YR,C('9a3a24')); c.set(x,YR+1,C('6e2818')); c.set(x,YR+2,C('4a1a10'))
        if x%9==0:
            for yy in (YR-2,YR-1,YR): c.set(x,yy,C('6e2818'))
    # fiada do beiral + calha
    for x in range(1,BW-1):
        c.set(x,HGT-4,sh(C('b0482c'),-0.35)); c.set(x,HGT-3,C('4a2418'))
        c.set(x,HGT-2,C('2a160c'))
    # lucarnas
    for fx in (0.17,0.5,0.83):
        dcx=int(BW*fx); dy0=YR+26
        # corpo
        c.rect(dcx-11,dy0+9,22,19,F.BM)
        c.rect(dcx-10,dy0+10,20,17,F.PL)
        for yy in range(dy0+10,dy0+27):
            for xx in range(dcx-10,dcx+10):
                if (xx*7+yy*3)%11==0: c.set(xx,yy,F.PLS)
        c.rect(dcx-6,dy0+12,12,11,F.BMD); c.rect(dcx-5,dy0+13,10,9,C('9ed2ec'))
        c.rect(dcx-5,dy0+18,10,4,C('f4d890'))
        c.rect(dcx-1,dy0+13,2,9,F.PL); c.rect(dcx-5,dy0+17,10,1,F.PL)
        c.rect(dcx-7,dy0+23,14,2,C('b8a078'))
        # empena
        for yy in range(dy0-5,dy0+10):
            half=14*(yy-(dy0-5))/15
            for xx in range(dcx-int(half),dcx+int(half)+1):
                col=C('a8432a') if (yy+xx//2)%4 else C('7a2c1c')
                if yy==dy0-5 or abs(xx-dcx)>=int(half)-0: col=C('4a1a10')
                c.set(xx,yy,col)
        c.rect(dcx-14,dy0+10,29,2,F.BMD)
        c.rect(dcx-11,dy0+28,22,2,(0,0,0,85))
        # bolinha enfeite
        c.set(dcx,dy0-7,F.GOLD); c.set(dcx,dy0-6,F.BMD)
    # chaminés
    for chx in (103,376):
        st=C('8e8680'); ct=YR-34; cb=YR+6; cw=15
        for y in range(ct,cb):
            for x in range(chx,chx+cw):
                col=st
                if (y-ct)%4==0: col=sh(st,-0.3)
                off=(((y-ct)//4)%2)*4
                if x in (chx+4+off,chx+10+off-(4 if off else 0)): col=sh(st,-0.3)
                if x==chx: col=sh(st,0.25)
                if x>=chx+cw-2: col=sh(st,-0.34)
                if (x*5+y*3)%13==0: col=sh(col,0.12)
                c.set(x,y,col)
        c.rect(chx-2,ct-3,cw+4,3,sh(st,-0.2)); c.rect(chx-2,ct-3,cw+4,1,sh(st,0.15))
        c.rect(chx+2,ct-2,cw-4,2,C('1a1210'))
        for x in range(chx-3,chx+cw+3): c.set(x,cb,C('4a1a10'))   # roda-telha
    # biruta de caneca + haste
    cx=BW//2
    c.rect(cx,YR-26,1,24,F.IRON); c.rect(cx-2,YR-27,5,1,F.GOLDH); c.set(cx,YR-29,F.GOLDH)
    c.rect(cx-6,YR-26,12,1,F.IRON) if False else None
    c.rect(cx+1,YR-24,8,7,F.GOLD); c.rect(cx+1,YR-24,8,1,F.GOLDH); c.rect(cx+1,YR-24,1,7,F.GOLDH)
    c.rect(cx+9,YR-23,2,4,F.GOLD); c.set(cx+11,YR-22,F.GOLD); c.set(cx+11,YR-21,F.GOLD)
    c.rect(cx+1,YR-18,8,1,sh(F.GOLD,-0.4))
    c.rect(cx+1,YR-26,8,2,C('fff6e0')); c.set(cx+3,YR-27,(255,255,255,255)); c.set(cx+6,YR-27,(255,255,255,255))
    # bandeirolas no beiral
    cols=[C('b8372f'),C('f2e6cc'),C('2f8c86'),C('d8a640')]
    x=14;k=0
    while x<BW-14:
        yb=HGT-9
        c.set(x,yb,C('3a2410')); c.rect(x+1,yb,3,1,cols[k%4]); c.rect(x+1,yb+1,3,1,cols[k%4]); c.set(x+2,yb+2,cols[k%4])
        x+=6;k+=1
    c.outline(C('2a1a0e'))
    return c

if __name__=='__main__':
    # preview completo: telhado + fachada 14 colunas
    W=14*32+24
    full=Cv(W,222+96+60)
    r=roof(); full.blit(r,0,0)
    fx=11
    kinds=[3,1,0,1,2,None,None,2,1,0,1,0,1,3]
    import door
    for i,k in enumerate(kinds):
        if k is None: continue
        e=1 if i==0 else (2 if i==13 else 0)
        full.blit(F.facade(k,e,i%2),fx+i*32,222)
    full.blit(door.door(0),fx+5*32,222)
    full.save('/home/claude/gen/out/prev_full.png',2)
    r.save('/home/claude/gen/out/roof.png')
