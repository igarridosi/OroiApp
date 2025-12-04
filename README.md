    
# Oroi

### Zure harpidetzen kontrola, zure eskuan.

`Oroi`, euskerazko "Oroitu" hitzatikan dator. Aplikazio honen filosofia finantza-kontzientzia sustatzea da, tresna minimalista, dotore eta funtzional baten bidez. Erabiltzaileak bere harpidetzak eskuz sartzen ditu, gastuen jarraipen aktibo eta kontzientea bultzatuz.

## ✨ Ezaugarriak

### ✅ Inplementatutako Ezaugarriak (MVP)

*   **Harpidetzen Kudeaketa Osoa:** Sortu, irakurri, eguneratu eta ezabatu (CRUD) zure hileko edo urteko harpidetzak modu sinplean.
*   **Kostuaren Bistaratze Anitza:** Pantaila nagusian, karrusel interaktibo batek zure gastu totala erakusten du hileko, urteko eta eguneko ikuspegietan, zure ohituren inpaktu ekonomikoa hobeto ulertzeko.
*   **Abisu Adimendunak:** `WorkManager` erabiliz, aplikazioak atzeko planoan lan egiten du jakinarazpen bat bidaltzeko harpidetza bat ordaindu baino 2 egun lehenago. Jakinarazpenak adierazten du zenbat diru aurreztuko zenukeen urtean harpidetza hori ezeztatuz gero.
*   **Nabigazio Intuitiboa:** Zerrendako elementu bat irristatuz, zuzenean editatzeko pantailara joan zaitezke, datuak aldatzeko edo harpidetza ezabatzeko.
*   **Identifikazio Bisuala:** Txartel bakoitzak etiketa txiki bat du, fakturazio-zikloa kolore bidez adierazten duena (astero, hilero, urtero), informazioa kolpe batean ikusteko.

### 🚀 Etorkizuneko Ideiak

*   **Kategoriak:** Harpidetzak sailkatzeko aukera (Aisialdia, Lana, Kirola...), gastuen analisia hobetzeko.
*   **"Botoi Gorria":** Zerbitzu ezagunenetan harpidetza ezeztatzeko orrietara esteka zuzenak dituen datu-base bat.
*   **Doako Proben Kudeatzailea:** Harpidetzak "proba garaian" daudela markatzeko aukera, amaitu baino lehen abisu intentsiboagoak jasotzeko.

## 🛠️ Teknologiak eta Arkitektura

Proiektu hau Android garapeneko praktika onenak eta tresna modernoenak jarraituz eraiki da.

*   **Lengoaia:** %100 **Kotlin**.
*   **Interfazea:** %100 **Jetpack Compose**, UI deklaratibo eta erreaktibo bat sortzeko.
*   **Arkitektura:** **MVVM** (Model-View-ViewModel), ardurak argi bereizteko (`UI` -> `ViewModel` -> `Model`).
*   **Datu-basea:** **Room**, datuen persistentzia lokala modu sendoan kudeatzeko.
*   **Asinkronia:** **Kotlin Coroutines** eta **Flow**, datu-fluxuak eta atzeko planoko eragiketak modu eraginkorrean kudeatzeko.
*   **Nabigazioa:** **Jetpack Navigation for Compose**, aplikazioaren pantailen artean mugitzeko ("Single-Activity Architecture" eredua jarraituz).
*   **Atzeko Planoko Lanak:** **WorkManager**, abisuak eta jakinarazpenak modu fidagarrian programatzeko, bateriaren kontsumoa optimizatuz.

### Proiektuaren Egitura
```sh
.
├── data/ # Datu-basearekin (Room) erlazionatutako guztia: DAO, entitateak, bihurgailuak.
├── model/ # Datu-klaseak (Entitateak).
├── ui/ # Erabiltzailearen interfazea: pantailak (@Composable) eta gaiak.
├── viewmodel/ # ViewModel klaseak, UI-aren logika eta egoera kudeatzen dutenak.
└── worker/ # WorkManager-ekin erlazionatutako atzeko planoko lanak.
```

    
## ⚙️ Nola Exekutatu

1.  Repositorio hau klonatu:
    ```bash
    git clone https://github.com/igarridosi/AndroidProjects.git
    ```
2.  Ireki `OroiApp` karpeta Android Studio-rekin.
3.  Proiektua sinkronizatu eta exekutatu emuladore edo gailu fisiko batean.

## ✒️ Egilea

**Ibai Garrido** - [GitHub Profila](https://github.com/igarridosi)

  
