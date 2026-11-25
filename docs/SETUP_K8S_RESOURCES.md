# Setup: Kubernetes- und Helm-Ressourcen für die Deployment-Pipeline

Dieser Guide beschreibt die Erstellung der benötigten Kubernetes-Manifests (Kustomize) und Helm-Charts, die von den GitHub Actions Workflows (`cd-kubectl.yml` und `cd-helm.yml`) erwartet werden.

---

## 1. Verzeichnisstruktur

Erstelle folgende Ordnerstruktur im Repository-Root:

```text
k8s/
  base/
    deployment-backend.yaml
    deployment-frontend.yaml
    service-backend.yaml
    service-frontend.yaml
    kustomization.yaml
  overlays/
    dev/
      kustomization.yaml
    prod/
      kustomization.yaml
helm/
  backend/
    Chart.yaml
    values.yaml
    values-dev.yaml
    values-prod.yaml
    templates/
      deployment.yaml
      service.yaml
  frontend/
    Chart.yaml
    values.yaml
    values-dev.yaml
    values-prod.yaml
    templates/
      deployment.yaml
      service.yaml
```

---

## 2. Kustomize – Basis-Manifests (`k8s/base/`)

### `k8s/base/deployment-backend.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lernportal-backend
  labels:
    app: lernportal
    component: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: lernportal
      component: backend
  template:
    metadata:
      labels:
        app: lernportal
        component: backend
    spec:
      containers:
        - name: backend
          image: docker.io/rochdi1/lernportal-backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "dev"
```

### `k8s/base/deployment-frontend.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lernportal-frontend
  labels:
    app: lernportal
    component: frontend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: lernportal
      component: frontend
  template:
    metadata:
      labels:
        app: lernportal
        component: frontend
    spec:
      containers:
        - name: frontend
          image: docker.io/rochdi1/lernportal-frontend:latest
          ports:
            - containerPort: 80
```

### `k8s/base/service-backend.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: lernportal-backend
  labels:
    app: lernportal
    component: backend
spec:
  selector:
    app: lernportal
    component: backend
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
```

### `k8s/base/service-frontend.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: lernportal-frontend
  labels:
    app: lernportal
    component: frontend
spec:
  selector:
    app: lernportal
    component: frontend
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
  type: ClusterIP
```

### `k8s/base/kustomization.yaml`

```yaml
resources:
  - deployment-backend.yaml
  - deployment-frontend.yaml
  - service-backend.yaml
  - service-frontend.yaml
commonLabels:
  app: lernportal
```

---

## 3. Overlays – Umgebungsspezifische Anpassungen (`k8s/overlays/`)

### `k8s/overlays/dev/kustomization.yaml`

```yaml
resources:
  - ../../base

patches:
  - path: patch-dev.yaml
```

### `k8s/overlays/dev/patch-dev.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lernportal-backend
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: backend
          image: docker.io/rochdi1/lernportal-backend:dev-latest
```

### `k8s/overlays/prod/kustomization.yaml`

```yaml
resources:
  - ../../base

patches:
  - path: patch-prod.yaml
```

### `k8s/overlays/prod/patch-prod.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lernportal-backend
spec:
  replicas: 2
```

---

## 4. Helm Charts (`helm/`)

### `helm/backend/Chart.yaml`

```yaml
apiVersion: v2
name: lernportal-backend
description: Helm chart for the Lernportal Backend
type: application
version: 0.1.0
appVersion: "1.0"
```

### `helm/backend/values.yaml`

```yaml
replicaCount: 1

image:
  repository: docker.io/rochdi1/lernportal-backend
  pullPolicy: IfNotPresent
  tag: ""

service:
  type: ClusterIP
  port: 80

resources: {}
nodeSelector: {}
tolerations: []
affinity: {}
```

### `helm/backend/values-dev.yaml`

```yaml
replicaCount: 1

image:
  tag: "dev-latest"

service:
  type: ClusterIP
```

### `helm/backend/values-prod.yaml`

```yaml
replicaCount: 2

image:
  tag: "{{ .Chart.AppVersion }}"

service:
  type: LoadBalancer
```

### `helm/backend/templates/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lernportal-backend
  labels:
    app: lernportal
    component: backend
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: lernportal
      component: backend
  template:
    metadata:
      labels:
        app: lernportal
        component: backend
    spec:
      containers:
        - name: backend
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          ports:
            - containerPort: 8080
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
```

### `helm/backend/templates/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: lernportal-backend
  labels:
    app: lernportal
    component: backend
spec:
  selector:
    app: lernportal
    component: backend
  ports:
    - protocol: TCP
      port: {{ .Values.service.port }}
      targetPort: 8080
  type: {{ .Values.service.type }}
```

### `helm/frontend/Chart.yaml`

```yaml
apiVersion: v2
name: lernportal-frontend
description: Helm chart for the Lernportal Frontend
type: application
version: 0.1.0
appVersion: "1.0"
```

### `helm/frontend/values.yaml`

```yaml
replicaCount: 1

image:
  repository: docker.io/rochdi1/lernportal-frontend
  pullPolicy: IfNotPresent
  tag: ""

service:
  type: ClusterIP
  port: 80
```

### `helm/frontend/values-dev.yaml`

```yaml
replicaCount: 1

image:
  tag: "dev-latest"
```

### `helm/frontend/values-prod.yaml`

```yaml
replicaCount: 2

image:
  tag: "{{ .Chart.AppVersion }}"

service:
  type: LoadBalancer
```

---

## 5. Minikube starten und Deploy testen

```bash
# 5.1 Minikube starten (falls noch nicht laufend)
minikube start --driver=docker --cpus=4 --memory=8192

# 5.2 Minikube-Status prüfen
minikube status

# 5.3 Kustomize Deploy (Dev-Umgebung)
kubectl apply -k k8s/overlays/dev

# 5.4 Helm Deploy (Dev-Umgebung)
helm upgrade --install lernportal-backend ./helm/backend \
  --values ./helm/backend/values-dev.yaml \
  --namespace lernportal-dev --create-namespace

helm upgrade --install lernportal-frontend ./helm/frontend \
  --values ./helm/frontend/values-dev.yaml \
  --namespace lernportal-dev --create-namespace

# 5.5 Status prüfen
kubectl get pods -n lernportal-dev
kubectl get services -n lernportal-dev
```

---

## 6. Workflow-Trigger (nach Einrichtung)

Nach Erstellung der K8s-Ressourcen und Einrichtung des Runners:

```bash
git checkout -b feat/test-pipeline
git add .
git commit -m "Test: K8s-Ressourcen und Pipeline"
git push origin feat/test-pipeline
```

GitHub Actions läuft automatisch über den `push`-Trigger. Die Pipeline führt dann aus:

1. Backend & Frontend Tests
2. SonarQube Scan (localhost:9000)
3. Docker Build & Push nach Docker Hub
4. Deploy nach Minikube (kubectl oder Helm, je nach Workflow)

---

## 7. Checkliste

| Aufgabe | Status |
|---|---|
| `k8s/base/` mit Deployment und Service erstellt | ⬜ |
| `k8s/overlays/dev/` und `k8s/overlays/prod/` erstellt | ⬜ |
| `helm/backend/` und `helm/frontend/` mit Templates erstellt | ⬜ |
| Minikube läuft | ⬜ |
| `kubectl apply -k k8s/overlays/dev` erfolgreich | ⬜ |
| Helm Deploy erfolgreich | ⬜ |

---