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
//@Value.Immutable
//@Value.Style(depluralize = true, typeAbstract = "*Contract", typeImmutable = "*")
public interface SchathelCreationCommonContract {

    @Value.Default
    default List<ExtensionInterface> extensions() {
        return Collections.emptyList();
    }

    @Value.Default
    default Map<String, String> params() {
        return Collections.emptyMap();
    }

    @Value.Default
    default String shathelEnv() {
        return params().getOrDefault(CommonParams.SHATHEL_ENV, "local");
    }

    @Value.Lazy
    default Parameters parameters() {
        return Parameters.fromMapWithSysPropAndEnv(params());
    }
}
