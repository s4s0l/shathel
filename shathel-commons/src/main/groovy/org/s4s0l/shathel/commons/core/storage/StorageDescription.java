package org.s4s0l.shathel.commons.core.storage;

import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class StorageDescription {
    private final String type;
    private final Map<String, Object> storageParameters;

    public StorageDescription(String type, Map<String, Object> storageParameters) {
        this.type = type;
        this.storageParameters = storageParameters;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getStorageParameters() {
        return storageParameters;
    }
}
