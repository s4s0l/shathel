import org.s4s0l.shathel.commons.docker.OpenSslWrapper
def path = "/home/sasol/Projects/shathel-swarm/sandbox/secrets"


new OpenSslWrapper().generateKeyPair("localhost", [], ["metadata", "localhost"] , "$path/server.key","$path/server.crt")


cat server.key server.crt > server.pem
cat client.key client.crt > client.pem
to trwa wieki
openssl dhparam -out dhparams.pem 4096
cat dhparams.pem >> server.pem


socat -d -d openssl-listen:3333,fork,reuseaddr,cert=$(pwd)/server.pem,cafile=$(pwd)/client.crt unix-connect:/var/run/docker.sock
curl  --cacert server.crt --cert-type pem --cert client.pem https://localhost:3333/v1.26/_ping
curl  --cacert /run/secrets/server_crt --cert-type pem --cert /run/secrets/client_pem https://dockerexposer:3333/v1.26/_ping
curl  --cacert /run/secrets/server_crt --cert-type pem --cert /run/secrets/client_pem https://dockerexposer:3333/v1.26/images/e4c38760e0bc/json

./docker-gen --config ./docker-gen.ini -tlscacert server.crt -tlscert client.crt -tlskey client.key -watch -endpoint tcp://localhost:3333
docker-gen -tlscacert /run/secrets/server_crt -tlscert /run/secrets/client_crt -tlskey /run/secrets/client_key -swarm -watch -endpoint dockerexposer:3333 /temp/nginx.tmpl

-swarm -watch -endpoint 111.111.111.99:3333 -tlscacert /home/sasol/Projects/shathel-swarm/sandbox/docker-gen/tmp/server.crt -tlscert /home/sasol/Projects/shathel-swarm/sandbox/docker-gen/tmp/client.crt -tlskey /home/sasol/Projects/shathel-swarm/sandbox/docker-gen/tmp/client.key /home/sasol/Projects/shathel-swarm/sandbox/docker-gen/nginx.tmpl



-tlscacert /run/secrets/server_crt -tlscert /run/secrets/client_crt -tlskey /run/secrets/client_key -swarm -watch -endpoint dockerexposer:3333 /temp/nginx.tmpl /temp/ngnix.conf