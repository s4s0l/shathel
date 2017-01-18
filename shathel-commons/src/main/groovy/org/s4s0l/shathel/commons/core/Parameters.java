package org.s4s0l.shathel.commons.core;

import lombok.Builder;
import lombok.Singular;

import java.util.Map;

/**
 * @author Matcin Wielgus
 */
@Builder
public class Parameters {
    @Singular
    private final Map<String, String> parameters;

    public String getParameter(String name,String defaultValue) {
        String s = parameters.get(name);
        if(s == null){
            return defaultValue;
        }
        return s;
    }


}
