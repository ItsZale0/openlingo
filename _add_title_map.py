import io
path=r'C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master\core\src\main\kotlin\com\alessandro\tedesco\i18n\Traduttore.kt'
with io.open(path,'r',encoding='utf-8') as f: s=f.read()
needle='        "s iniziale" to "Initial s",'
if needle not in s:
    print('not found'); raise SystemExit(1)
ins = needle + '\n        "OpenLingo — imparare le lingue con didattica reale" to "OpenLingo — language learning with real didactics",'
s=s.replace(needle,ins)
with io.open(path,'w',encoding='utf-8',newline='\n') as f: f.write(s)
print('added')
