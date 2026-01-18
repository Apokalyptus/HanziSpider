# Projektanalyse

```markdown
# HanzeSpider

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
2.  Die extrahierten Daten (Text, Hyperlinks) werden in einem StringBuilder gespeichert.
3.  Hyperlinks werden in einem Set gespeichert.
4.  Die Daten werden in der Datenbank (tabelle `url`, `results`, `status`) gespeichert.

## Technische Details

*   **Programmiersprache:** Java
*   **Bibliotheken:** Jsoup, Selenium, MySQL Connector, JPA/Hibernate
*   **Datenbank:** MySQL

## Konfiguration

*   Die MySQL-Verbindung muss mit den entsprechenden Zugangsdaten (Host, Port, Benutzername, Passwort, Datenbankname) konfiguriert werden.
*   Die Konfiguration kann über Umgebungsvariablen oder eine Konfigurationsdatei erfolgen.
```