import re
text = open('core/src/main/kotlin/com/alessandro/tedesco/data/Dialoghi.kt', encoding='utf-8').read()
for m in re.finditer(r'titolo = "([^"]+)"', text):
    print('TITOLO\t' + m.group(1))
for m in re.finditer(r'traduzione = "([^"]+)"', text):
    print('TRAD\t' + m.group(1))
for m in re.finditer(r'BattutaDialogo\("[ABC]", "((?:[^"\\]|\\.)*)", "((?:[^"\\]|\\.)*)"\)', text):
    print('BATT\t' + m.group(2))
for m in re.finditer(r'DomandaDialogo\("((?:[^"\\]|\\.)*)", listOf\((.*?)\), \d+\)', text, re.S):
    print('Q\t' + m.group(1))
    for o in re.findall(r'"((?:[^"\\]|\\.)*)"', m.group(2)):
        print('OPT\t' + o)
