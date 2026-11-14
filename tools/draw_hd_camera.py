#!/usr/bin/env python3
"""HD Digital Camera item texture: python3 tools/draw_hd_camera.py hd.txt && magick txt:hd.txt hd_camera.png"""
import sys
# HD Digital Camera: the thermal camera's silhouette and angle, with a dark body, a gold side stripe,
# a red power LED and blue digital glass with magenta and cyan multi-coating flecks.
P={'.':None,'A':(29,31,37),'B':(20,21,27),'D':(55,57,66),'F':(40,42,49),
   'H':(63,116,201),'K':(30,58,120),'I':(18,36,79),'J':(13,21,48),'W':(220,235,255),
   'C':(122,87,24),'E':(184,137,46),'G':(230,190,90),'R':(229,65,58),
   'P':(190,80,200),'Q':(90,220,210)}
G='''
................
................
................
....AAAAAABBCBB.
....BDRDDDBAEAB.
...BBAABFDBFGAB.
..BAWBFFBFBFGFB.
.BAHPJADDABFGFB.
.BFKJIAFFBBFGFB.
.AFQJKBAABBFGFB.
..ADIBAFBFBFEFB.
...BBBBBFFBAGAB.
....BDFFFDBAEAB.
....BBBBBBBBCBB.
................
................
'''.strip('\n').split('\n')
assert len(G)==16 and all(len(r)==16 for r in G),[len(r) for r in G]
out=["# ImageMagick pixel enumeration: 16,16,255,srgba"]
for y,row in enumerate(G):
    for x,ch in enumerate(row):
        c=P[ch]
        out.append(f"{x},{y}: "+("(0,0,0,0)" if c is None else f"({c[0]},{c[1]},{c[2]},255)"))
open(sys.argv[1],'w').write("\n".join(out)+"\n")
