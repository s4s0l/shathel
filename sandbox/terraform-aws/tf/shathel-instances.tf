resource "aws_instance" "shathel_manager" {
  count = "${var.shathel_manager_count}"
  ami = "${var.ami}"
  instance_type = "${var.instance_type}"
  key_name = "${aws_key_pair.shathel.key_name}"
  availability_zone = "${element(var.avzones,count.index)}"
  subnet_id = "${element(aws_subnet.shathel.*.id, count.index)}"
  vpc_security_group_ids = [
    "${aws_security_group.shathel_common.id}",
    "${aws_security_group.shathel_internal.id}",
    "${aws_security_group.shathel_www.id}",
    "${aws_security_group.shathel_docker.id}",
    "${aws_security_group.shathel_swarm.id}",
  ]
//  Needed because otherwise provisioner will not be able to connect to instance
//  Will be overriden by eip later on
  associate_public_ip_address = true
  connection {
    user = "${var.ami_user}"
    private_key = "${file(var.key_private)}"
  }
  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install python -y",
    ]
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.shathel_solution_name}"
    Name = "${var.shathel_solution_name}-manager-${count.index}"
    Role = "manager"
  }
}

resource "aws_eip" "shathel_manager_ip" {
  count = "${aws_instance.shathel_manager.count}"
  instance = "${element(aws_instance.shathel_manager.*.id, count.index)}"
  depends_on = [
    "aws_instance.shathel_manager"]
}


resource "aws_instance" "shathel_worker" {
  count = "${var.shathel_worker_count}"
  ami = "${var.ami}"
  instance_type = "${var.instance_type}"
  key_name = "${aws_key_pair.shathel.key_name}"
  availability_zone = "${element(var.avzones,-count.index  )}"
  subnet_id = "${element(aws_subnet.shathel.*.id, -count.index )}"
  vpc_security_group_ids = [
    "${aws_security_group.shathel_common.id}",
    "${aws_security_group.shathel_internal.id}",
    "${aws_security_group.shathel_www.id}",
    "${aws_security_group.shathel_docker.id}",
    "${aws_security_group.shathel_swarm.id}",
  ]
  //  Needed because otherwise provisioner will not be able to connect to instance
  //  Will be overriden by eip later on
  associate_public_ip_address = true
  connection {
    user = "${var.ami_user}"
    private_key = "${file(var.key_private)}"
  }
  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install python -y",
    ]
  }
  tags {
    Shathel = "true"
    ShathelSolution = "${var.shathel_solution_name}"
    Name = "${var.shathel_solution_name}-worker-${count.index}"
    Role = "worker"
  }
}

resource "aws_eip" "shathel_worker_ip" {
  count = "${aws_instance.shathel_worker.count}"
  instance = "${element(aws_instance.shathel_worker.*.id, count.index)}"
  depends_on = [
    "aws_instance.shathel_worker"]

}

