package org.s4s0l.shathel.commons.core.model

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class EnvironmentFileModelTest extends Specification {

    def "Is bale to read file"() {
        given:
        def file = """
version: 1
shathel-env:
  gav: git@github.com/s4s0l/shathel-stacks:vagrant:\$version
  mandatoryEnvs:
      SOME_NEEDED_ENV: Description
  user: ubuntu
  phases:
      image-preparation:
        type: groovy
        inline: "Inline script"
      infrastructure:
        type: vagrant
        name: Vagrantfile
      setup:
        type: vagrant
        name: ./playbook.yml
      swarm:
        type: vagrant
        name: ../common/swarmize.yml
"""
        when:
        def load = EnvironmentFileModel.load(file)

        then:
        load.getGav() == "git@github.com/s4s0l/shathel-stacks:vagrant:\$version"
        load.mandatoryEnvironmentVariables == [SOME_NEEDED_ENV: "Description"]
        load.imageUser == "ubuntu"
        load.imagePreparationScript == [type: "groovy", inline: "Inline script", name: null]
        load.infrastructureScript == [type: "vagrant", inline: null, name: "Vagrantfile"]
        load.setupScript == [type: "vagrant", inline: null, name: "./playbook.yml"]
        load.swarmScript == [type: "vagrant", inline: null, name: "../common/swarmize.yml"]
    }
}
