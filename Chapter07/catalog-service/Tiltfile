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

sys_arch="$(uname -m)"

get_os_arch() {
  case "$1" in
    arm64|aarch64) echo "linux/arm64" ;;
    x86_64|amd64)  echo "linux/amd64" ;;
    *)             echo "Error: Unsupported architecture: $1" >&2; exit 1 ;;
  esac
}

# Build a local Paketo run image that adds tar and rm to the minimal runtime.
# Tilt needs these tools to transfer and remove files during Live Update.
# bootBuildImage then uses this run image as the base of the final application image, so these two builds are directly related rather than duplicate work.
# Docker caches the unchanged tool-copying layers, keeping subsequent builds cheap.
# Removing this step would also leave a fresh workstation without the custom run image required by the Gradle tiltDev configuration.
docker build --platform "$image_platform" --file Dockerfile.tilt-run --tag catalog-service-tilt-run:local .

# outputs_image_ref_to makes this script responsible for choosing and reporting the deployable image reference.
# A timestamp plus the shell process ID avoids collisions without requiring the optional uuidgen utility.
image_ref="catalog-service:tilt-$(date +%s)-$$"
./gradlew -PtiltDev bootBuildImage --imageName "$image_ref"
minikube image load "$image_ref" --profile polar
mkdir -p .tilt
printf '%s' "$image_ref" > .tilt/catalog-service-image-ref
''',
  # Rebuild the image only when its build definition changes. Compiled output is watched for live updates, while src is compiled locally by Gradle/your IDE.
  deps = [
    'build.gradle.kts',
    'settings.gradle.kts',
    'gradle',
    'gradlew',
    'Dockerfile.tilt-run',
    'build/classes/java/main',
    'build/resources/main',
  ],
  # Use the ref loaded above instead of Tilt retagging and pushing it to a registry.
  outputs_image_ref_to = '.tilt/catalog-service-image-ref',
  # The image is already loaded into Minikube, will not push it to Docker Hub.
  disable_push = True,
  live_update = [
    sync('./build/classes/java/main', '/workspace/BOOT-INF/classes'),
    sync('./build/resources/main', '/workspace/BOOT-INF/classes'),
  ]
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('catalog-service', port_forwards=['9001'])
