import re, os

for root in ['app/src/main/java/com/alessandro/tedesco/ui', 'desktop/src/main/kotlin/com/alessandro/tedesco/desktop']:
    if not os.path.isdir(root):
        continue
    for dirpath, dirs, files in os.walk(root):
        for fn in files:
            if not fn.endswith('.kt'):
                continue
            p = os.path.join(dirpath, fn)
            try:
                with open(p, encoding='utf-8') as f:
                    lines = f.readlines()
            except Exception:
                continue
            for i, line in enumerate(lines, 1):
                if 'Text("' not in line:
                    continue
                if 't(' in line or 'stringResource' in line or 'AnnotatedString' in line:
                    continue
                # skip Text(variable)
                if re.search(r'Text\(\s*[a-zA-Z_][A-Za-z0-9_\.]*\s*\)', line):
                    continue
                print(f"{p}:{i}:{line.rstrip()}")
