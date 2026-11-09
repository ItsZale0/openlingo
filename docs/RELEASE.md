# Processo di rilascio

Questa pagina descrive come viene preparata e pubblicata una versione di
Deutsch. Il processo è volutamente semplice e verificabile: gli asset sono
costruiti localmente, il manifest dichiara lo SHA-256 dell'APK e la CI controlla
che il tag sia coerente.

## Versionamento

La fonte di verità è `version.properties`:

```properties
VERSION_CODE=308
VERSION_NAME=1.48.75
```

`VERSION_CODE` è l'intero usato da Android per confrontare le versioni;
`VERSION_NAME` è la versione leggibile. I tag seguono il formato `v<versione>`
(es. `v1.48.75`).

Il task Gradle `bumpVersion` incrementa entrambi e riscrive
`updates/version.json`:

```bash
./gradlew bumpVersion -Pchangelog="Cosa è cambiato"
```

Il manifest contiene anche `apkUrl` e `apkSha256`. `bumpVersion` non conosce
l'hash dell'APK: va inserito dopo la build, prima del commit.

## 1. Preparare la versione

1. Aggiorna `VERSION_CODE` e `VERSION_NAME` (o usa `bumpVersion`).
2. Aggiorna `updates/version.json`: versione, changelog e `apkUrl` del tag che
   si sta per creare.
3. Aggiorna la documentazione se cambiano comportamento o voci dell'interfaccia.

## 2. Costruire gli asset

```bash
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
./gradlew :app:testDebugUnitTest :core:test
```

Per il pacchetto desktop:

```bash
./gradlew :desktop:createDistributable
```

Gli artefatti prodotti sono:

- `app/build/outputs/apk/debug/app-debug.apk`;
- `desktop/build/compose/binaries/main/app/Deutsch/`.

Rinomina gli asset con lo schema usato dalle release:

```
Deutsch-v<versione>.apk
Deutsch-desktop-v<versione>-windows.zip
Deutsch.exe
```

## 3. Calcolare lo SHA-256

```powershell
Get-FileHash "Deutsch-v<versione>.apk" -Algorithm SHA256
```

Inserisci l'hash (minuscolo) in `updates/version.json`:

```json
"apkSha256": "<hash>"
```

L'app rifiuta un APK il cui hash non coincide con quello dichiarato. Se l'APK
viene ricostruito dopo questo passaggio, l'hash va ricalcolato.

## 4. Pubblicare il codice

```bash
git add -A
git commit -m "v<versione>: <sintesi>"
git push origin master
git tag v<versione>
git push origin v<versione>
```

Il push del tag avvia il workflow **Verify release**, che controlla:

- coerenza tra `version.properties`, `updates/version.json` e nome del tag;
- compilazione dell'APK.

Il workflow non pubblica nulla: la release viene creata dal maintainer, così
l'APK distribuito mantiene la stessa firma e lo stesso SHA-256 del manifest.

## 5. Creare la release

Crea una release GitHub associata al tag e carica i tre asset:

- `Deutsch-v<versione>.apk`;
- `Deutsch-desktop-v<versione>-windows.zip`;
- `Deutsch.exe`.

La descrizione deve riportare le novità principali e i requisiti di
installazione. Al termine verifica che:

1. l'APK scaricato abbia lo stesso SHA-256 del manifest;
2. la firma sia la stessa della versione precedente;
3. il manifest su `master` punti alla release corretta.

## Firme e aggiornamenti

Tutte le release devono essere firmate con la **stessa chiave**. Android
rifiuta un aggiornamento firmato con una chiave diversa: cambiare chiave
richiede una nuova installazione e la perdita dei dati locali, a meno di
esportazione e ripristino.

Il keystore di debug non è versionato. Per build riproducibili o per una chiave
di release si usano le variabili d'ambiente:

```
TEDESCO_KEYSTORE_FILE
TEDESCO_KEYSTORE_PASSWORD
TEDESCO_KEY_ALIAS
TEDESCO_KEY_PASSWORD
```

Senza queste variabili Gradle usa il keystore di debug locale.

## Controlli prima della pubblicazione

- [ ] `version.properties` e `updates/version.json` allineati.
- [ ] Test verdi (`:app:testDebugUnitTest`, `:core:test`).
- [ ] APK installabile e aggiornabile dalla versione precedente.
- [ ] `apkSha256` corrispondente all'APK caricato.
- [ ] Asset desktop avviabili.
- [ ] Changelog e note di release aggiornati.
