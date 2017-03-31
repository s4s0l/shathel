provider "aws" {
  access_key = "${var.aws_access_key}"
  secret_key = "${var.aws_secret_key}"
  region = "${var.region}"
}


resource "aws_key_pair" "shathel" {
  key_name_prefix = "${var.solution_name}"
  public_key = "${file(var.key_public)}"
}