#!/usr/bin/env sh

set -eu

app_image_name="${APP_IMAGE_NAME:?APP_IMAGE_NAME is required}"
dev_run_image="${DEV_RUN_IMAGE:?DEV_RUN_IMAGE is required}"
image_ref_file="${IMAGE_REF_FILE:?IMAGE_REF_FILE is required}"
minikube_profile="${MINIKUBE_PROFILE:?MINIKUBE_PROFILE is required}"

case "$(uname -m)" in
  arm64|aarch64) image_platform="linux/arm64" ;;
  x86_64|amd64) image_platform="linux/amd64" ;;
  *)
    echo "Unsupported architecture: $(uname -m)" >&2
    exit 1
    ;;
esac

# Tilt Live Update transfers files with tar and removes stale files with rm.
# The Paketo tiny run image omits both tools, so this derived run image adds them.
# bootBuildImage uses this run image as the base of the final application image.
# Docker caches these unchanged layers on later builds.
docker build \
  --platform "$image_platform" \
  --file Dockerfile.tilt-run \
  --tag "$dev_run_image" \
  .

# outputs_image_ref_to requires this script to choose and report the deployed tag.
# Combining seconds with the shell process ID avoids collisions without uuidgen.
image_ref="${app_image_name}:tilt-$(date +%s)-$$"
./gradlew -PtiltDev bootBuildImage --imageName "$image_ref"
minikube image load "$image_ref" --profile "$minikube_profile"

mkdir -p "$(dirname "$image_ref_file")"
printf '%s' "$image_ref" > "$image_ref_file"
