# Chapter 12 – Security: Authorization and Auditing

This chapter extends the Polar Bookshop security model from authentication to authorization and auditing.\
After Keycloak authenticates a user, Edge Service relays an OAuth2 access token to Catalog Service and Order Service so they can act on that user's behalf.

You will configure Spring Cloud Gateway and Spring Security to protect APIs with OAuth2 in both imperative and reactive applications.\
JWT access tokens carry claims about the authenticated user, such as roles, and Spring Security uses those claims to enforce role-based access control (RBAC) policies.

The chapter also uses Spring Data auditing to record who creates or changes data, and it protects data so that only its owner can access it.\
Finally, you will test authorization, JWT-based security, and service integration with Spring Boot, Spring Security, Testcontainers, and Keycloak.

Original source code: [Thomas Vitale's Chapter 12 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter12/12-end).
