import pathlib
p=pathlib.Path(r'app/src/main/java/com/alessandro/tedesco/ui/UpdateBanner.kt')
s=p.read_text(encoding='utf-8')
# replace the specific block by searching for key parts
old = '                            is UpdateState.Available -> "Aggiornamento disponibile: v${(st as UpdateState.Available).version.versionName}"\n                            is UpdateState.Downloading -> "Download ${(st as UpdateState.Downloading).progress}%"\n'
if old not in s:
    print('block not found')
    exit(1)
new = '                            is UpdateState.Available -> t("Aggiornamento disponibile") + ": v${(st as UpdateState.Available).version.versionName}"\n                            is UpdateState.Downloading -> t("Download") + " ${(st as UpdateState.Downloading).progress}%"\n'
s=s.replace(old,new)
# also fix checking line to stable ellipsis
s=s.replace('t("Controllo aggiornamenti\u2026")', 't("Controllo aggiornamenti...")')
s=s.replace('t("Controllo aggiornamenti\ufffd\ufffd\ufffd")', 't("Controllo aggiornamenti...")')
p.write_text(s,encoding='utf-8',newline='\n')
print('ok')
