import io
path=r'C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master\core\src\main\kotlin\com\alessandro\tedesco\i18n\Traduttore.kt'
with io.open(path,'r',encoding='utf-8') as f: s=f.read()
needle='        "OpenLingo — imparare le lingue con didattica reale" to "OpenLingo — language learning with real didactics",'
if needle not in s:
    print('not found'); raise SystemExit(1)
ins = needle + '''
        "Importa da file" to "Import from file",
        "Lettura file in corso…" to "Reading file…",
        "Importazione non riuscita" to "Import failed",
        "Impossibile leggere il file" to "Unable to read the file",
        "File di esportazione non leggibile" to "Export file is not readable",'''
s=s.replace(needle,ins)
with io.open(path,'w',encoding='utf-8',newline='\n') as f: f.write(s)
print('added')
