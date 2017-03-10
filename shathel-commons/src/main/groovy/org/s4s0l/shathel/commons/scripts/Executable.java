package org.s4s0l.shathel.commons.scripts;

import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface Executable {

    Object execute(Map<String, Object> context);
}
