# Chapter 14 – Configuration and Secrets Management

This chapter prepares the Polar Bookshop system for production by focusing on its other essential release component: configuration.\
Alongside executable JAR files or container images, cloud-native applications need externalized configuration that can vary safely between environments.

You will secure Spring Cloud Config Server with Spring Security, refresh configuration at runtime with Spring Cloud Bus, and manage secrets with Spring Cloud Config.\
The chapter then introduces Kubernetes-native configuration through ConfigMaps and Secrets, including Spring Cloud Kubernetes support for runtime refreshes.

Finally, you will use Kustomize to manage Kubernetes manifests and environment-specific configuration.\
Using bases, overlays, and patches, you will customize environment variables, ConfigMaps, container image versions, and replica counts without duplicating deployment definitions.

Original source code: [Thomas Vitale's Chapter 14 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter14/14-end).

---

All commands start from `Chapter14/`

Makesure that you are in `Chapter14` directory

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter14
```
