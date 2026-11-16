import re,sys
x=open(sys.argv[1],encoding='utf-8',errors='ignore').read()
for m in re.finditer(r'<node [^>]*>',x):
    n=m.group(0)
    g=lambda k:(re.search(k+r'="([^"]*)"',n) or [None,''])[1]
    t=g('text') or g('content-desc'); b=g('bounds')
    if not t: continue
    bb=list(map(int,re.findall(r'\d+',b)))
    c=' [c]' if g('clickable')=='true' else ''
    print(f"({(bb[0]+bb[2])//2},{(bb[1]+bb[3])//2}){c} {t}")
