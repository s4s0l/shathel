package org.s4s0l.shathel.commons.docker

import groovy.json.JsonSlurper
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class DockerWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerComposeWrapper.class);

    final ExecWrapper exec = new ExecWrapper(LOGGER, 'docker')

    def Map<String, String> findLabelsOfOneByFilter(String filter) {
        String[] dockerIds = exec.executeForOutput(new File("."), "ps -f $filter -q").split("\\s")
        if (dockerIds.size() > 1) {
            throw new Exception("Multiple containers match filter $filter")
        }
        if (dockerIds.size() == 0 || "" == dockerIds[0]) {
            return [:]
        }
        String inspect = exec.executeForOutput(new File("."), "inspect ${dockerIds[0]}")
        def val = new JsonSlurper().parseText(inspect);
        val[0].Config.Labels
    }
}
