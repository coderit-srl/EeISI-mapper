package it.infocert.eigor.converter.cii2cen;

import com.google.common.io.ByteStreams;
import it.infocert.eigor.api.*;
import it.infocert.eigor.api.configuration.ConfigurationException;
import it.infocert.eigor.api.configuration.DefaultEigorConfigurationLoader;
import it.infocert.eigor.api.configuration.EigorConfiguration;
import it.infocert.eigor.api.errors.ErrorCode;
import it.infocert.eigor.api.utils.IReflections;
import it.infocert.eigor.api.utils.JavaReflections;
import it.infocert.eigor.api.xml.FileXSDValidator;
import it.infocert.eigor.api.xml.XSDValidator;
import it.infocert.eigor.model.core.InvoiceUtils;
import it.infocert.eigor.model.core.datatypes.Identifier;
import it.infocert.eigor.model.core.enums.Iso4217CurrenciesFundsCodes;
import it.infocert.eigor.model.core.model.*;
import org.springframework.core.io.FileSystemResource;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class Cii2CenConfigurationFileTest {

    static MyCiiToCenConverter sut;

    @BeforeClass
    public static void setUp() throws ConfigurationException {
        sut = new MyCiiToCenConverter(new JavaReflections(), DefaultEigorConfigurationLoader.configuration());
        sut.configure();
    }

    @Test
    public void issueEisi288() throws SyntaxErrorInInvoiceFormatException {
        InputStream sourceInvoiceStream = getClass().getResourceAsStream("/eisi-288-cii.xml");
        assertThat(sourceInvoiceStream, notNullValue());
        ConversionResult<BG0000Invoice> convert = sut.convert(sourceInvoiceStream);
        Identifier bt60 = InvoiceUtils.evalExpression(() -> convert.getResult().getBG0010Payee(0).getBT0060PayeeIdentifierAndSchemeIdentifier(0).getValue());
        assertThat(bt60.getIdentifier(), equalTo("Payee identifier 2"));
        assertThat(bt60.getIdentificationSchema(), equalTo("0059"));
        assertThat(bt60.getSchemaVersion(), nullValue());
    }

    @Test
    public void issueEisi193() throws SyntaxErrorInInvoiceFormatException {
        InputStream sourceInvoiceStream = getClass().getResourceAsStream("/eisi-294-cii.xml");
        assertThat(sourceInvoiceStream, notNullValue());
        ConversionResult<BG0000Invoice> convert = sut.convert(sourceInvoiceStream);
        LocalDate bt26Value = InvoiceUtils.evalExpression(() -> convert.getResult().getBG0003PrecedingInvoiceReference(0).getBT0026PrecedingInvoiceIssueDate(0).getValue());
        assertThat(bt26Value, CoreMatchers.equalTo(new LocalDate(2015, 3, 1)));
    }

    @Test
    public void shouldAcceptACiiInvoiceMatchingTheCiiXsd() throws IOException, SAXException {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1M.xml");
        List<IConversionIssue> errors = validateXmlWithCiiXsd(sourceInvoiceStream);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldRefuseACiiInvoiceNotValidAccordingToCiiXsd() throws IOException, SAXException {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1_KO.xml");
        List<IConversionIssue> errors = validateXmlWithCiiXsd(sourceInvoiceStream);
        assertTrue(errors.size() == 1);
        IConversionIssue issue = errors.get(0);
        assertTrue(issue.getCause() instanceof SAXParseException);
        assertTrue(issue.isError());
        assertTrue(issue.getMessage().endsWith("XSD validation failed"));
    }

    @Ignore("Waiting for updated CII examples that complies with the validations.")
    @Test
    public void shouldAcceptACiiInvoiceThatSatisfiesTheCiiSchematron() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example9M.xml");
        List<IConversionIssue> errors = validateXmlWithCiiSchematron(sourceInvoiceStream);
        assertTrue(errors.stream().map(error -> error.getErrorMessage().toString() + "\n").reduce("", (acc, str) -> acc = acc + str), errors.isEmpty());
    }

    @Ignore("Waiting for updated CII examples that complies with the validations.")
    @Test
    public void shouldRefuseACiiInvoiceThatDoesNotSatisfyTheCiiSchematron() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1_KO.xml");
        List<IConversionIssue> errors = validateXmlWithCiiSchematron(sourceInvoiceStream);
        String temp;
        for (IConversionIssue conversionIssue : errors) {
            temp = conversionIssue.getMessage();
            assertTrue(temp.contains("[BR-02]") || temp.contains("[BR-04]") || temp.contains("[BR-S-02]") || temp.contains("[BR-S-08]") || temp.contains("[CII-SR-014]"));
        }
    }

    @Test
    public void shouldAcceptACiiInvoiceThatSatisfiesTheCiiCIUSSchematron() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example9M-ita-compliant.xml");
        List<IConversionIssue> errors = validateXmlWithCiiCIUSSchematron(sourceInvoiceStream);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldRefuseACiiInvoiceThatDoesNotSatisfyTheCiiCIUSSchematron() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1_KO.xml");
        List<IConversionIssue> errors = validateXmlWithCiiCIUSSchematron(sourceInvoiceStream);
        String temp;
        for (IConversionIssue conversionIssue : errors) {
            temp = conversionIssue.getMessage();
            assertTrue(temp.contains("CII_IN.CIUS_SCH_VALIDATION.INVALID - Schematron failed assert"));
        }
    }

    @Test
    public void testOneToOneTrasformationMapping() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        ConversionResult<BG0000Invoice> result = oneToOneMapping(sourceInvoiceStream);
        BG0000Invoice invoice = result.getResult();
        BT0005InvoiceCurrencyCode expected = new BT0005InvoiceCurrencyCode(Iso4217CurrenciesFundsCodes.DKK); //CII_example5M-ita-compliant.xml
        assertEquals(expected, invoice.getBT0005InvoiceCurrencyCode(0));
    }

    @Test
    public void testFailOneToOneTrasformationMapping() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1_KO.xml");
        ConversionResult<BG0000Invoice> result = oneToOneMapping(sourceInvoiceStream);
        BG0000Invoice invoice = result.getResult();
        assertTrue(invoice.getBT0001InvoiceNumber().isEmpty());
    }

    @Test
    public void testManyToOneTrasformationMapping() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        ConversionResult<BG0000Invoice> result = manyToOneMapping(sourceInvoiceStream);
        BG0000Invoice invoice = result.getResult();
        BT0041SellerContactPoint expectedBT0041 = new BT0041SellerContactPoint("Anthon Larsen Prova dep");
        assertEquals(expectedBT0041, invoice.getBG0004Seller(0).getBG0006SellerContact(0).getBT0041SellerContactPoint(0));
    }

    @Test
    public void testFailManyToOneTrasformationMapping() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example1_KO.xml");
        ConversionResult<BG0000Invoice> result = manyToOneMapping(sourceInvoiceStream);
        BG0000Invoice invoice = result.getResult();
        assertTrue(invoice.getBT0011ProjectReference().isEmpty());
    }

    @Test
    public void testInvoiceNoteConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        InvoiceNoteConverter bg0001 = new InvoiceNoteConverter();
        ConversionResult<BG0000Invoice> result = bg0001.toBG0001(document, invoice, errors);

        assertEquals("AAI", result.getResult().getBG0001InvoiceNote(0).getBT0021InvoiceNoteSubjectCode().get(0).getValue());
    }

    @Test
    public void testBuyerIdentifierConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        BG0007Buyer bg0007 = new BG0007Buyer();
        invoice.getBG0007Buyer().add(bg0007);

        BuyerIdentifierConverter bt0046 = new BuyerIdentifierConverter();
        ConversionResult<BG0000Invoice> result = bt0046.toBT0046(document, invoice, errors);

        assertEquals("5790000436057", result.getResult().getBG0007Buyer(0).getBT0046BuyerIdentifierAndSchemeIdentifier(0).getValue().getIdentifier());
    }

    @Test
    public void testDeliverTOLocationIdentifierConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        BG0013DeliveryInformation bg0013 = new BG0013DeliveryInformation();
        invoice.getBG0013DeliveryInformation().add(bg0013);

        DeliverToLocationIdentifierConverter bt0071 = new DeliverToLocationIdentifierConverter();
        ConversionResult<BG0000Invoice> result = bt0071.toBT0071(document, invoice, errors);

        assertEquals("5790000436068", result.getResult().getBG0013DeliveryInformation(0).getBT0071DeliverToLocationIdentifierAndSchemeIdentifier(0).getValue().getIdentifier());
    }

    @Test
    public void testPrecedingInvoiceReferenceConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        PrecedingInvoiceReferenceConverter bg0003 = new PrecedingInvoiceReferenceConverter();
        ConversionResult<BG0000Invoice> result = bg0003.toBG0003(document, invoice, errors, ErrorCode.Location.CII_IN);

        assertEquals("TOSL109", result.getResult().getBG0003PrecedingInvoiceReference(0).getBT0025PrecedingInvoiceReference(0).getValue());
        assertEquals("2013-03-10", result.getResult().getBG0003PrecedingInvoiceReference(0).getBT0026PrecedingInvoiceIssueDate(0).getValue().toString());
    }

    @Test
    public void testCreditTransferConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();
        CreditTransferConverter bg0017 = new CreditTransferConverter();

        BG0016PaymentInstructions bg0016 = new BG0016PaymentInstructions();
        invoice.getBG0016PaymentInstructions().add(bg0016);

        ConversionResult<BG0000Invoice> result = bg0017.toBG0017(document, invoice, errors);

        assertEquals("1234567890123456", result.getResult().getBG0016PaymentInstructions(0).getBG0017CreditTransfer(0).getBT0084PaymentAccountIdentifier(0).getValue());
        assertEquals("Nome account", result.getResult().getBG0016PaymentInstructions(0).getBG0017CreditTransfer(0).getBT0085PaymentAccountName(0).getValue());
    }

    @Test
    public void testAdditionalSupportingDocumentsConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        AdditionalSupportingDocumentsConverter bg0024 = new AdditionalSupportingDocumentsConverter();
        ConversionResult<BG0000Invoice> result = bg0024.toBG0024(document, invoice, errors, ErrorCode.Location.CII_IN);

        assertEquals("123456", invoice.getBG0024AdditionalSupportingDocuments(0).getBT0124ExternalDocumentLocation(0).getValue());
    }

    @Test
    public void testFailAdditionalSupportingDocumentsConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        AdditionalSupportingDocumentsConverter bg0024 = new AdditionalSupportingDocumentsConverter();
        ConversionResult<BG0000Invoice> result = bg0024.toBG0024(document, invoice, errors, ErrorCode.Location.CII_IN);

        assertTrue(invoice.getBG0024AdditionalSupportingDocuments(0).getBT0124ExternalDocumentLocation().isEmpty());
    }

    @Test
    public void testVATBreakdownConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        VATBreakdownConverter bg0023 = new VATBreakdownConverter();
        ConversionResult<BG0000Invoice> result = bg0023.toBG0023(document, invoice, errors, ErrorCode.Location.CII_IN);

        assertEquals("provaReason", invoice.getBG0023VatBreakdown(0).getBT0120VatExemptionReasonText(0).getValue());
        assertEquals("AAA", invoice.getBG0023VatBreakdown(0).getBT0121VatExemptionReasonCode(0).getValue());
    }

    @Test
    public void testInvoiceLineConverter() throws Exception {
        InputStream sourceInvoiceStream = getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example5M-ita-compliant.xml");
        Document document = getDocument(sourceInvoiceStream);
        BG0000Invoice invoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        InvoiceLineConverter bg0025 = new InvoiceLineConverter();
        ConversionResult<BG0000Invoice> result = bg0025.toBG0025(document, invoice, errors, ErrorCode.Location.CII_IN);

        assertEquals("1", result.getResult().getBG0025InvoiceLine(0).getBT0126InvoiceLineIdentifier(0).getValue());
        assertEquals("C62", result.getResult().getBG0025InvoiceLine(0).getBG0029PriceDetails(0).getBT0150ItemPriceBaseQuantityUnitOfMeasureCode(0).getValue().getCommonCode());
        assertEquals("C62", result.getResult().getBG0025InvoiceLine(0).getBT0130InvoicedQuantityUnitOfMeasureCode(0).getValue().getCommonCode());
    }

    @Test
    public void testShouldNotIdentifyAttachedDocumentElement() throws Exception {

        // given
        Document ciiInvoice = getDocument(getClass().getClassLoader().getResourceAsStream("examples/cii/CII_example8.xml"));
        BG0000Invoice cenInvoice = new BG0000Invoice();
        List<IConversionIssue> errors = new ArrayList<>();

        AdditionalSupportingDocumentsConverter sut = new AdditionalSupportingDocumentsConverter();

        // when
        ConversionResult<BG0000Invoice> result = sut.toBG0024(ciiInvoice, cenInvoice, errors, ErrorCode.Location.CII_IN);

        // then
        assertTrue("AdditionalReferencedDocument with typeCode=130 should not be mapped under BG0024.", result.getResult().getBG0024AdditionalSupportingDocuments().isEmpty());
    }

    private List<IConversionIssue> validateXmlWithCiiXsd(InputStream sourceInvoiceStream) throws IOException, SAXException {
        byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
        String filePath = "../converter-commons/src/main/resources/converterdata/converter-commons/cii/xsd/uncoupled/data/standard/CrossIndustryInvoice_100pD16B.xsd";
        File xsdFile = new File(filePath);
        XSDValidator xsdValidator = new FileXSDValidator(xsdFile, ErrorCode.Location.CII_IN);
        return xsdValidator.validate(bytes);
    }

    private List<IConversionIssue> validateXmlWithCiiSchematron(InputStream sourceInvoiceStream) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
        File schematronFile = FileUtils.getFile("../converter-commons/src/main/resources/converterdata/converter-commons/cii/schematron-xslt/EN16931-CII-validation.xslt");
        IXMLValidator ciiValidator = new SchematronValidator(new FileSystemResource(schematronFile), true, false, ErrorCode.Location.CII_IN);
        return ciiValidator.validate(bytes);
    }

    private List<IConversionIssue> validateXmlWithCiiCIUSSchematron(InputStream sourceInvoiceStream) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
        File schematronFile = FileUtils.getFile("../converter-commons/src/main/resources/converterdata/converter-commons/cii/cius/schematron-xslt/EN16931-CIUS-IT-CIIValidation.xslt");
        SchematronValidator ciiCIUSValidator = new CiusSchematronValidator(new FileSystemResource(schematronFile), true, false, ErrorCode.Location.CII_IN);
        return ciiCIUSValidator.validate(bytes);
    }

    private ConversionResult<BG0000Invoice> oneToOneMapping(InputStream sourceInvoiceStream) throws Exception {
        byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
        InputStream clonedInputStream = new ByteArrayInputStream(bytes);
        Document document = getDocument(clonedInputStream);
        List<IConversionIssue> errors = new ArrayList<>();
        return sut.applyOne2OneTransformationsBasedOnMapping(document, errors);
    }

    private ConversionResult<BG0000Invoice> manyToOneMapping(InputStream sourceInvoiceStream) throws Exception {
        byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
        InputStream clonedInputStream = new ByteArrayInputStream(bytes);
        Document document = getDocument(clonedInputStream);
        List<IConversionIssue> errors = new ArrayList<>();
        BG0000Invoice invoice = sut.applyOne2OneTransformationsBasedOnMapping(document, errors).getResult();
        return sut.applyMany2OneTransformationsBasedOnMapping(invoice, document, errors);
    }

    public Document getDocument(InputStream sourceInvoiceStream) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setIgnoringBoundaryWhitespace(true);
        return saxBuilder.build(sourceInvoiceStream);
    }

    static class MyCiiToCenConverter extends Cii2Cen {

        public MyCiiToCenConverter(IReflections reflections, EigorConfiguration configuration) throws ConfigurationException {
            super(reflections, configuration);
        }

        @Override
        public ConversionResult<BG0000Invoice> applyOne2OneTransformationsBasedOnMapping(Document document, List<IConversionIssue> errors) throws SyntaxErrorInInvoiceFormatException {
            return super.applyOne2OneTransformationsBasedOnMapping(document, errors);
        }

        @Override
        public ConversionResult<BG0000Invoice> applyMany2OneTransformationsBasedOnMapping(BG0000Invoice partialInvoice, Document document, List<IConversionIssue> errors) throws SyntaxErrorInInvoiceFormatException {
            return super.applyMany2OneTransformationsBasedOnMapping(partialInvoice, document, errors);
        }
    }
}
