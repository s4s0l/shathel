# Configure the Docker provider
provider "docker" {
//  host = ""
}

variable "container_name" {

}

resource "docker_container" "foo" {
  image = "tutum/hello-world"
  name  = "${var.container_name}"
}

output some_output {
  value = "My value"
}