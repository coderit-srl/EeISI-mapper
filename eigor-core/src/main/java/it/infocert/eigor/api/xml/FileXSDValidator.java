package it.infocert.eigor.api.xml;

import it.infocert.eigor.api.errors.ErrorCode;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class FileXSDValidator extends XSDValidator {

    public FileXSDValidator(File schemaFile, SchemaFactory schemaFactory, ErrorCode.Location callingLocation) throws SAXException {
        this(new StreamSource(schemaFile), schemaFactory, callingLocation);
    }

    public FileXSDValidator(Source schemaSource, SchemaFactory schemaFactory, ErrorCode.Location callingLocation) throws SAXException {

        super(callingLocation, null, schemaSource);

    }

    public FileXSDValidator(File schemaFile, ErrorCode.Location callingLocation) throws SAXException {
        this(new StreamSource(schemaFile), callingLocation);
    }

    public FileXSDValidator(Source schemaSource, ErrorCode.Location callingLocation) throws SAXException {

        super(callingLocation, null, schemaSource);

    }

}
