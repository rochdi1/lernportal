# CD Deployment Guide – SonarQube, Docker, ArgoCD & Helm / kubectl

## Overview

This guide combines the setup steps for a continuous deployment (CD) pipeline for the **Lernportal** project (`rochdi1/lernportal`).

- SonarQube (via `docker-compose.sonarqube.yml`)
- Docker Build & Push for Backend & Frontend
- SonarQube Scan
- Deployment variants:
  1. **ArgoCD** (GitOps – used in the current workflow `cd-argocd.yml`)
  2. **Helm** (chart‑based deployment – workflow `cd-helm.yml`)
  3. **kubectl / Docker‑Compose** (manual deployment – workflow `cd-kubectl.yml`)

The goal is to provide a **single place in the docs** that explains the three possible CD strategies, why ArgoCD was chosen for the current implementation, and how the relevant pieces (secrets, runner, workflows) fit together.

---

## 1. Quick‑look at the three CD strategies

| Strategy | Main tool(s) | How it works | When to use it |
|----------|--------------|---------------|----------------|
| **ArgoCD** (GitOps) | `argocd` CLI (single tool) + Docker build‑push (backend/frontend) | ArgoCD runs inside the local Kubernetes cluster (Minikube). You define `Application` resources in Git. The workflow builds images, pushes them to Docker Hub, then logs in via `argocd login` and runs `argocd app set`, `argocd app sync`, `argocd app wait`. | Ideal for source‑of‑truth‑based deployments, rollbacks, multi‑environment (dev/QS/prod). |
| **Helm** (chart‑based) | `helm` + Docker build‑push (backend/frontend) | Instead of ArgoCD, you use Helm charts for backend/frontend/postgres. Workflow builds images and pushes them; then runs `helm upgrade --install` with a values file per environment. | Useful when you already have Helm charts and prefer release‑based versioning. |
| **kubectl / Docker‑Compose** (manual) | `kubectl` / `docker compose` | Simple deployment via docker‑compose or `kubectl apply -f docker-compose.sonarqube.yml`. No CI/CD orchestration; just build images and bring up containers locally. | Quick testing, small stacks, no GitOps requirement. |

> **Current implementation:** The repository is set up with **ArgoCD** as the primary deployment method (`.github/workflows/cd-argocd.yml`). The other workflows (`cd-helm.yml`, `cd-kubectl.yml`) exist as alternatives for flexibility/learning purposes.

---

## 2. SonarQube + Docker‑Compose (foundational)

### Files
- `docker-compose.sonarqube.yml` – SonarQube + PostgreSQL (ports `9000` for SonarQube, internal DB `5432`).
- `sonar-project.properties` – Sonar project key, sources, Java binaries, exclusions, coverage.

### Key configuration
- `sonar.java.binaries = lernportal-backend/build/classes/java/main`
- `sonar.java.source = 25`
- `sonar.sources = lernportal-backend/src/main,lernportal-frontend/src`
- `sonar.tests = lernportal-backend/src/test`
- Exclusions: `**/node_modules/**,**/build/**,**/dist/**,**/.angular/**,**/*.spec.ts`

### Required step before SonarQube Scan
Ensure backend compiled classes exist:

```bash
cd lernportal-backend
./gradlew build -x test
```

Otherwise the scan will fail with:

```
Invalid value for 'sonar.java.binaries' property
SonarScanner exited with code 1
```

---

## 3. CI/CD Workflow Overview

### `.github/workflows/cd-argocd.yml`
1. **backend**: JDK 25 → `./gradlew test` → `./gradlew build -x test`
2. **frontend**: Node 22 → `npm ci` → `npx ng test --watch=false` → `npx ng build --configuration production`
3. **sonarqube**: Build backend (`./gradlew build -x test`) → run `SonarSource/sonarqube-scan-action@master`
4. **docker**: Build & push backend & frontend images to Docker Hub.
5. **update-argocd**: (with the new step) Install ArgoCD CLI → `argocd login` → `argocd app set` → `argocd app sync` → `argocd app wait`.

### Differences in the other workflows

| Workflow | Deployment step |
|----------|----------------|
| `cd-helm.yml` | Same builds, but instead of ArgoCD steps it runs Helm commands (e.g., `helm upgrade --install ...`). |
| `cd-kubectl.yml` | Same builds, but deploys via `kubectl apply` / docker‑compose. No ArgoCD/Helm. |

> **Note:** The workflows share many jobs; only the final deployment job differs.

---

## 4. Setup of ArgoCD (Local)

### Prerequisites
- Linux, Docker, Docker Compose
- kubectl, minikube
- Java 25, Node 22
- Self‑hosted runner running locally (`local-runner`)

### Install ArgoCD (Helm)
```bash
helm repo add argo https://argoproj.github.io/helm-charts
helm repo update
helm install argocd argo/argo-cd -n argocd --create-namespace
```

### Port‑forward for local access
```bash
kubectl port-forward svc/argocd-server -n argocd 8080:8080
```

Now ArgoCD is reachable at `http://localhost:8080`.

### Runner & Secrets
- `ARGOCD_SERVER`: `http://localhost:8080`
- `ARGOCD_USERNAME`: `admin`
- `ARGOCD_PASSWORD`: *(your password)*

These must be set as GitHub Secrets.

---

## 5. Secrets & Environments

Full details are in **`.github/workflows/GITHUB_SECRETS_ENVIRONMENTS.md`** (or the updated version at `/home/casa/AI_Project/JavaAngular/docs/GITHUB_SECRETS_ENVIRONMENTS.md`).

Key secrets:

- `SONAR_TOKEN`
- `SONAR_HOST_URL`
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `ARGOCD_SERVER`
- `ARGOCD_USERNAME`
- `ARGOCD_PASSWORD`

Environments:

- `development`
- `QS`
- `production`

---

## 6. Decision Summary – Why ArgoCD Was Chosen

- **GitOps** is the most future‑proof approach: the cluster state lives in the Git repository, enabling rollbacks and multi‑environment configuration via one tool.
- **ArgoCD** integrates well with existing GitHub‑Actions: the runner can install the CLI and use the API to sync apps.
- **Helm** is excellent but requires you to maintain Helm charts and values per environment; ArgoCD can still call Helm templates if desired.
- **kubectl / Docker‑Compose** is simplest but lacks CI/CD orchestration and scalability.

Since the project already has a pipeline for builds and tests, ArgoCD is the logical final step for GitOps.

---

## 7. Next Steps

1. **Add the three missing GitHub Secrets** (`ARGOCD_SERVER`, `ARGOCD_USERNAME`, `ARGOCD_PASSWORD`) via the GitHub UI.  
2. **Start the local runner** (if not already running).  
3. **Run the workflow** (`git push` or manual dispatch).  
4. **Check logs** for the new step “Install ArgoCD CLI” and subsequent login/sync actions.

If any error persists (e.g., `sonar.java.binaries` or `argocd: command not found`), refer to the corresponding sections above.

---

## 8. Quick Cheat‑Sheet

```bash
# Install ArgoCD in Minikube
helm install argocd argo/argo-cd -n argocd --create-namespace

# Port‑forward
kubectl port-forward svc/argocd-server -n argocd 8080:8080

# Build backend
cd lernportal-backend && ./gradlew build -x test

# Start SonarQube + Postgres
docker compose -f docker-compose.sonarqube.yml up -d

# Trigger CD
git push origin feat/github-actions-local-pipeline
```

---

## 9. References

- `docs/deployment-recommendation.md`
- `docs/SETUP_SELF_HOSTED_RUNNER.md`
- `docs/GITHUB_SECRETS_ENVIRONMENTS.md`
- `docs/ARGOCD_LOCAL_SETUP.md`
- `.github/workflows/cd-argocd.yml`
- `.github/workflows/cd-helm.yml`
- `.github/workflows/cd-kubectl.yml`

---