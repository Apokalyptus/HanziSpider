# Projektanalyse

```markdown
# HanziSpider

Dieses Projekt ist ein Crawler, der Webseiten von URLs abruft, extrahiert und in einer Datenbank speichert.

## Was bringt das Projekt dem Nutzer?

Das Projekt ermöglicht es, automatisch Webseiten zu scrapen und die darin enthaltenen Daten (Text, Hyperlinks) zu speichern und zu verwalten.  Es bietet eine Möglichkeit, Daten von Webseiten zu sammeln und zu analysieren.

## Was kann man damit machen?

Mit diesem Projekt kann man:

*   Webseiten nach bestimmten Inhalten durchsuchen.
*   Hyperlinks von Webseiten extrahieren.
*   Webseiteninhalte in einer Datenbank speichern.
*   Daten aus verschiedenen Webseiten zusammenführen und analysieren.
*   Verschiedene Web Scraping-Methoden nutzen: Jsoup und Selenium sind integriert.

## Wie funktioniert es?

Das Projekt verwendet Jsoup und Selenium, um Webseiten abzurufen.  Es interagiert mit einer Datenbank (MySQL) zum Speichern der gesammelten Daten.  Die Datenstruktur wird über JPA/Hibernate verwaltet.  Der Ablauf beinhaltet:

1.  Webseiteninhalte werden über Jsoup oder Selenium abgerufen.
2.  Traditionelle Hanzi werden per OpenCC in vereinfachte Hanzi konvertiert.
3.  Die extrahierten Daten (Text, Hyperlinks) werden in einem StringBuilder gespeichert.
4.  Hyperlinks werden in einem Set gespeichert.
5.  Die Daten werden in der Datenbank (tabelle `url`, `results`, `status`) gespeichert.

## Technische Details

*   **Programmiersprache:** Java
*   **Bibliotheken:** Jsoup, Selenium, OpenCC (opencc4j), MySQL Connector, JPA/Hibernate
*   **Datenbank:** MySQL oder PostgreSQL (per JDBC-URL)

## Konfiguration

*   Die Datenbankverbindung wird ueber `DB_CONNECTION_STRING` konfiguriert.
*   Die Konfiguration kann ueber Umgebungsvariablen oder eine Konfigurationsdatei erfolgen.

## Umgebungsvariablen

*   **DB_CONNECTION_STRING** (erforderlich): JDBC-URL, z. B. `jdbc:mysql://host:3306/db` oder `jdbc:postgresql://host:5432/db`.
*   **MAX_THREADS** (Default: `10`): Anzahl paralleler Worker.
*   **SLEEP_INTERVAL_MS** (Default: `5`): Delay zwischen zwei Fetch-Zyklen pro Worker in Millisekunden.
*   **WEBHANDLER** (Default: `jsoup`): `jsoup` oder `selenium`.
*   **OUTPUTHANDLER** (Default: `mysqldatabase`): `mysqldatabase` oder `file`.
*   **PROXY** (optional): Proxy-Host, z. B. `127.0.0.1`.
*   **PROXYPORT** (optional): Proxy-Port, z. B. `8080`.
*   **SINGLE_SPIDER_URL** (optional): Wenn gesetzt, werden nur URLs mit gleichem Schema/Host/Port und Pfad-Präfix gecrawlt. Alles andere wird ignoriert.
*   **CHECKOUT_TIMEOUT_MS** (Default: `600000`): Timeout fuer `check-out` URLs; danach werden sie erneut eingeplant.
*   **IGNORE_SELECTORS** (optional): CSS-Selektoren, die vor der Textextraktion entfernt werden; `default` nutzt eingebaute Standard-Selektoren.
*   **CONTENT_SELECTOR** (optional): CSS-Selektor, der den zu extrahierenden Inhalt eingrenzt; wenn nicht gesetzt, wird der gesamte `body` verwendet.
*   **HIBERNATE_SHOW_SQL** (Default: `false`): Gibt SQL in der Konsole aus.
*   **HIBERNATE_FORMAT_SQL** (Default: `false`): Formatiert ausgegebenes SQL.
*   **HIBERNATE_HBM2DDL_AUTO** (Default: `update`): Hibernate Schema-Strategie.

### Beispiel SINGLE_SPIDER_URL

```env
SINGLE_SPIDER_URL=https://www.reddit.com/r/China_irl/
```

Alle URLs innerhalb dieses Pfads werden gecrawlt, z. B.:

```text
https://www.reddit.com/r/China_irl/comments/...
```

Außerhalb liegende URLs werden ignoriert, z. B.:

```text
https://www.reddit.com/r/China/
https://www.reddit.com/r/China_irl2/
```

### Statuswerte

Diese Statuswerte werden beim Crawling gespeichert:

*   `check-out` – URL wurde für Verarbeitung reserviert.
*   `visited-ok` – Verarbeitung erfolgreich.
*   `visited-error` – Abruf/Verarbeitung fehlgeschlagen.
*   `visited-empty` – Kein Inhalt extrahiert.
*   `visited-no-han` – Keine Han-Zeichen gefunden.
*   `visited-invalid-protocol` – URL mit anderem Schema als http/https.
*   `visited-out-of-scope` – URL außerhalb des `SINGLE_SPIDER_URL`-Scopes.

### Beispiel (.env)

```env
DB_CONNECTION_STRING=jdbc:mysql://localhost:3306/hanzispider
MAX_THREADS=10
SLEEP_INTERVAL_MS=5
WEBHANDLER=jsoup
OUTPUTHANDLER=mysqldatabase
PROXY=
PROXYPORT=
SINGLE_SPIDER_URL=
CHECKOUT_TIMEOUT_MS=600000
IGNORE_SELECTORS=default
CONTENT_SELECTOR=
HIBERNATE_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false
HIBERNATE_HBM2DDL_AUTO=update
```

## Build & Start

*   Build (JAR mit Dependencies): `./build.sh` oder `mvn -q clean package -DskipTests`
*   Start per Skript: `./start.sh` (klassische Konfiguration)
*   Reddit-Run: `./start_reddit_crawl.sh` (liest `.env`)

## .env Dateien

*   `.env` wird von `start_reddit_crawl.sh` geladen.
*   `.env.example` ist eine Vorlage ohne echte Zugangsdaten.

## Output

*   **mysqldatabase**: Ergebnisse landen in `results` (Hanzi) sowie Status in `status`.
*   **file**: Ausgabe unter `~/.HanziSpider/out/` (UTF-8, Dateichunks).

## Schema (Mermaid)

*   Mermaid ER-Diagramm: `SQL/schema_diagram.mmd`

## Hinweise

*   **Selenium** benoetigt `geckodriver` im `PATH` (Firefox headless).
*   **Crash-Recovery**: `CHECKOUT_TIMEOUT_MS` gibt an, wann ein `check-out` erneut eingeplant wird.
*   **Traditionell -> Simplified**: OpenCC (opencc4j) konvertiert vor dem Hanzi-Filter.
```
