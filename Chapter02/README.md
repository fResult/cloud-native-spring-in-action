# Chapter 02

This chapter focuses on containerizing the application (creating a Docker Image).\
We use **Spring Boot** with **Cloud Native Buildpacks**, which allows us to build an image without writing a `Dockerfile`.

## Containerizing the Application Locally

### Building the Docker Image

First, create a Docker image from the source code using the following command:

### Running and Testing the Container

After the build finishes, you can verify that the Docker image was created successfully by listing your Docker images.

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter02
→ cd catalog-service && ./gradlew bootBuildImage
> Task :bootBuildImage

...

Successfully built image 'docker.io/library/catalog-service:0.0.1-SNAPSHOT'

BUILD SUCCESSFUL
```

After the build finishes, you can verify that the Docker image was created successfully by listing your Docker images.

```console
→ docker images --digest catalog-service:0.0.1-SNAPSHOT
REPOSITORY        TAG              DIGEST          IMAGE ID       CREATED        SIZE
catalog-service   0.0.1-SNAPSHOT   sha256:60b...   60b819e356b0   46 years ago   684MB
```

Now, run the application as a Docker container.\
The `--rm` flag automatically removes the container when it stops, and `-p 8080:8080` maps the host port to the container port.

```console
→ docker run --rm --name catalog-service -p 8080:8080 catalog-service:0.0.1-SNAPSHOT 
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
→ curl localhost:8080
Welcome to the book catalog!
```

## Deploying to a Local Kubernetes Cluster

### Loading the Image into Minikube

Next, let's deploy the application to a local Kubernetes cluster using Minikube.\
First, load the Docker image into Minikube's Docker daemon so that Kubernetes can access it.

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
Let's expose our Catalog Service to the cluster through Service resource by running the following command:

```console
→ kubectl expose deployment catalog-service --name=catalog-service --port=8080
service/catalog-service exposed
```

Verify that the Service was created successfully.

```console
→ kubectl get service catalog-service
NAME              TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
catalog-service   ClusterIP   10.105.103.203   <none>        8080/TCP   100s
```

### Port Forwarding and Testing

Run the following command to forward the traffic from the local port on a local machine (e.g., 8000) to the port exposed by the Service inside the cluster (8080).\
Let's keep it running (don't cancel it via <kbd>CTRL</kbd>+<kbd>C</kbd> when we need port forwarding to access the application)

```console
→ kubectl port-forward service/catalog-service 8000:8080
Forwarding from 127.0.0.1:8000 -> 8080
Forwarding from [::1]:8000 -> 8080
```

Open a new terminal window and send a request to the forwarded port to test the application.

```console
→ curl localhost:8000
Welcome to the book catalog!
```

And we will see the port forwarding session like this (appended after `Forwarding from [::1]:8000 -> 8080`)

```console
Handling connection for 8000
```
