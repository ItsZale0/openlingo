import os, re

def scan(root):
    hits=[]
    pat = re.compile(r'Text\(\s*"((?:[^"\\]|\\.){2,})"')
    for dirpath,_,files in os.walk(root):
        for f in files:
            if not f.endswith('.kt'): continue
            path=os.path.join(dirpath,f)
            try:
                lines=open(path,encoding='utf-8').read().splitlines()
            except: continue
            for i,l in enumerate(lines,1):
                for m in pat.finditer(l):
                    s=m.group(1)
                    q=l.find('"'+s+'"', m.start())
                    if q<0: continue
                    before=l[max(0,q-3):q]
                    if 't(' in before: continue
                    # skip if looks like only numbers/symbols/percent
                    if re.fullmatch(r'[\d\s%.,:\-+/()·]+', s): continue
                    hits.append((path,i,s,l.strip()[:140]))
    return hits

for label,root in [('APP','app/src/main/java/com/alessandro/tedesco/ui'),('DESK','desktop/src/main/kotlin/com/alessandro/tedesco/desktop')]:
    h=scan(root)
    print(f'==={label} {len(h)}===')
    for p,i,s,l in h[:50]:
        print(f'{p}:{i}: {s}')
