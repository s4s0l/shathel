variable "do_token" {

}
variable "cloudflare_email" {
}
variable "cloudflare_token" {
}
variable "cloudflare_domain" {
}
variable "cloudflare_subdomain" {
}
variable "shathel_solution_name" {
}
variable "key_public" {
  default = "./private/id_rsa.pub"
}
variable "key_private" {
  default = "./private/id_rsa"
}
variable shathel_manager_count {
  default = "2"
}
variable shathel_worker_count {
  default = "1"
}

variable "do_image" {
  default = "ubuntu-16-04-x64"
}
variable "do_region" {
  default = "nyc1"
}
variable "do_size" {
  default = "1gb"
}
variable "do_user" {
  default = "root"
}
variable "do_backups" {
  default = "false"
}

