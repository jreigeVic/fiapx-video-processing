# Diagrama de Implantação - Detalhado

## Objetivo

Detalhar a implantacao da plataforma em Kubernetes e servicos gerenciados AWS.

```mermaid
flowchart TB
  Dev["Developer"]
  GitHub["GitHub Repository"]
  Actions["GitHub Actions"]
  Registry["GitHub Container Registry"]

  subgraph Cluster["Kubernetes Cluster"]
    Ingress["Ingress Controller"]
    IdentityPod["Identity Service Pods"]
    VideoPod["Video Service Pods"]
    WorkerPod["Processing Worker Pods"]
    NotificationPod["Notification Service Pods"]
    Secrets["Kubernetes Secrets"]
    ConfigMaps["ConfigMaps"]
  end

  subgraph AWS["AWS Managed Services"]
    S3["Amazon S3"]
    SNS["Amazon SNS"]
    SQS["Amazon SQS"]
    RDS["Amazon RDS PostgreSQL"]
    CW["CloudWatch"]
  end

  NewRelic["New Relic"]

  Dev --> GitHub
  GitHub --> Actions
  Actions --> Registry
  Actions --> Cluster
  Ingress --> IdentityPod
  Ingress --> VideoPod
  WorkerPod --> SQS
  NotificationPod --> SQS
  VideoPod --> S3
  WorkerPod --> S3
  VideoPod --> SNS
  WorkerPod --> SNS
  IdentityPod --> RDS
  VideoPod --> RDS
  NotificationPod --> RDS
  IdentityPod --> NewRelic
  VideoPod --> NewRelic
  WorkerPod --> NewRelic
  NotificationPod --> NewRelic
  AWS --> CW
  Secrets --> IdentityPod
  Secrets --> VideoPod
  Secrets --> WorkerPod
  Secrets --> NotificationPod
  ConfigMaps --> IdentityPod
  ConfigMaps --> VideoPod
  ConfigMaps --> WorkerPod
  ConfigMaps --> NotificationPod
```

## Regras

- Cada servico tem imagem e deploy independente.
- Secrets nao devem ser commitados.
- RDS contem bancos logicos separados por servico.
- S3, SNS e SQS sao servicos gerenciados AWS.
