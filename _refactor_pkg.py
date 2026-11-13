import os, re, shutil, pathlib

root = pathlib.Path('.')
skip_dirs = {'.git'}

def should_skip(p: pathlib.Path):
    parts = set(p.parts)
    if '.git' in parts:
        return True
    if 'desktop-rust' in parts and 'target' in parts:
        return True
    return False

exts = {'.kt','.java','.kts','.xml','.json','.md','.pro','.properties','.yml','.yaml','.txt','.gradle','.html','.css','.js','.rs','.toml','.lock'}
replacements = [
    ('com.alessandro.tedesco', 'com.alessandro.openlingo'),
    ('com/alessandro/tedesco', 'com/alessandro/openlingo'),
    ('TedescoViewModel', 'OpenLingoViewModel'),
    ('TedescoApp', 'OpenLingoApp'),
]
changed_files = 0
for p in root.rglob('*'):
    if not p.is_file():
        continue
    if should_skip(p):
        continue
    if p.suffix.lower() not in exts and p.name not in {'gradlew', 'gradlew.bat'}:
        continue
    try:
        data = p.read_text(encoding='utf-8')
    except Exception:
        continue
    orig = data
    for a, b in replacements:
        data = data.replace(a, b)
    if p.name == 'settings.gradle.kts':
        data = data.replace('rootProject.name = "Deutsch"', 'rootProject.name = "OpenLingo"')
    if data != orig:
        p.write_text(data, encoding='utf-8', newline='\n')
        changed_files += 1
print('changed_files', changed_files)

src_roots = [
    pathlib.Path('app/src/main/java'),
    pathlib.Path('app/src/test/java'),
    pathlib.Path('core/src/main/kotlin'),
    pathlib.Path('core/src/test/kotlin'),
    pathlib.Path('desktop/src/main/kotlin'),
    pathlib.Path('desktop/src/test/kotlin'),
]
for sr in src_roots:
    old = sr / 'com/alessandro/tedesco'
    new = sr / 'com/alessandro/openlingo'
    if old.exists():
        new.parent.mkdir(parents=True, exist_ok=True)
        if new.exists():
            for item in list(old.iterdir()):
                shutil.move(str(item), str(new / item.name))
            try:
                old.rmdir()
            except Exception:
                pass
        else:
            shutil.move(str(old), str(new))
        for parent in [sr / 'com/alessandro', sr / 'com']:
            try:
                if parent.exists() and not any(parent.iterdir()):
                    parent.rmdir()
            except Exception:
                pass
        print('moved', old, '->', new)
    else:
        print('skip', old)

for p in list(root.rglob('TedescoApp.kt')):
    if should_skip(p):
        continue
    newp = p.with_name('OpenLingoApp.kt')
    if not newp.exists():
        p.rename(newp)
        print('renamed file', p, '->', newp)

for p in list(root.rglob('TedescoViewModel.kt')):
    if should_skip(p):
        continue
    newp = p.with_name('OpenLingoViewModel.kt')
    if not newp.exists():
        p.rename(newp)
        print('renamed file', p, '->', newp)

print('done')
