# Chapter 13 – Observability and Monitoring

This chapter makes the Polar Bookshop system observable before it goes to production.\
After building secure, scalable, and resilient applications in the previous chapters, you now need enough operational visibility to understand what the system is doing—especially when something goes wrong.

You will learn the difference between monitoring known failure states and observability, which lets you infer an application's internal state from its outputs and investigate unknown problems.\
Spring Boot Actuator exposes production-ready information, including logs, health probes, metrics, traces, migration details, and application metadata.

Using the Grafana observability stack, you will collect and explore logs with Fluent Bit and Loki, monitor metrics with Prometheus, and trace distributed requests with OpenTelemetry and Tempo.\
The chapter also shows how Kubernetes uses liveness and readiness probes to manage application availability, and how Actuator supports operational diagnostics such as heap dumps.

Original source code: [Thomas Vitale's Chapter 13 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter13/13-end).

---

All commands start from `Chapter13/`

Makesure that you are in `Chapter13` directory

```console
→ pwd
/path/to/cloud-native-spring-in-action/Chapter13
```
