entries = []
coppie = [
 ('u corta vs ü','short u vs ü'),('già','already'),('bello','beautiful'),
 ('u vs ü','u vs ü'),('madre','mother'),('madri','mothers'),
 ('torta','cake'),('cucine','kitchens'),
 ('ö lunga vs ö corta','long ö vs short ö'),('caverna','cave'),('inferno','hell'),
 ('o lunga vs o corta + ff','long o vs short o + ff'),('forno','oven'),('aperto','open'),
 ('i corta vs ie lunga','short i vs long ie'),('chiedere','to ask'),('offrire','to offer'),
 ('ü lunga vs ü corta','long ü vs short ü'),('sentire','to feel'),('riempire','to fill'),
 ('e lunga vs e corta','long e vs short e'),('aiuola','flowerbed'),('letto','bed'),
 ('a corta vs a lunga','short a vs long a'),('città','city'),('stato','state'),
 ('ch dolce vs sch','soft ch vs sch'),('chiesa','church'),('ciliegia','cherry'),
 ('ss vs ß + ei','ss vs ß + ei'),('sapere','to know'),('bianco (verbo)','to whiten'),
 ('s vs ß','s vs ß'),('viaggiare','to travel'),('strappare','to tear'),
 ('a lunga vs a corta','long a vs short a'),('barca','boat'),('posso','I can'),
 ('fratello','brother'),('fratelli','brothers'),
 ("v si legge 'f' vs w","v is pronounced 'f' vs w"),('cugino','cousin'),('tempo','weather/time'),
 ('ei vs i','ei vs i'),('indicare/biancare','to point/whiten'),
 ('au vs a','au vs a'),('cane','dog'),('mano','hand'),
 ('e lunga vs ä lunga','long e vs long ä'),('bacche','berries'),('orsi','bears'),
]
consigli = [
 ('Coppie minime','Minimal pairs'),
 ("Dì 'i' con le labbra arrotondate come per 'u'. Non è 'u'.", "Say 'i' with your lips rounded as for 'u'. It is not 'u'."),
 ("Dì 'e' con le labbra arrotondate come per 'o'.", "Say 'e' with your lips rounded as for 'o'."),
 ("Dopo a/o/u è un suono gutturale (come la 'c' toscana di 'casa'); dopo i/e è dolce.", "After a/o/u it is a guttural sound (like 'ch' in Scottish 'loch'); after i/e it is soft."),
 ("In finale è quasi muto, vocalizzato: 'der' suona 'dea'. Non arrotare la r italiana.", "At the end it is almost silent, vocalized: 'der' sounds like 'dea'. Do not roll the r as in Italian."),
 ("È 'ts', non 'z' italiana: 'zehn' = 'tsehn'.", "It is 'ts', not an Italian 'z': 'zehn' = 'tsehn'."),
 ("Davanti a vocale è sonora ('s' di 'rosa'): 'sie' = 'zii'.", "Before a vowel it is voiced (like 'z' in 'zoo'): 'sie' = 'zii'."),
 ("'ei' = 'ai'; 'ie' = 'ii' lunga. Sono opposti: non confonderli.", "'ei' = 'ai'; 'ie' = long 'ee'. They are opposites: don't confuse them."),
 ('A fine sillaba b/d/g diventano p/t/k.', 'At the end of a syllable b/d/g become p/t/k.'),
 ("Nelle parole tedesche l'accento è spesso sulla prima sillaba.", 'In German words the stress is often on the first syllable.'),
 ('Auslautverhärtung', 'Final devoicing (Auslautverhärtung)'),
 ('accento', 'Word stress'),
 ('s iniziale', 'Initial s'),
]
seen=set()
out=[]
for it,en in coppie+consigli:
    if it in seen: continue
    seen.add(it)
    it2=it.replace('\\','\\\\').replace('"','\\"')
    en2=en.replace('\\','\\\\').replace('"','\\"')
    out.append(f'        "{it2}" to "{en2}",')
with open(r'C:\Users\zampe\AppData\Local\Temp\opencode\tedesco-master\_pron_chunk_lines.txt','w',encoding='utf-8') as f:
    f.write('\n'.join(out))
print(len(out))
