# Chapter 8 – Reactive Spring: Resilience and Scalability

This chapter introduces reactive programming for building cloud-native applications that are scalable, efficient, and resilient when handling I/O-bound work such as database access and service-to-service communication.

You will start building the **Order Service** for Polar Bookshop with Spring Boot, Spring WebFlux, and Spring Data R2DBC.\
The application uses Project Reactor’s `Mono` and `Flux` types, persists data through R2DBC, and exposes a non-blocking REST API.

Order Service also communicates with Catalog Service through Spring WebClient to retrieve book details and availability.\
Along the way, this chapter applies resilience patterns—including timeouts, retries, fallbacks, and error handling—to make remote calls more robust.\
Finally, it covers testing reactive REST clients, data persistence, and controllers with Spring, Reactor, mock web servers, and Testcontainers.

All commands start from **`Chapter08/`**.

Make sure that you are in `Chapter08` directory.

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter08
```

## What is in this chapter?

| Directory                             | Purpose                                                                                                      |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------|
| [config-service/](config-service)     | Spring Cloud Config Server on port `8888`                                                                    |
| [catalog-service/](catalog-service)   | Catalog API on port `9001`; obtains external configuration from Config Service                               |
| [config-repo/](config-repo)           | Configuration served by Config Service, local mirror of [fResult-PolarBookshop/config-repo][org-config-repo] |
| [polar-deployment/](polar-deployment) | Docker Compose and Kubernetes manifests, including the aggregate development Tiltfile                        |

## Prerequisites

- **JDK 26** and the included Gradle wrappers (a separate Gradle installation is unnecessary)
- **Docker Desktop** or **Docker Engine** running locally
- **`kubectl`**, **Minikube**, and **Tilt**
- **HTTPie** or **`curl`** for the API request examples
- *Optional:* **Headlamp Desktop** for visual inspection and **Kubeconform** for manifest validation

On macOS, install the core tools with Homebrew:

```console
brew install kubectl minikube tilt
brew install --cask headlamp # optional
kubectl version --client
minikube version
tilt version
```

On Linux or Windows, install the equivalent tools using the official [kubectl](https://kubernetes.io/docs/tasks/tools/), [Minikube](https://minikube.sigs.k8s.io/docs/start/), and [Tilt](https://docs.tilt.dev/install.html) instructions.\
The Tilt image helper scripts use a POSIX shell, so native Windows needs an equivalent script or WSL.

> [!WARNING]
> Docker Desktop and the `polar` Minikube profile keep separate image stores.\
> The Tilt build helpers load each generated application image into Minikube; the cluster cannot pull a host-only image.
