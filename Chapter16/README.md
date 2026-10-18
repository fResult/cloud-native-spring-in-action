# Chapter 16 – Serverless, GraalVM, and Knative

This final chapter expands the Polar Bookshop journey beyond a production deployment on Kubernetes.\
You will explore additional cloud-native tools for building applications that start faster, use fewer resources, and scale efficiently with demand.

You will use GraalVM and Spring Native to compile Spring Boot applications into native executables.\
Compared with JVM applications, native images provide faster startup times, lower memory consumption, and instant peak performance—characteristics that make them especially useful for serverless workloads.

The chapter then introduces serverless applications with Spring Cloud Function and deploys them with Knative on Kubernetes.\
Serverless platforms handle operational concerns such as autoscaling, including scaling applications to zero when there is no work to process, so developers can focus on business logic.\
Knative also provides higher-level developer abstractions for deploying both standard containerized and event-driven workloads on Kubernetes.

Original source code: [Thomas Vitale's Chapter 16 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter16/16-end).

---

All commands start from `Chapter16/`

Makesure that you are in `Chapter16` directory

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter16
```
