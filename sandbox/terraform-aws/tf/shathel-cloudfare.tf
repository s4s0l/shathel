provider "cloudflare" {
  email = "${var.cloudflare_email}"
  token = "${var.cloudflare_token}"
}


resource "cloudflare_record" "shathel" {
  count = "${aws_eip.shathel_manager_ip.count}"
  domain = "${var.cloudflare_domain}"
  name = "${var.cloudflare_subdomain}"
  type = "A"
  value = "${element(aws_eip.shathel_manager_ip.*.public_ip, count.index)}"
  proxied = false
}
