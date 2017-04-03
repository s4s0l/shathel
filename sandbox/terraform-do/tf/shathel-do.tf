provider "digitalocean" {
  token = "${var.do_token}"
}

resource "digitalocean_ssh_key" "shathel" {
  name       = "${var.shathel_solution_name}-key"
  public_key = "${file(var.key_public)}"
}

resource "digitalocean_tag" "shathel_tag" {
  name = "shathel"
}

resource "digitalocean_tag" "shathel_solution" {
  name = "shathel-${var.shathel_solution_name}"
}

resource "digitalocean_tag" "shathel_worker" {
  name = "shathel-worker"
}

resource "digitalocean_tag" "shathel_manager" {
  name = "shathel-manager"
}

