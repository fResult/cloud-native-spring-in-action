# Unlike the book's setup, this Minikube profile uses containerd and cannot directly see images built in the host Docker daemon.
# Load each build into Minikube instead of pushing it to an image registry.
# See original: https://github.com/ThomasVitale/cloud-native-spring-in-action/blob/main/Chapter07/07-end/catalog-service/Tiltfile

# Build
custom_build(
  # Name of the container image
  ref = 'catalog-service',
  # Build into the host Docker daemon, then load the same immutable image reference directly into Minikube's containerd image store.
  command = '''\
set -eu
image_ref="catalog-service:tilt-$(uuidgen | tr '[:upper:]' '[:lower:]')"
./gradlew bootBuildImage --imageName "$image_ref"
minikube image load "$image_ref" --profile polar
printf '%s' "$image_ref" > /tmp/catalog-service-tilt-image-ref
''',
  # Files to watch that trigger a new docker_build
  deps = ['build.gradle.kts', 'src'],
  # Use the ref loaded above instead of Tilt retagging and pushing it to a registry.
  outputs_image_ref_to = '/tmp/catalog-service-tilt-image-ref',
  # The image is already loaded into Minikube, will not push it to Docker Hub.
  disable_push = True,
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('catalog-service', port_forwards=['9001'])
