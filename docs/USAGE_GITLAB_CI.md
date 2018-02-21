```yml

verify:
  stage: verify
  image:
    name: sasol/shathel:$SHATHEL_VERSION
    entrypoint:
     - /bin/bash
     - -c
  variables:
    SHATHEL_DIR: /builds/ravenetics-ltd/toktme/toktme-env/solution
    #not needed by shathel but shows how to get host docker in general
    DOCKER_HOST: unix:///var/run/docker.sock.host
  script:
     - env | grep DOCKER
     - docker info
     - pwd
     - ls -al /var/run/
     - >
       shathel-deployer "parameters list;
         parameters list;"
     - docker info

verify-dind:
  stage: verify
  image:
    name: sasol/dind4j:2.5f
  variables:
    DOCKER_HOST: unix:///var/run/docker.sock.host
  script:
     - pwd
     - ls -al /var/run/
     - docker info          
     - >
       docker run -v /builds/ravenetics-ltd/toktme/toktme-env/solution:/solution --rm sasol/shathel:$SHATHEL_VERSION 
       "\"
       parameters list;
       parameters list;
       \""

```