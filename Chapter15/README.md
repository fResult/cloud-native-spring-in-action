# Chapter 15 – Continuous Delivery and GitOps

This chapter brings the Polar Bookshop system to production.\
After applying cloud-native patterns, principles, and best practices throughout the previous chapters, you will complete the delivery workflow needed to release Spring Boot applications on Kubernetes.

You will define uniquely identifiable release candidates and implement the acceptance stage of a deployment pipeline with GitHub Actions.\
The chapter then configures Spring Boot applications for production with Kustomize, including resource limits and production-specific Kubernetes configuration.

Finally, you will complete the pipeline with a production stage and implement continuous deployment through GitOps.\
Using Argo CD, the cluster continuously pulls the desired state from Git and reconciles it whenever the deployed state diverges.

Original source code: [Thomas Vitale's Chapter 15 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter15/15-end).

---

All commands start from `Chapter15/`

Makesure that you are in `Chapter15` directory

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter15
```
