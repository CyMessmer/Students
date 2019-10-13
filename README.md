# Students API

## start local server
`./gradlew run` or `docker-compose up`

## Kubernetes (minikube with local container)
- `minikube start`
- `eval $(minikube docker-env)`
- `docker build -t students .`
- `kubectl create -f students-deployment.yaml` 
- `kubectl create -f students-service.yaml`
- Verify with `kubectl get pods` or check the dashboard `minikube dashboard`
- Get server url with `minikube service students-service --url` 

## swagger docks
local swagger endpoint: http://localhost:8080/api/docs/swagger.json

Local copy of swagger docs can be found in /Students/src/main/resources/openai/students.(yaml|json)
 
For a pretty UI head to http://editor.swagger.io/ and paste in either the json or yaml.