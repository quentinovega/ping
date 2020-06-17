#!/bin/sh

# run it like that: . ./build.sh
# These execute the script under the current shell instead of loading another one
# https://stackoverflow.com/questions/496702/can-a-shell-script-set-environment-variables-of-the-calling-shell

git add .
git commit -m "🎉 ping service updated"

export APPLICATION_NAME=$(basename $(git rev-parse --show-toplevel))
export DOCKER_USER="registry.dev.test:5000"
export IMAGE_NAME="${APPLICATION_NAME}-img"
export TAG=$(git rev-parse --short HEAD)
export IMAGE="${DOCKER_USER}/${IMAGE_NAME}:${TAG}"

# build jar
./mvnw clean package

docker build -t ${IMAGE_NAME} .
docker tag ${IMAGE_NAME} ${IMAGE}
docker push ${IMAGE}

curl http://${DOCKER_USER}/v2/_catalog

