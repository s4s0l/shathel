docker-machine use test-manager-1

docker build -t cli ./boot-cli/


docker-machine use test-manager-1
docker build -t cli ./boot-cli/
docker run -d --name localonmanager1 -v /hosthome:/hosthome -p 2001:8080 cli $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy

docker-machine use test-worker-1
docker build -t cli ./boot-cli/
docker run -d --name localonworker1 -v /hosthome:/hosthome -p 2002:8080 cli $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy

docker-machine use test-worker-2
docker build -t cli ./boot-cli/
docker run -d --name localonworker2 -v /hosthome:/hosthome -p 2003:8080 cli $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy

docker-machine use test-manager-1



docker service create \
    --name globalnonetwork \
    --publish 3000:8080 \
    --mode global \
    --mount type=bind,src=/hosthome,dst=/hosthome \
    cli \
    $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy


docker network create --driver overlay globalnetwork


docker service create \
    --name globalwithnetwork \
    --publish 3001:8080 \
    --network globalnetwork \
    --mode global \
    --mount type=bind,src=/hosthome,dst=/hosthome \
    cli \
    $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy

docker service create \
    --name replicatedwithnetwork \
    --publish 3002:8080 \
    --network globalnetwork \
    --replicas 3 \
    --mode replicated \
    --mount type=bind,src=/hosthome,dst=/hosthome \
    cli \
    $(TMP=$(pwd) && echo ${TMP/\/home\//\/hosthome/})/app.groovy




/swarm-cluster-docker.sh test build -t cli ./boot-cli/


