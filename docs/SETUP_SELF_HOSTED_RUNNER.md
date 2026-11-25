# Setup: Self-Hosted Runner für GitHub Actions

Dieser Guide beschreibt die Einrichtung eines Self-Hosted Runners auf deiner lokalen Maschine, damit GitHub Actions Jobs direkt lokal ausführen kann – insbesondere für den Zugriff auf lokalen SonarQube (localhost:9000) und Minikube.

---

## 1. Voraussetzungen

Stelle sicher, dass auf deiner Maschine installiert ist:

- **Linux** (hier getestet: Ubuntu/Debian-basiert)
- **Docker** & **Docker Compose**
- **kubectl** & **minikube**
- **Java 25** (für Gradle-Builds)
- **Node.js 22** (für Angular-Builds/Tests)
- **git** mit konfiguriertem Benutzernamen und E-Mail

---

## 2. Runner-Token von GitHub abrufen

1. Öffne das Repository im Browser: `https://github.com/rochdi1/lernportal`
2. Gehe zu: **Settings → Actions → Runners**
3. Klicke auf **„New runner“**
4. Wähle: **Linux** und **x64**
5. GitHub zeigt einen **Runner Token** an – kopiere ihn (für Schritt 4 benötigt)

---

## 3. Runner herunterladen und konfigurieren

```bash
# 3.1 Verzeichnis erstellen und wechseln
mkdir -p ~/actions-runner
cd ~/actions-runner

# 3.2 Binary herunterladen (aktuellste stabile Version)
curl -o actions-runner-linux-x64-2.317.0.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.317.0/actions-runner-linux-x64-2.317.0.tar.gz

# 3.3 Entpacken
tar xzf actions-runner-linux-x64-2.317.0.tar.gz

# 3.4 Runner konfigurieren
# <TOKEN> durch den Runner Token aus Schritt 2 ersetzen
./config.sh \
  --url https://github.com/rochdi1/lernportal \
  --token <TOKEN> \
  --name local-runner \
  --labels self-hosted,local,docker,k8s,sonarqube
```

> **Wichtig:** Die Labels müssen exakt so lauten wie in den Workflows verwendet (`runs-on: [self-hosted, local]`).

---

## 4. Runner starten

### Variante A – Interaktiv (Terminal bleibt geöffnet)

```bash
cd ~/actions-runner
./run.sh
```

### Variante B – Als System-Service (empfohlen für Dauerbetrieb)

```bash
cd ~/actions-runner
sudo ./svc.sh install
sudo ./svc.sh start
```

Nach dem Start erscheint der Runner unter GitHub: **Settings → Actions → Runners** als `local-runner` (Status: Idle / Online).

---

## 5. Docker-Rechte prüfen (falls nötig)

Falls Docker-Befehle im Runner nicht ohne `sudo` funktionieren:

```bash
sudo usermod -aG docker $USER
# Danach abmelden und erneut anmelden, oder den Runner neu starten
```

---

## 6. Überprüfung

```bash
cd ~/actions-runner
./bin/actions-runner list
```

In GitHub unter **Settings → Actions → Runners** sollte der Runner als **Online** sichtbar sein.

---

## 7. Probleme beheben

| Problem | Lösung |
|---|---|
| Runner nicht online | `./run.sh` neu starten oder Service prüfen mit `sudo ./svc.sh status` |
| Zugriff auf SonarQube nicht möglich | SonarQube muss laufen: `docker compose -f docker-compose.sonarqube.yml up -d` |
| Zugriff auf Minikube nicht möglich | Minikube muss laufen: `minikube start --driver=docker --cpus=4 --memory=8192` |
| Permission denied bei Docker | `sudo usermod -aG docker $USER` + neu einloggen |

---

## 8. Erster Test

Nach erfolgreicher Einrichtung des Runners kannst du den Workflow manuell triggern:

1. GitHub: **Actions → CI** → **Run workflow**
2. Oder einfach `git push` auf einen Feature-Branch (`feat/*`)

Der Job sollte nun auf dem lokalen Runner laufen und Zugriff auf localhost:9000 sowie Minikube haben.

---