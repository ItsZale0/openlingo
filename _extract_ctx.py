import re
p = r'core/src/main/kotlin/com/alessandro/tedesco/data/DidatticaImmersione.kt'
with open(p, encoding='utf-8') as f:
    s = f.read()
seen=set()
for m in re.finditer(r'contesto\s*=\s*"([^"]*)"', s):
    v=m.group(1)
    if v not in seen:
        seen.add(v)
        print(v)
