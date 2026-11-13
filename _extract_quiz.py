import re
text = open('core/src/main/kotlin/com/alessandro/tedesco/data/QuizData.kt', encoding='utf-8').read()
for m in re.finditer(r'domanda = "((?:[^"\\]|\\.)*)"', text): print('D\t'+m.group(1))
for m in re.finditer(r'opzioni = listOf\((.*?)\),', text, re.S):
    for o in re.findall(r'"((?:[^"\\]|\\.)*)"', m.group(1)): print('O\t'+o)
for m in re.finditer(r'spiegazione = "((?:[^"\\]|\\.)*)"', text): print('S\t'+m.group(1))
