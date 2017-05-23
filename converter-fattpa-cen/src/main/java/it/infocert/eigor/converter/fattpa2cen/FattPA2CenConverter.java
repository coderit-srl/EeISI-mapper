package it.infocert.eigor.converter.fattpa2cen;

import com.google.common.base.Preconditions;
import it.infocert.eigor.api.ConversionResult;
import it.infocert.eigor.api.ToCenConversion;
import it.infocert.eigor.converter.fattpa2cen.mapping.probablyDeprecated.FattPA2CenMapper;
import it.infocert.eigor.converter.fattpa2cen.models.FatturaElettronicaType;
import it.infocert.eigor.model.core.enums.Untdid5305DutyTaxFeeCategories;
import it.infocert.eigor.model.core.model.BG0000Invoice;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FattPA2CenConverter implements ToCenConversion {

    private static final Logger log = LoggerFactory.getLogger(FattPA2CenConverter.class);

    private Reflections reflections;

    public FattPA2CenConverter(Reflections reflections) {
        this.reflections = reflections;
    }

    public ConversionResult<BG0000Invoice> convert(InputStream input) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            doc = dBuilder.parse(input);
        } catch ( IOException | ParserConfigurationException | SAXException e) {
            log.error(e.getMessage(), e);
        }
        assert doc != null;
        doc.getDocumentElement().normalize();

        //TODO Implement conversion

        return new ConversionResult<BG0000Invoice>( new BG0000Invoice() );
    }

    public BG0000Invoice convert(String fileName) {
        return convert(new File(fileName));
    }

    public BG0000Invoice convert(File file) {
        BG0000Invoice converted = null;

        try(FileInputStream input = new FileInputStream(file)) {
            converted = convert(input).getResult();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return converted;
    }


    @Override
    public boolean support(String format) {
        return "fattpa".equals(format);
    }

    @Override
    public Set<String> getSupportedFormats() {
        return new HashSet<>( Arrays.asList("fattpa") );
    }
}