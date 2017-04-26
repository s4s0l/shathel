package org.s4s0l.lshathel.testutils;

import org.immutables.value.Value;
import org.s4s0l.shathel.commons.core.CommonParams;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.utils.ExtensionInterface;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public interface SchathelCreationCommonContract {


    List<ExtensionInterface> extensions();


    Map<String, String> params();

    @Value.Default
    default String shathelEnv() {
        return parameters().getParameter(CommonParams.SHATHEL_ENV).orElse("local");
    }

    @Value.Lazy
    default Parameters parameters() {
        return Parameters.fromMapWithSysPropAndEnv(params());
    }
}
