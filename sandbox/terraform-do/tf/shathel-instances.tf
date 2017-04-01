resource "digitalocean_droplet" "shathel_manager" {
  count = "${var.manager_count}"
  image = "${var.do_image}"
  region = "${var.do_region}"
  name = "${var.solution_name}-manager-${count.index}"
  size = "${var.do_size}"
  backups = "${var.do_backups}"
  ssh_keys = [
    "${digitalocean_ssh_key.shathel.id}"]
  private_networking = true

  //TODO check if user data script gives any advantage over provisioning below
  connection {
    user = "${var.do_user}"
    private_key = "${file(var.key_private)}"
    agent = false
  }
  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install python -y",
    ]
  }
  tags = [
    "${digitalocean_tag.shathel_manager.id}",
    "${digitalocean_tag.shathel_solution.id}",
    "${digitalocean_tag.shathel_tag.id}"
  ]
}

resource "digitalocean_droplet" "shathel_worker" {
  count = "${var.worker_count}"
  image = "${var.do_image}"
  region = "${var.do_region}"
  name = "${var.solution_name}-worker-${count.index}"
  size = "${var.do_size}"
  backups = "${var.do_backups}"
  ssh_keys = [
    "${digitalocean_ssh_key.shathel.id}"]
  private_networking = true
  connection {
    user = "${var.do_user}"
    private_key = "${file(var.key_private)}"
    agent = false
  }
  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install python -y",
    ]
  }
  tags = [
    "${digitalocean_tag.shathel_worker.id}",
    "${digitalocean_tag.shathel_solution.id}",
    "${digitalocean_tag.shathel_tag.id}"
  ]
}





