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


testować tak:
for i in {1..100}; do curl http://20.20.20.103:3001; echo "" ; sleep 1;  done

========================================RANCHER:
docker-


docker-machine create -d virtualbox r-one
docker-machine create -d virtualbox r-two
docker-machine use one

docker run -d --restart=always --name=rancher-server -p 8080:8080 rancher/server

aby mi to działąło to musiałem zmienić że host ma labelkę 12, oraz że tylko jeden manager jest potrzebny


./rancher-cluster-create.sh test 3

rancher-cluster-docker.sh test build -t cli ./boot-cli/

curl rancher-metadata < pobranie metadanych serwisu

===============================================

w EXtra jest przykład proxy do repo i repo samego w sobie