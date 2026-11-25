# Setup: GitHub Secrets und Environments

Dieser Guide beschreibt die Konfiguration von GitHub Secrets und Environments im Repository `rochdi1/lernportal`, damit die CI/CD-Pipeline sicher auf SonarQube, Docker Hub und die Deployment-Umgebungen zugreifen kann.

## 1. Repository Secrets anlegen

Gehe zu: **Repo → Settings → Secrets and variables → Actions → New repository secret**

Lege folgende Secrets an:

| Secret-Name | Beispielwert | Bedeutung |
|---|---|---|
| `SONAR_TOKEN` | `squ_abc123def456...` | API Token aus SonarQube-UI (Account → Security → Generate Token) |
| `SONAR_HOST_URL` | `http://localhost:9000` | URL des lokalen SonarQube (wird vom Runner aufgerufen) |
| `DOCKERHUB_USERNAME` | `rochdi1` | Dein Docker Hub Benutzername |
| `DOCKERHUB_TOKEN` | `dckr_pat_xyz...` | Docker Hub Access Token (Docker Hub → Account Settings → Access Tokens) |
| `ARGOCD_SERVER` | `http://localhost:8080` | URL des ArgoCD-Servers – muss erreichbar sein (z.B. nach Port‑Forwarding oder Minikube Service) |
| `ARGOCD_USERNAME` | `admin` | Nur nötig für ArgoCD-Workflow |
| `ARGOCD_PASSWORD` | `geheimes-pw` | Nur nötig für ArgoCD-Workflow |

> **Wichtig:** `ARGOCD_SERVER` muss auf einen Ort zeigen, der vom Self‑Hosted Runner aus erreichbar ist. Nach dem Installieren von ArgoCD in Minikube kannst du z.B. `kubectl port-forward svc/argocd-server -n argocd 8080:8080` nutzen und dann `http://localhost:8080` als Wert setzen.

## 2. SonarQube Token generieren

1. Öffne die SonarQube Web-UI: `http://localhost:9000`
2. Melde dich an (`admin / admin` – ggf. nach Erstinstallation ändern).
3. Gehe zu **Account → Security → Generate Token**.
4. Gib einen Namen ein (z.B. `github-actions`).
5. Klicke **Generate**, kopiere den Token und hinterlege ihn als Secret `SONAR_TOKEN`.

## 3. Docker Hub Token generieren

1. Öffne: `https://hub.docker.com/settings/security`
2. Klicke **New Access Token**, gib einen Namen ein, setze Berechtigungen (Read, Write, Delete), kopiere den Token und hinterlege ihn als Secret `DOCKERHUB_TOKEN`.
3. Trage deinen Benutzernamen als `DOCKERHUB_USERNAME` ein.

## 4. Environments anlegen

Gehe zu **Repo → Settings → Environments → New environment**.

| Environment-Name | Required Reviewers | Zweck |
|---|---|---|
| `development` | Keine (auto-deploy) | Automatischer Deploy nach Merge in `main` |
| `QS` | Ja (manuell) | Qualitätssicherung vor Produktion |
| `production` | Ja (manuell) | Produktivumgebung |

### Umgebung `development` anlegen
1. Klicke **New environment**, Name: `development`, keine Reviewer, **Create environment**.

### Umgebung `QS` anlegen
1. Klicke **New environment**, Name: `QS`, füge **Required reviewers** hinzu (z.B. dein GitHub‑User oder ein Team), **Create environment**.

### Umgebung `production` anlegen
1. Klicke **New environment**, Name: `production`, füge **Required reviewers** hinzu, **Create environment**.

### 4.1. Kubernetes‑Namespaces & ArgoCD‑Applications (alternativ via `kubectl apply`)

Für das Lern‑/Test‑Setup reicht es, eine **Entwicklungsumgebung** (Namespace `lernportal-dev`) sowie **QS** und **Produktion** als separate Kubernetes‑Namespaces zu erstellen. Zusätzlich lege wir ArgoCD‑Applications‑Resources an, die jeweils das richtige Overlay des Git‑Repo verweisen.

> **Hinweis:** GitHub‑Environments (`development`, `QS`, `production`) steuern lediglich die Deployment‑Gate‑/Approval‑Logik in der Workflow‑Pipeline. Die Kubernetes‑Namespaces sind rein für das K8s‑Cluster selbst gedacht.

#### 4.1.1. Namespaces erstellen (via kubectl)

```bash
kubectl create namespace lernportal-dev --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace lernportal-qs   --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace lernportal-prod --dry-run=client -o yaml | kubectl apply -f -
```

#### 4.1.2. ArgoCD‑Application‑Manifeste (via kubectl apply)

**Beispiel `application-dev.yaml`**

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: lernportal-dev
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/rochdi1/lernportal.git
    targetRevision: HEAD
    path: k8s/overlays/development
  destination:
    server: https://kubernetes.default.svc
    namespace: lernportal-dev
  syncPolicy:
    syncOnly:
      - group: apps
        kind: Deployment
        srcPaths:
          - k8s/overlays/development
```

Analog definierst du für **QS** und **Produktion**:

- `application-qs.yaml` → `path: k8s/overlays/qs`, Destination Namespace `lernportal-qs`.
- `application-prod.yaml` → `path: k8s/overlays/production`, Destination Namespace `lernportal-prod`.

> **Deployment‑Strategie:** In den jeweiligen Overlay‑Ordnern (`k8s/overlays/<env>`) findest du die Kubernetes‑Manifests (Deployments, Services, ConfigMaps, Secrets‑Refs). ArgoCD synchronisiert diese über Git‑Source.

## 5. Secrets sind NICHT im Repository zu speichern

⚠️ Geheimnisse dürfen niemals in Dateien (z.B. `.env`, `config.yml`) im Git-Repository gespeichert werden.
- `.env`-Dateien dürfen nicht committet werden.
- Füge `.env` zu `.gitignore` hinzu.
- Nutze ausschließlich die GitHub Secrets-Oberfläche.

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

## 8. ArgoCD Secrets & Configuration

ArgoCD wird in Minikube installiert. Die relevanten Secrets für das ArgoCD‑Workflow lauten:

| Secret | Wert | Bemerkung |
|---|---|---|
| `ARGOCD_SERVER` | `http://localhost:8080` | URL des ArgoCD‑Servers (nach Port‑Forwarding/Minikube Service) |
| `ARGOCD_USERNAME` | `admin` | Admin‑Benutzer von ArgoCD |
| `ARGOCD_PASSWORD` | *(dein Passwort)* | Setzt du beim Installieren eines Passwords |

Nach Installation von ArgoCD in Minikube kannst du den Workflow `cd-argocd.yml` ausführen. Der Schritt **Install ArgoCD CLI** fügt `/usr/local/bin/argocd` hinzu, bevor `argocd login …` ausgeführt wird.

## 9. ArgoCD Local Setup (optional)

Falls du den Self‑Hosted Runner direkt starten möchtest:

```bash
cd ~/actions-runner
./run.sh          # interaktiv
# oder
sudo ./svc.sh install && sudo ./svc.sh start
```

Stelle sicher, dass Docker‑Rechte vorhanden sind (`sudo usermod -aG docker $USER && sudo login`).

---

*Generiert am 2026‑11‑25*