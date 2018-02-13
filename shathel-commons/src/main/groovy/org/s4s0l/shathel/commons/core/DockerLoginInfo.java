package org.s4s0l.shathel.commons.core;

import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class DockerLoginInfo {
    private final String user;
    private final String pass;
    private final Optional<String> registryAddress;

    public DockerLoginInfo(String user, String pass, Optional<String> registryAddress) {
        this.user = user;
        this.pass = pass;
        this.registryAddress = registryAddress;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public Optional<String> getRegistryAddress() {
        return registryAddress;
    }
}
