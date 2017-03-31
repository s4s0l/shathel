variable "aws_access_key" {
}
variable "aws_secret_key" {
}
variable "cloudflare_email" {
}
variable "cloudflare_token" {
}
variable "cloudflare_domain" {
}
variable "cloudflare_subdomain" {
}
variable "key_public" {
  default = "./private/id_rsa.pub"
}
variable "key_private" {
  default = "./private/id_rsa"
}
variable "region" {
  default = "us-east-1"
}
variable "vpc_cidr" {
  default = "10.0.0.0/16"
}
variable "avzones" {
  type = "list"
  default = [
    "us-east-1a" ,
    "us-east-1b" ,
    "us-east-1c" ,
    "us-east-1d" ,
    "us-east-1e"
  ]
}
variable "avzone_cidr" {
  type = "map"
  default = {
    "us-east-1a" = "10.0.0.0/20",
    "us-east-1b" = "10.0.16.0/20",
    "us-east-1c" = "10.0.32.0/20",
    "us-east-1d" = "10.0.48.0/20",
    "us-east-1e" = "10.0.64.0/20"
  }
}
variable "solution_name" {
}
variable manager_count {
  default = "2"
}

variable worker_count {
  default = "1"
}

variable "ami" {
  default = "ami-b374d5a5"
}
variable "ami_user" {
  default = "ubuntu"
}
variable "instance_type" {
  default = "t2.micro"
}