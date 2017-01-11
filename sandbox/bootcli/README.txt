


./docker-build-tag-publish.sh env1 .

docker-machine use env1-manager-1

./docker-with-repo.sh env1 run -d \
    -v /hosthome:/hosthome \
    -p 2000:8080 \
    --name bootcli \
    _REPO_/bootcli:latest $(./inmachine.sh ./app.groovy)

--mode replicated \
--replicas 3 \


./docker-with-repo.sh env1 service create \
    --name bootcli \
    --mode global \
    --publish 2000:8080 \
    --mount type=bind,src=/hosthome,dst=/hosthome \
    _REPO_/bootcli:latest $(./inmachine.sh ./app.groovy)


