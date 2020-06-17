#!/bin/sh
export KUBECONFIG=../create-cluster/k3s.yaml

APPLICATION_NAME=$(basename $(git rev-parse --show-toplevel))
NAMESPACE="training"
NB_REPLICAS=3

kubectl scale --replicas=${NB_REPLICAS} deploy ${APPLICATION_NAME} -n ${NAMESPACE}
