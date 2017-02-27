package org.s4s0l.shathel.commons.core.storage;

import org.s4s0l.shathel.commons.core.ParameterProvider;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public interface Storage {

    File getDependencyCacheDirectory(ParameterProvider parameterProvider, String env);

    File getDataDirectory(ParameterProvider parameterProvider, String env);

    File getSafeDirectory(ParameterProvider parameterProvider, String env);

    File getSettingsDirectory(ParameterProvider parameterProvider, String env);

    File getEnrichedDirectory(ParameterProvider parameterProvider, String env);

    File getTemptDirectory(ParameterProvider parameterProvider, String env);

    void verify();

    File getConfiguration();






}
