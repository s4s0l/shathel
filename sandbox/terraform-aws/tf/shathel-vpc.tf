resource "aws_vpc" "shathel" {
  cidr_block = "${var.vpc_cidr}"
  enable_dns_hostnames = true
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}"
  }
}
//default group allows all
resource "aws_default_security_group" "shathel" {
  vpc_id = "${aws_vpc.shathel.id}"

  ingress {
    protocol = -1
    self = true
    from_port = 0
    to_port = 0
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}"
  }
}

resource "aws_subnet" "shathel" {
  vpc_id = "${aws_vpc.shathel.id}"
  count = "${length(var.avzones)}"
  availability_zone = "${element(var.avzones,count.index )}"
  cidr_block = "${var.avzone_cidr[element(var.avzones,count.index )]}"
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}-sn-${element(var.avzones,count.index )}"
  }
}

resource "aws_internet_gateway" "shathel" {
  vpc_id = "${aws_vpc.shathel.id}"
  tags {
    Shathel = "true"
    Name = "${var.solution_name}"
    ShathelSolution = "${var.solution_name}"
  }
}

resource "aws_default_route_table" "shathel" {
  default_route_table_id = "${aws_vpc.shathel.default_route_table_id}"
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.shathel.id}"
  }

  tags {
    Shathel = "true"
    Name = "${var.solution_name}"
    ShathelSolution = "${var.solution_name}"
  }
}



