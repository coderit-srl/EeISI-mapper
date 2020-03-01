package it.infocert.eigor.api.configuration;

import it.infocert.eigor.api.EigorRuntimeException;
import it.infocert.eigor.api.errors.ErrorMessage;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertiesBackedConfiguration implements EigorConfiguration {

    private final PropertiesWithReplacement properties;
    private final DefaultResourceLoader drl;

    public PropertiesBackedConfiguration() {
        this.properties = new PropertiesWithReplacement();
        this.drl = new DefaultResourceLoader(PropertiesBackedConfiguration.class.getClassLoader());
    }

    public PropertiesBackedConfiguration(final Properties properties) {
        this.properties = new PropertiesWithReplacement(checkNotNull(properties));
        this.drl = new DefaultResourceLoader(PropertiesBackedConfiguration.class.getClassLoader());
    }

    public Object setProperty(String property, String value) {
        return properties.setProperty(property, value);
    }

    public PropertiesBackedConfiguration addProperty(String property, String value) {
        properties.setProperty(property, value);
        return this;
    }

    @Override
    public String getMandatoryString(String property) {
        String theProperty = properties.getProperty(property);
        if (theProperty == null) throw MissingMandatoryPropertyException.missingProperty(property);
        return theProperty;
    }

    @Override
    public File getMandatoryFile(String property) {
        String res = getMandatoryString(property);
        try {
            DefaultResourceLoader drl = new DefaultResourceLoader();
            Resource resource = drl.getResource(res);
            if (resource.isFile()) {
                return resource.getFile();
            } else {
                throw new RuntimeException("Could not open property " + property + " as file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error opening property " + property + " as file", e);
        }
    }
}
