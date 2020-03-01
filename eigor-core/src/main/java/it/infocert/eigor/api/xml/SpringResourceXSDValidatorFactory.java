package it.infocert.eigor.api.xml;

import it.infocert.eigor.api.errors.ErrorCode;
import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class SpringResourceXSDValidatorFactory {

    public static XSDValidator getValidator(Resource resource, ErrorCode.Location location) {
        if (resource instanceof ClassPathResource) {
            ClassPathResource cpr = (ClassPathResource) resource;
            return new ClasspathXSDValidator(cpr.getPath(), location);
        } else if (resource instanceof AbstractFileResolvingResource) {
            AbstractFileResolvingResource fsr = (AbstractFileResolvingResource) resource;
            try {
                return new FileXSDValidator(fsr.getFile(), location);
            } catch (SAXException | IOException e) {
                throw new RuntimeException("Error while getting XSDValidator", e);
            }
        } else {
            throw new RuntimeException("Unsupported resource class " + resource.getClass() + ". Resource: " + resource.getDescription());
        }
    }
}
