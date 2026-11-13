import re, json

p = r'core/src/main/kotlin/com/alessandro/tedesco/data/Dialoghi.kt'
with open(p, encoding='utf-8') as f:
    s = f.read()

# find all string literals in relevant fields
patterns = [
    r'titolo = "([^"]*)"',
    r'traduzione = "([^"]*)"', # general and battuta
    r'domanda = "([^"]*)"',
    r'opzioni = listOf\(([^)]+)\)',
]
seen = set()
out = []
for pat in patterns:
    for m in re.finditer(pat, s):
        if 'opzioni' in pat:
            for om in re.finditer(r'"([^"]*)"', m.group(1)):
                v = om.group(1)
                if v not in seen:
                    seen.add(v); out.append(v)
        else:
            v = m.group(1)
            if v not in seen:
                seen.add(v); out.append(v)

for x in out:
    print(x)
