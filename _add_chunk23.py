import io
path = r'C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master\core\src\main\kotlin\com\alessandro\tedesco\i18n\Traduttore.kt'
with io.open(path, 'r', encoding='utf-8') as f:
    s = f.read()
with io.open(r'C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master\_pron_chunk_lines.txt','r',encoding='utf-8') as f:
    lines = f.read().rstrip()

old = '        r.putAll(esattoChunk22())'
if old not in s:
    print('putall not found'); raise SystemExit(1)
s = s.replace(old, old + '\n        r.putAll(esattoChunk23())')

marker = '    }\n}\n\n/** Short alias used at the call sites. */'
if marker not in s:
    print('marker not found'); raise SystemExit(1)
func = '\n    private fun esattoChunk23(): Map<String, String> = mapOf(\n' + lines + '\n    )\n'
s = s.replace(marker, func + '    }\n}\n\n/** Short alias used at the call sites. */')

with io.open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(s)
print('ok')
