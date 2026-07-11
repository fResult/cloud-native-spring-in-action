# Chapter 02

This chapter focuses on containerizing the application (creating a Docker Image). We use **Spring Boot** with **Cloud Native Buildpacks**, which allows us to build an image without writing a `Dockerfile`. The main steps are:

1. **Build the Image:** Create a Docker image from the source code using `./gradlew bootBuildImage`.
2. **Verify the Image:** Check the image details and size using `docker images`.
3. **Run the Container:** Start the application as a Docker container using `docker run`.

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
