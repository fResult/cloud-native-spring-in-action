# Chapter 11 - Security: Authentication and SPA

This chapter adds authentication to the Polar Bookshop system.\
An access-control system first identifies a user, then authenticates the claimed identity, and finally authorizes the actions that user can perform.

You will manage users and roles with Keycloak, an identity and access management solution.\
You will also use OpenID Connect and JSON Web Tokens (JWTs) to authenticate users, exchange identity information, and register applications with Keycloak.

With Spring Security, you will protect the application, inspect the authenticated user context, and configure logout.\
The chapter adds an Angular single-page application (SPA), secures its authentication flow, protects it against cross-site request forgery (CSRF), and tests the Spring Security and OpenID Connect integration.

Original source code: [Thomas Vitale's Chapter 11 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter11/11-end).
