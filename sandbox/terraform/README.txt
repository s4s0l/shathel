Display i one step:

terraform graph |  dot -Tpng | display

sudo apt-get update
sudo apt-get install socat
sudo socat tcp-l:80,reuseaddr,fork exec:/bin/login,pty,setsid,setpgid,stderr,ctty


Jak zainstalować to machine na generic driwerze
docker-machine -s $(pwd)/private create --driver generic --generic-ip-address=34.205.22.16 --generic-ssh-key ./private/id_rsa --generic-ssh-user=ubuntu shttmpl-manager-0

Binarki do znalezienia:

terraform-inventory:

https://github.com/adammck/terraform-inventory/releases/download/v0.7-pre/terraform-inventory_v0.7-pre_linux_amd64.zip

terraform

https://releases.hashicorp.com/terraform/0.9.2/terraform_0.9.2_linux_amd64.zip

packer

https://releases.hashicorp.com/packer/0.12.3/packer_0.12.3_linux_amd64.zip