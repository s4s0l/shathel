package org.s4s0l.shathel.commons.scripts.terraform

import groovy.json.JsonSlurper

/**
 * @author Marcin Wielgus
 */
class TerraformStateToAnsibleInventory {

    void generateAnsibleFileFromTerraform(File ansibleInvbentory, File terraformState, Map<String, String> stringStringMap) {
        def Object parsed = new JsonSlurper().parse(terraformState)
        String managerObjectKeyPrefix = stringStringMap['SHATHEL_TERRAFORM_MANAGER_OBJECTS']
        String workerObjectKeyPrefix = stringStringMap['SHATHEL_TERRAFORM_WORKER_OBJECTS']
        String public_ip_attr = stringStringMap['SHATHEL_TERRAFORM_PUBLIC_IP_ATTRIBUTE']
        String private_ip_attr = stringStringMap['SHATHEL_TERRAFORM_PRIVATE_IP_ATTRIBUTE']
        String name_attr = stringStringMap['SHATHEL_TERRAFORM_NAME_ATTRIBUTE']








    }
}
