#!/bin/sh

# run it like that: ./build.sh

export KUBECONFIG=../create-cluster/k3s.yaml

#export APPLICATION_NAME=$(basename $(git rev-parse --show-toplevel))
#export DOCKER_USER="registry.dev.test:5000"
#export IMAGE_NAME="${APPLICATION_NAME}-img"
#export TAG=$(git rev-parse --short HEAD)
#export IMAGE="${DOCKER_USER}/${IMAGE_NAME}:${TAG}"

export CLUSTER_IP=$(multipass info basestar | grep IPv4 | awk '{print $2}')

export NAMESPACE="training"

export CONTAINER_PORT=8080
export EXPOSED_PORT=80

export BRANCH=$(git symbolic-ref --short HEAD)
export HOST="${APPLICATION_NAME}.${BRANCH}.${CLUSTER_IP}.nip.io"

envsubst < ./deploy.template.yaml > ./kube/deploy.${TAG}.yaml

kubectl apply -f ./kube/deploy.${TAG}.yaml -n ${NAMESPACE}
echo "🌍 http://${HOST}"
echo "🤜 http://${HOST}/api/knock-knock"
