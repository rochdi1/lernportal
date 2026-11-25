# Setup: GitHub Secrets und Environments

Dieser Guide beschreibt die Konfiguration von GitHub Secrets und Environments im Repository `rochdi1/lernportal`, damit die CI/CD-Pipeline sicher auf SonarQube, Docker Hub und die Deployment-Umgebungen zugreifen kann.

---

## 1. Repository Secrets anlegen

Gehe zu:  
**Repo → Settings → Secrets and variables → Actions → New repository secret**

Lege folgende Secrets an:

| Secret-Name | Beispielwert | Bedeutung |
|---|---|---|
| `SONAR_TOKEN` | `squ_abc123def456...` | API Token aus SonarQube-UI (Account → Security → Generate Token) |
| `SONAR_HOST_URL` | `http://localhost:9000` | URL des lokalen SonarQube (wird vom Runner aufgerufen) |
| `DOCKERHUB_USERNAME` | `rochdi1` | Dein Docker Hub Benutzername |
| `DOCKERHUB_TOKEN` | `dckr_pat_xyz...` | Docker Hub Access Token (Docker Hub → Account Settings → Access Tokens) |
| `ARGOCD_SERVER` | `argocd.example.com` | Nur nötig für ArgoCD-Workflow |
| `ARGOCD_USERNAME` | `admin` | Nur nötig für ArgoCD-Workflow |
| `ARGOCD_PASSWORD` | `geheimes-pw` | Nur nötig für ArgoCD-Workflow |

---

## 2. SonarQube Token generieren

1. Öffne die SonarQube Web-UI: `http://localhost:9000`
2. Melde dich an (Standard-Admin: `admin` / `admin` – bitte nach Erstinstallation ändern)
3. Gehe zu: **Account → Security → Generate Token**
4. Gib einen Namen ein (z. B. `github-actions`)
5. Klicke auf **Generate**
6. Kopiere den Token (z. B. `squ_...`) – dieser wird nur einmal angezeigt
7. Hinterlege ihn als Secret `SONAR_TOKEN` im GitHub Repository

---

## 3. Docker Hub Token generieren

1. Öffne: `https://hub.docker.com/settings/security`
2. Klicke auf **New Access Token**
3. Gib einen Namen ein (z. B. `github-actions`)
4. Setze Berechtigungen: **Read, Write, Delete** (oder nur Read/Write für Push)
5. Kopiere den Token (z. B. `dckr_pat_...`)
6. Hinterlege ihn als Secret `DOCKERHUB_TOKEN` im GitHub Repository
7. Trage deinen Benutzernamen als `DOCKERHUB_USERNAME` ein

---

## 4. Environments anlegen

Gehe zu:  
**Repo → Settings → Environments → New environment**

Lege folgende Environments an:

| Environment-Name | Required Reviewers | Zweck |
|---|---|---|
| `development` | Keine (auto-deploy) | Automatischer Deploy nach Merge in `main` |
| `QS` | Benutzer/Team (manuell) | Qualitätssicherung vor Produktion |
| `production` | Benutzer/Team (manuell) | Produktivumgebung |

### Umgebung `development` anlegen

1. Klicke auf **New environment**
2. Name: `development`
3. **Protection rules**: Keine `Required reviewers` hinzufügen (Auto-Deploy)
4. Klicke auf **Create environment**

### Umgebung `QS` anlegen

1. Klicke auf **New environment**
2. Name: `QS`
3. **Protection rules**: `Required reviewers` hinzufügen (z. B. deinen GitHub-User oder ein Team)
4. Klicke auf **Create environment**

### Umgebung `production` anlegen

1. Klicke auf **New environment**
2. Name: `production`
3. **Protection rules**: `Required reviewers` hinzufügen
4. Klicke auf **Create environment**

---

## 5. Secrets sind NICHT im Repository zu speichern

⚠️ **Wichtig:** Geheimnisse dürfen niemals in Dateien (z. B. `.env`, `config.yml`) im Git-Repository gespeichert werden.

- `.env`-Dateien dürfen nicht committet werden
- Füge `.env` zu `.gitignore` hinzu (falls noch nicht geschehen)
- Nutze ausschließlich die GitHub Secrets-Oberfläche (Repository Settings)

---

## 6. Verwendung in den Workflows

Die Secrets werden in den Workflow-Dateien wie folgt verwendet:

**Beispiel aus `cd-kubectl.yml`:**

```yaml
- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKERHUB_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}

- name: SonarQube Scan
  uses: SonarSource/sonarqube-scan-action@master
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

**Beispiel Environment-Nutzung im Workflow:**

```yaml
jobs:
  deploy-dev:
    environment:
      name: development
  deploy-prod:
    environment:
      name: production
```

---

## 7. Checkliste

| Aufgabe | Status |
|---|---|
| `SONAR_TOKEN` als GitHub Secret hinterlegt | ⬜ |
| `SONAR_HOST_URL` als GitHub Secret hinterlegt | ⬜ |
| `DOCKERHUB_USERNAME` als GitHub Secret hinterlegt | ⬜ |
| `DOCKERHUB_TOKEN` als GitHub Secret hinterlegt | ⬜ |
| Environment `development` angelegt (auto-deploy) | ⬜ |
| Environment `QS` angelegt (mit Reviewern) | ⬜ |
| Environment `production` angelegt (mit Reviewern) | ⬜ |
| `.gitignore` enthält `.env` | ⬜ |

---


./config.sh \
  --url https://github.com/rochdi1/lernportal \
  --token <DAS_TOKEN> \
  --name local-runner \
  --labels self-hosted,local,docker,k8s,sonarqube
