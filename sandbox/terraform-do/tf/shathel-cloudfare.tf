provider "cloudflare" {
  email = "${var.cloudflare_email}"
  token = "${var.cloudflare_token}"
}


resource "cloudflare_record" "shathel" {
  count = "${digitalocean_droplet.shathel_manager.count}"
  domain = "${var.cloudflare_domain}"
  name = "${var.cloudflare_subdomain}"
  type = "A"
  value = "${element(digitalocean_droplet.shathel_manager.*.ipv4_address, count.index)}"
  proxied = false
}
