package org.s4s0l.shathel.commons.scripts.terraform

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class TerraformStateToAnsibleInventoryTest extends Specification {

    private File getTargetDir() {
        new File("./build/${getClass().getSimpleName()}")
    }

    def "should read digital ocean terraform state" (){
        given:
        File inFile = new File("src/test/resources/do-terraform.state")
        File outFile = new File(getTargetDir(), "do.ini")
        Map<String,String> params = [
                SHATHEL_TERRAFORM_MANAGER_OBJECTS:'digitalocean_droplet.shathel_worker',
                SHATHEL_TERRAFORM_WORKER_OBJECTS:'digitalocean_droplet.shathel_manager',
                SHATHEL_TERRAFORM_PUBLIC_IP_ATTRIBUTE:'ipv4_address',
                SHATHEL_TERRAFORM_PRIVATE_IP_ATTRIBUTE:'ipv4_address_private',
                SHATHEL_TERRAFORM_NAME_ATTRIBUTE:'name',
        ]
    }
}
