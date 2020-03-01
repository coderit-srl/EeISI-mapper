package it.infocert.eigor.api.configuration;

import java.io.File;

/**
 * The Eigor configuration.
 * The main way to load such configuration is to use {@link DefaultEigorConfigurationLoader the default loader}.
 */
public interface EigorConfiguration {

    String getMandatoryString(String property);

    File getMandatoryFile(String property);
}
