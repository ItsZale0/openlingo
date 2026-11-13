import re
text = open('core/src/main/kotlin/com/alessandro/tedesco/data/Sudtirol.kt', encoding='utf-8').read()
for m in re.finditer(r'Variante\("([^"]+)", "([^"]+)", "([^"]+)"\)', text):
    print('VAR\t' + m.group(3))
for m in re.finditer(r'VoceLessico\("([^"]+)", "([^"]+)", "([^"]+)"\)', text):
    print('VOC_IT\t' + m.group(2))
    print('VOC_CAT\t' + m.group(3))
for m in re.finditer(r'Frase\("([^"]+)", "([^"]+)"(?:, "([^"]*)")?\)', text):
    print('FRASE_IT\t' + m.group(2))
    if m.group(3): print('FRASE_NOTA\t' + m.group(3))
for m in re.finditer(r'titolo = "([^"]+)"', text):
    print('TITOLO\t' + m.group(1))
for m in re.finditer(r'contesto = "([^"]+)"', text):
    print('CONTESTO\t' + m.group(1))
for m in re.finditer(r'DomandaDialogo\("((?:[^"\\]|\\.)*)", listOf\((.*?)\), \d+\)', text, re.S):
    print('Q\t' + m.group(1))
    for o in re.findall(r'"((?:[^"\\]|\\.)*)"', m.group(2)):
        print('OPT\t' + o)
