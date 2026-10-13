# helpers de pixel art
from PIL import Image, ImageDraw
import random, math

def C(hexs):
    hexs = hexs.lstrip('#')
    return (int(hexs[0:2],16), int(hexs[2:4],16), int(hexs[4:6],16), 255)

SHADOW=(34,22,58); LIGHT=(255,238,190)
def mix(a,b,t):
    return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))+(a[3] if len(a)>3 else 255,)
def sh(c,amt):
    if amt<0: return mix(c,SHADOW,min(1,-amt*0.9))
    return mix(c,LIGHT,min(1,amt*0.8))

class Cv:
    def __init__(s,w,h):
        s.w=w; s.h=h; s.im=Image.new('RGBA',(w,h),(0,0,0,0)); s.p=s.im.load()
    def get(s,x,y):
        return s.p[x,y] if 0<=x<s.w and 0<=y<s.h else (0,0,0,0)
    def set(s,x,y,c):
        if 0<=x<s.w and 0<=y<s.h: s.p[x,y]=c
    def rect(s,x,y,w,h,c):
        for yy in range(y,y+h):
            for xx in range(x,x+w): s.set(xx,yy,c)
    def line(s,x0,y0,x1,y1,c):
        dx=abs(x1-x0); dy=-abs(y1-y0); sx=1 if x0<x1 else -1; sy=1 if y0<y1 else -1; e=dx+dy
        while True:
            s.set(x0,y0,c)
            if x0==x1 and y0==y1: break
            e2=2*e
            if e2>=dy: e+=dy; x0+=sx
            if e2<=dx: e+=dx; y0+=sy
    def ell(s,cx,cy,rx,ry,c):
        for y in range(int(cy-ry)-1,int(cy+ry)+2):
            for x in range(int(cx-rx)-1,int(cx+rx)+2):
                if ((x+.5-cx)/rx)**2+((y+.5-cy)/ry)**2<=1: s.set(x,y,c)
    def poly(s,pts,c):
        ImageDraw.Draw(s.im).polygon(pts,fill=c)
    def blit(s,o,ox,oy):
        for y in range(o.h):
            for x in range(o.w):
                c=o.p[x,y]
                if c[3]>0: s.set(x+ox,y+oy,c)
    def solid(s,x,y): return s.get(x,y)[3]>128
    def outline(s,c):
        add=[]
        for y in range(s.h):
            for x in range(s.w):
                if s.solid(x,y): continue
                if s.solid(x-1,y) or s.solid(x+1,y) or s.solid(x,y-1) or s.solid(x,y+1): add.append((x,y))
        for x,y in add: s.set(x,y,c)
    def save(s,path,scale=1):
        im=s.im if scale==1 else s.im.resize((s.w*scale,s.h*scale),Image.NEAREST)
        im.save(path)
