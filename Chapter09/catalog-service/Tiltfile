app_name = 'catalog-service'
minikube_profile = 'polar'
dev_run_image = 'catalog-service-tilt-run:local'
image_ref_file = '.tilt/catalog-service-image-ref'
container_classes = '/workspace/BOOT-INF/classes'

# A change here replaces the application image and Pod.
image_build_deps = [
  'build.gradle.kts',
  'settings.gradle.kts',
  'gradle',
  'gradlew',
  'Dockerfile.tilt-run',
  'scripts/build-and-load-tilt-image.sh',
]

# The IDE or Gradle compiles source changes into these paths before Tilt syncs them.
compiled_classes =   'build/classes/java/main'
compiled_resources = 'build/resources/main'
live_update_deps = [compiled_classes, compiled_resources]

custom_build(
  ref = app_name,
  command = '''\
APP_IMAGE_NAME="{app_name}" \\
DEV_RUN_IMAGE="{dev_run_image}" \\
IMAGE_REF_FILE="{image_ref_file}" \\
MINIKUBE_PROFILE="{minikube_profile}" \\
sh ./scripts/build-and-load-tilt-image.sh
'''.format(
    app_name = app_name,
    dev_run_image = dev_run_image,
    image_ref_file = image_ref_file,
    minikube_profile = minikube_profile,
  ),
  deps = image_build_deps + live_update_deps,
  outputs_image_ref_to = image_ref_file,
  disable_push = True,
  live_update = [
    fall_back_on(image_build_deps),
    sync(compiled_classes, container_classes),
    sync(compiled_resources, container_classes),
  ],
)

k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])
k8s_resource(app_name, port_forwards=['9001'])
