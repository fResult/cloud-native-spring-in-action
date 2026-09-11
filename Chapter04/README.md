# Chapter 4 - Externalized Configuration Management

This chapter focuses on managing application configuration externally using **Spring Boot and Spring Cloud Config**.

We start by exploring configuration properties and profiles in Spring Boot, then learn how to externalize configuration using command-line arguments, JVM system properties, and environment variables.\
This allows the same application artifact to be deployed across different environments with different configurations.

Next, we build a centralized configuration management solution using **Spring Cloud Config Server**, with Git as the configuration data store.\
We then configure the application as a Spring Cloud Config Client, make both the server and client resilient, and explore how to refresh configuration at runtime without rebuilding the application.

## Prerequisites

- **Java 25+**
- [Docker](https://docs.docker.com/engine/install)
- [Kubernetes](https://kubernetes.io/releases/download)
- [**Grype**](https://oss.anchore.com/docs/installation/grype) – a powerful vulnerability scanner
- [HTTPie](https://httpie.io/cli)

## Development Scripts

### Running Grype

After building the project (`./gradlew build`), from the project root directory, scan for vulnerabilities:

```console
→ grype .
 ✔ Vulnerability DB                [updated]  
 ✔ Indexed file system                                                                                                    .
 ✔ Cataloged contents
   ├── ✔ Packages                        [0 packages]  
   └── ✔ Executables                     [0 executables]  
 ✔ Scanned for vulnerabilities     [0 vulnerability matches]  
   ├── by severity: 0 critical, 0 high, 0 medium, 0 low, 0 negligible
   └── by status:   0 fixed, 0 not-fixed, 0 ignored
No vulnerabilities found
```

The output will list any discovered vulnerabilities.

> [!NOTE]
> The official source code for this chapter is available at [ThomasVitale/cloud-native-spring-in-action/Chapter04/04-end/catalog-service](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter04/04-end/catalog-service).

### Testing the REST API

After starting the application (e.g., using `./gradlew bootRun`), you can use HTTPie to test the REST API.

First, from a terminal, add a new book to the catalog using the `POST /books` endpoint:

```console
→ http POST :9001/books author="Lyra Silverstar" \
  title="Northern Lights" isbn="1234567891" price=9.90
HTTP/1.1 201
Content-Type: application/json

{
  "author": "Lyra Silverstar",
  "isbn": "1234567891",
  "price": 9.90,
  "title": "Northern Lights"
}
```

Then, verify the book was created by querying it with the `GET /books/{isbn}` endpoint:

```console
→ http :9001/books/1234567891
HTTP/1.1 200
Content-Type: application/json

{
  "author": "Lyra Silverstar",
  "isbn": "1234567891",
  "price": 9.90,
  "title": "Northern Lights"
}
```

## Containerizing the Application Locally

### Building the Docker Image

First, create a Docker image from the source code using the following command:

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter04
→ cd catalog-service && ./gradlew bootBuildImage
> Task :bootBuildImage

...

Successfully built image 'docker.io/library/catalog-service:0.0.1-SNAPSHOT'

BUILD SUCCESSFUL
```

### Running and Testing the Container

After the build finishes, you can verify that the Docker image was created successfully by listing your Docker images.

```console
→ docker images --digest catalog-service:0.0.1-SNAPSHOT
REPOSITORY        TAG              DIGEST          IMAGE ID       CREATED        SIZE
catalog-service   0.0.1-SNAPSHOT   sha256:60b...   60b819e356b0   46 years ago   684MB
```

Now, run the application as a Docker container.\
The `--rm` flag automatically removes the container when it stops, and `-p 9001:9001` maps the host port to the container port.

```console
→ docker run --rm --name catalog-service -p 9001:9001 catalog-service:0.0.1-SNAPSHOT 
Calculating JVM memory based on 11102852K available memory
...
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.0)

...
2026-07-11T18:02:14.017Z  INFO 1 --- [catalog-service] [           main] c.p.c.CatalogServiceApplication          : Started CatalogServiceApplication in 0.841 seconds (process running for 1.028)
```

Finally, open a new terminal window and send an HTTP request to verify that the application is running successfully.

```console
→ curl localhost:9001
Welcome to the book catalog!
```

## Deploying to a Local Kubernetes Cluster

### Loading the Image into Minikube

Next, let's deploy the application to a local Kubernetes cluster using Minikube.\
Make sure you have Minikube and `kubectl` installed and that your Minikube cluster is running.

```console
→ minikube image load catalog-service:0.0.1-SNAPSHOT
```

### Creating a Deployment

Create a Kubernetes deployment using the loaded image.

```console
→ kubectl create deployment catalog-service --image=catalog-service:0.0.1-SNAPSHOT
deployment.apps/catalog-service created
```

Finally, verify that the deployment was created successfully and is running.

```console
→ kubectl get deployment
NAME              READY   UP-TO-DATE   AVAILABLE   AGE
catalog-service   1/1     1            1           60s
```

You can also check the status of the Pod created by the deployment.

```console
→ kubectl get pod
NAME                               READY   STATUS    RESTARTS   AGE
catalog-service-86c5b54c8b-s4fws   1/1     Running   0          2m22s
```

### Exposing the Application

By default, applications running in Kubernetes are not accessible.\
Let's expose our Catalog Service inside the cluster using a Kubernetes Service resource by running the following command:

```console
→ kubectl expose deployment catalog-service --name=catalog-service --port=9001
service/catalog-service exposed
```

Verify that the Service was created successfully.

```console
→ kubectl get service catalog-service
NAME              TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
catalog-service   ClusterIP   10.105.103.203   <none>        9001/TCP   100s
```

### Port Forwarding and Testing

Run the following command to forward traffic from a local port on your machine (e.g., 8000) to the port exposed by the Service inside the cluster (9001).\
Keep this command running (don't cancel it with <kbd>CTRL</kbd>+<kbd>C</kbd>) as long as you need port forwarding to access the application.

```console
→ kubectl port-forward service/catalog-service 8000:9001
Forwarding from 127.0.0.1:8000 -> 9001
Forwarding from [::1]:8000 -> 9001
```

Open a new terminal window and send a request to the forwarded port to test the application.

```console
→ curl localhost:8000
Welcome to the book catalog!
```

You should see the port forwarding session log like this (appended after `Forwarding from [::1]:8000 -> 9001`):

```console
Handling connection for 8000
```
