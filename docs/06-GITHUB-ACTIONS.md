# GitHub Actions Workflow – Erklärung & Konfiguration

> **Ziel:** Dieses Dokument erklärt, wie der aktuelle CI‑Workflow funktioniert, warum er fehlschlägt und wie man ihn für Lernzwecke vereinfacht. Später kann das Submodul‑Setup sauber nachgerüstet werden.

---

## 1. Was sind GitHub Actions?

GitHub Actions ist ein **CI/CD‑System**, das direkt in GitHub integriert ist.  
Ein **Workflow** wird als YAML‑Datei unter `.github/workflows/*.yml` definiert und besteht aus:

| Bestandteil | Beschreibung |
|-------------|--------------|
| **name** | Anzeigename des Workflows (z. B. `CI`). |
| **on** | Trigger‑Events (push, pull_request, schedule, …). |
| **jobs** | Ein oder mehrere Jobs, die parallel oder sequenziell laufen. |
| **jobs.<id>.runs‑on** | Runner‑Image (z. B. `ubuntu-latest`). |
| **jobs.<id>.steps** | Liste von **Steps** – jeder Step führt eine Aktion aus (Checkout, Setup, Script, …). |
| **uses** | Verweis auf eine wiederverwendbare Action (z. B. `actions/checkout@v4`). |
| **run** | Inline‑Shell‑Kommando. |
| **with** | Eingabeparameter für eine Action (z. B. `submodules: recursive`). |

---

## 2. Aktueller Workflow (`.github/workflows/ci.yml`)

```yaml
name: CI

on:
  push:
    branches:
      - main
      - 'feat/frontend-*'
      - 'feat/backend-*'
  pull_request:
    branches:
      - main

jobs:
  backend:
    name: Backend (Java/Spring Boot)
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: lernportal-backend
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          submodules: recursive        # ← versucht Submodule zu laden
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      - name: Run backend tests
        run: ./gradlew test

  frontend:
    name: Frontend (Angular)
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: lernportal-frontend
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          submodules: recursive        # ← versucht Submodule zu laden
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'
      - name: Install dependencies
        run: npm ci                    # benötigt package‑lock.json
      - name: Run frontend tests
        run: npx ng test --watch=false --browsers=ChromeHeadless
```

### Erklärung der wichtigsten Teile

| Section | Was passiert |
|---------|--------------|
| `on.push.branches` | Workflow läuft bei Push auf `main` und Feature‑Branches (`feat/frontend-*`, `feat/backend-*`). |
| `on.pull_request.branches` | Auch bei PRs gegen `main`. |
| `backend.job` | Läuft im Ordner `lernportal-backend`. Nutzt Java 25, führt Gradle‑Tests aus. |
| `frontend.job` | Läuft im Ordner `lernportal-frontend`. Nutzt Node 22, macht `npm ci` und Angular‑Tests. |
| `submodules: recursive` | **Wichtig:** Sagt dem Checkout, alle Submodule rekursiv zu klonen. **Voraussetzung:** Eine Datei `.gitmodules` muss im Repository existieren. |

---

## 3. Das aktuelle Problem – Fehlende `.gitmodules`

Der Ordner `lernportal-frontend` ist **als Git‑Submodul** im Haupt‑Repo registriert (Git‑Link, Modus `160000`).  
Aber die **Datei `.gitmodules` fehlt** komplett. Deshalb schlägt der Checkout mit `submodules: recursive` fehl:

```
The process '/usr/bin/git' failed with exit code 128
No url found for submodule path 'lernportal-frontend' in .gitmodules
```

**Folgen:**
- Beide Jobs (Backend & Frontend) brechen **beim Checkout** ab.
- `npm ci` wird nie erreicht.

**Lösung (später):** `.gitmodules` anlegen:

```ini
[submodule "lernportal-frontend"]
	path = lernportal-frontend
	url = https://github.com/rochdi1/lernportal-frontend.git
```

Dann `git add .gitmodules lernportal-frontend` und committen.

---

## 4. Plan: Erst einfacher Workflow (ohne Submodule) zum Lernen

Um **schnell einen grünen Build** zu haben und die Grundlagen von GitHub Actions zu verstehen, schlagen wir folgende Schritte vor:

### Option A – Frontend als normaler Ordner (kein Submodul)

1. **Submodul auflösen** (einmalig):
   ```bash
   # Im Haupt‑Repo
   git rm --cached lernportal-frontend   # Submodul‑Eintrag entfernen
   rm -rf lernportal-frontend/.git       # .git im Submodul löschen
   git add lernportal-frontend           # Dateien als normale Dateien hinzufügen
   git commit -m "chore: convert frontend submodule to regular folder"
   ```
2. **Workflow anpassen** – `submodules: recursive` entfernen:
   ```yaml
   - name: Checkout repository
     uses: actions/checkout@v4          # ohne with: submodules
   ```
3. **Commit & Push** – CI läuft durch.

### Option B – Submodul behalten, aber `.gitmodules` nachliefern (für später)

1. `.gitmodules` anlegen (siehe oben).
2. `submodules: recursive` im Workflow lassen.
3. Commit & Push – CI läuft durch.

> **Empfehlung für den Lernstart:** **Option A** – damit entfällt die Submodul‑Komplexität und du siehst sofort, wie ein simpler Workflow aussieht.

---

## 5. Konfigurations‑Leitfaden (Schritt für Schritt)

### 5.1 Workflow‑Datei erstellen / bearbeiten

- **Ort:** `.github/workflows/ci.yml` (oder beliebig `*.yml` im gleichen Ordner).
- **Syntax:** YAML – Einrückung mit **2 Leerzeichen**, keine Tabs.
- **Validierung:** GitHub prüft Syntax beim Push; lokal kann `yamllint` helfen.

### 5.2 Trigger definieren (`on`)

```yaml
on:
  push:
    branches: [main, 'feat/**']
  pull_request:
    branches: [main]
```
- `branches` kann ein Array oder ein String‑Pattern sein.
- Weitere Events: `schedule`, `workflow_dispatch`, `release`, …

### 5.3 Jobs & Runner

```yaml
jobs:
  backend:
    runs-on: ubuntu-latest
    # oder: windows-latest, macos-latest, self‑hosted
```

### 5.4 Working‑Directory setzen

```yaml
defaults:
  run:
    working-directory: lernportal-backend   # gilt für alle run‑Steps dieses Jobs
```

### 5.5 Steps aufbauen

**Typische Step‑Reihenfolge:**

1. **Checkout**
   ```yaml
   - uses: actions/checkout@v4
     # with:
     #   submodules: recursive   # nur bei Submodulen nötig
   ```
2. **Tool‑Setup** (Java, Node, Python, …)
   ```yaml
   - uses: actions/setup-java@v4
     with:
       distribution: temurin
       java-version: '25'
   ```
   ```yaml
   - uses: actions/setup-node@v4
     with:
       node-version: '22'
       cache: 'npm'               # optional: cached node_modules
   ```
3. **Dependencies installieren**
   - **Java/Gradle:** `./gradlew test` (inkl. Dependency‑Download).
   - **Node/npm:** `npm ci` (benötigt `package-lock.json`) oder `npm install`.
4. **Tests / Build / Lint ausführen**
   ```yaml
   - run: ./gradlew test
   - run: npx ng test --watch=false --browsers=ChromeHeadless
   ```

### 5.6 Caching (optional, aber empfohlen)

```yaml
- uses: actions/cache@v4
  with:
    path: ~/.gradle/caches
    key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      gradle-${{ runner.os }}-
```

Für npm:
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.npm
    key: npm-${{ runner.os }}-${{ hashFiles('package-lock.json') }}
    restore-keys: |
      npm-${{ runner.os }}-
```

### 5.7 Artefakte & Reports (optional)

```yaml
- uses: actions/upload-artifact@v4
  with:
    name: test-results
    path: '**/build/reports/tests/**'
```

---

## 6. Beispiel: Vereinfachter Workflow (ohne Submodul)

```yaml
# .github/workflows/ci-simple.yml
name: CI Simple

on:
  push:
    branches: [main, 'feat/**']
  pull_request:
    branches: [main]

jobs:
  backend:
    name: Backend (Java/Spring Boot)
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: lernportal-backend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
      - run: chmod +x gradlew
      - run: ./gradlew test

  frontend:
    name: Frontend (Angular)
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: lernportal-frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'
      - run: npm ci
      - run: npx ng test --watch=false --browsers=ChromeHeadless
```

**Unterschiede zum aktuellen Workflow:**
- Kein `submodules: recursive`.
- `cache: 'npm'` beim Node‑Setup für schnellere Runs.
- Getrennt in zwei Dateien möglich (`ci.yml` + `ci-simple.yml`) – GitHub führt beide aus.

---

## 7. Nächste Schritte für dich

| Schritt | Kommando / Aktion | Ziel |
|--------|-------------------|------|
| 1. Submodul auflösen (optional) | `git rm --cached lernportal-frontend && rm -rf lernportal-frontend/.git && git add lernportal-frontend && git commit -m "chore: make frontend a regular folder"` | Einfacher Workflow ohne Submodul‑Hürden |
| 2. Workflow anpassen | `submodules: recursive` entfernen (oder neue Datei `ci-simple.yml` anlegen) | CI läuft durch |
| 3. Push & prüfen | `git push origin feat/frontend-phase-1` | GitHub Actions Tab → grüner Haken |
| 4. Lokale Tests | `cd lernportal-frontend && npm ci && npx ng test --watch=false` | Sicherstellen, dass Tests lokal grün sind |
| 5. Später: Submodul sauber einrichten | `.gitmodules` anlegen, `submodules: recursive` wieder aktivieren | Wenn du Submodule wirklich brauchst |

---

## 8. Häufige Fragen & Tipps

| Frage | Antwort |
|-------|---------|
| **Warum `npm ci` statt `npm install`?** | `npm ci` installiert **exakt** die Versionen aus `package-lock.json` – deterministisch, schneller im CI. |
| **Was macht `cache: 'npm'`?** | Speichert `~/.npm` zwischen Runs → `npm ci` wird deutlich schneller. |
| **Wie debugge ich einen fehlschlagenden Step?** | Im Actions‑Log auf den Step klicken → `Run …` ausklappen → Fehlermeldung lesen. Lokal mit `act` (https://github.com/nektos/act) simulieren. |
| **Kann ich mehrere Workflows parallel haben?** | Ja, jede `.yml` unter `.github/workflows/` ist ein eigenständiger Workflow. |
| **Wie setze ich Secrets (API‑Keys)?** | Repository → Settings → Secrets and variables → Actions → New repository secret. Im Workflow: `${{ secrets.MY_SECRET }}`. |

---

## 9. Weiterführende Links

- **Offizielle Doku:** https://docs.github.com/en/actions
- **Workflow‑Syntax:** https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions
- **Actions Checkout:** https://github.com/actions/checkout
- **Setup‑Node:** https://github.com/actions/setup-node
- **Caching Dependencies:** https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows
- **Submodules in Actions:** https://github.com/actions/checkout#checkout-submodules

---

> **Tipp:** Erstelle für Experimente einen eigenen Branch (z. B. `feat/learn-github-actions`) und spiele dort mit Workflow‑Dateien. So bleibt `main` sauber.

---

*Generiert am $(date) – basierend auf dem aktuellen Repository‑Stand.*  
*Passe Pfade und Versionen an dein Projekt an.*