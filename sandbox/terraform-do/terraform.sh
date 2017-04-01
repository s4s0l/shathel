#!/usr/bin/env bash
terraform $@ -state=./state/terraform.tfstate -var-file=./private/private.tfvars ./tf