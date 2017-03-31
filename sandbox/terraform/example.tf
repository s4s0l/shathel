provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_instance" "example" {
  ami = "ami-b374d5a5"
  instance_type = "t2.micro"
  key_name = "xxx"

  provisioner "local-exec" {
    command = "echo ${aws_instance.example.public_ip} > ip_address.txt"
  }
}

resource "aws_eip" "ip" {
  instance = "${aws_instance.example.id}"
  depends_on = [
    "aws_instance.example"]
}

output "ip" {
  value = "${aws_eip.ip.public_ip}"
}

//
//module "consul" {
//  source = "github.com/hashicorp/consul/terraform/aws"
//
//  key_name = "AWS SSH KEY NAME"
//  key_path = "PATH TO ABOVE PRIVATE KEY"
//  region = "us-east-1"
//  servers = "3"
//}
