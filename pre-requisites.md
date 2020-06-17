


## Build

### Push OpenJDK to our registry

```bash
registry_domain="registry.dev.test"
image_name_to_pull="azul/zulu-openjdk-alpine:latest"

docker pull ${image_name_to_pull}
docker tag ${image_name_to_pull} ${registry_domain}:5000/${image_name_to_pull}
docker push ${registry_domain}:5000/${image_name_to_pull}

curl http://${registry_domain}:5000/v2/_catalog

```

### With Docker

> WIP

### With Kaniko

> interesting with GitLab CI

```bash
registry_domain="registry.dev.test"
image_name_to_pull="gcr.io/kaniko-project/executor:debug"

docker pull ${image_name_to_pull}
docker tag ${image_name_to_pull} ${registry_domain}:5000/${image_name_to_pull}
docker push ${registry_domain}:5000/${image_name_to_pull}

curl http://${registry_domain}:5000/v2/_catalog
```

## Deploy

https://k33g.gitlab.io/articles/2020-02-23-K3S-04-BETTER-DEPLOY.html

environment variable inside yaml file


## Connect to Redis from the host

- Port forwarding
```bash
export KUBECONFIG=$PWD/k3s.yaml

kubectl port-forward --namespace database svc/redis-master 6379:6379 &
```

Tools: Medis (OSX)
