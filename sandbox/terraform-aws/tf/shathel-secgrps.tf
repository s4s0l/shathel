
resource "aws_security_group" "shathel_common" {
  name = "shathel_common"
  vpc_id = "${aws_vpc.shathel.id}"
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
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
    Name = "${var.solution_name}-common"
  }
}


resource "aws_security_group" "shathel_www" {
  name = "shathel_www"
  vpc_id = "${aws_vpc.shathel.id}"
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}-www"
  }
}

resource "aws_security_group" "shathel_internal" {
  name = "shathel_internal"
  vpc_id = "${aws_vpc.shathel.id}"
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    self = true
  }
  ingress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    self = true
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}-internal"
  }
}

resource "aws_security_group" "shathel_docker" {
  name = "shathel_docker"
  vpc_id = "${aws_vpc.shathel.id}"
  ingress {
    from_port = 2376
    to_port   = 2376
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}-docker"
  }
}

resource "aws_security_group" "shathel_swarm" {
  name = "shathel_swarm"
  vpc_id = "${aws_vpc.shathel.id}"
  ingress {
    from_port = 2375
    to_port   = 2375
    protocol  = "tcp"
    self = true
  }
  ingress {
    from_port = 2376
    to_port   = 2376
    protocol  = "tcp"
    self = true
  }
  ingress {
    from_port = 2377
    to_port   = 2377
    protocol  = "tcp"
    self = true
  }
  ingress {
    from_port = 7946
    to_port   = 7946
    protocol  = "tcp"
    self = true
  }
  ingress {
    from_port = 7946
    to_port   = 7946
    protocol  = "udp"
    self = true
  }
  ingress {
    from_port = 4789
    to_port   = 4789
    protocol  = "tcp"
    self = true
  }
  ingress {
    from_port = 4789
    to_port   = 4789
    protocol  = "udp"
    self = true
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.solution_name}"
    Name = "${var.solution_name}-swarm"
  }
}