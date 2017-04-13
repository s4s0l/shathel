package org.s4s0l.shathel.commons.core.environment

/**
 * @author Marcin Wielgus
 */
class ShathelNode {
    private final String nodeName
    private final String publicIp
    private final String privateIp
    private final String role

    ShathelNode(String nodeName, String publicIp, String privateIp, String role) {
        this.nodeName = nodeName
        this.publicIp = publicIp
        this.privateIp = privateIp
        this.role = role
    }

    String getNodeName() {
        return nodeName
    }

    String getPublicIp() {
        return publicIp
    }

    String getPrivateIp() {
        return privateIp
    }

    String getRole() {
        return role
    }
}

