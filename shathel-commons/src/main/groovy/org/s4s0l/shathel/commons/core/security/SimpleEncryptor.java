package org.s4s0l.shathel.commons.core.security;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface SimpleEncryptor {


    String crypt(char[] value);

    String decrypt(String value);

    default boolean isCrypted(String value) {
        return value != null && value.startsWith("{enc}");
    }

    default Map<String, String> fixValues(Map<String, String> in) {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, String> e : in.entrySet()) {
            if (isCrypted(e.getValue())) {
                ret.put(e.getKey(), decrypt(e.getValue()));
            } else {
                ret.put(e.getKey(), e.getValue());
            }
        }
        return ret;
    }
}
