package it.infocert.eigor.api;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.helger.schematron.xslt.SchematronResourceXSLT;
import it.infocert.eigor.api.errors.ErrorCode;
import it.infocert.eigor.api.errors.ErrorMessage;
import it.infocert.eigor.api.utils.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class SchematronValidator implements IXMLValidator {

    private final ErrorCode.Location callingLocation;
    private ISchematronResource schematronResource;
    private static final Logger log = LoggerFactory.getLogger(SchematronValidator.class);
    private ErrorCode.Action defaultAction = ErrorCode.Action.SCH_VALIDATION;

    public SchematronValidator(Resource schemaResource, boolean isXSLT, boolean xsltFileUpdate, ErrorCode.Location callingLocation) {
        this.callingLocation = callingLocation;

        // just to test if it can be used as a file
        boolean canBeReadAsFile = true;
        File asFile = null;
        try {
            asFile = schemaResource.getFile();
        } catch (Exception e) {
            canBeReadAsFile = false;
        }


        long delta = System.currentTimeMillis();
        try {
            checkArgument(schemaResource != null, "Provide a Schematron file.");

            if (isXSLT) {
                if (asFile != null && xsltFileUpdate) {
                    // we check if relative path ../schematron contains newer .sch files
                    SchematronXSLTFileUpdater xsltFileUpdater = new SchematronXSLTFileUpdater(
                            asFile.getParent(),
                            asFile.getAbsoluteFile().getParentFile().getParent() + "/schematron"
                    );

                    int count = xsltFileUpdater.updateXSLTfromSch();
                    log.info("{} XSLT files were updated.", count);
                }
                checkArgument(schemaResource.exists(), "Schematron XSLT file '%s' (resolved to absolute path '%s') does not exist.", schemaResource.getURI(), canBeReadAsFile ? asFile.getAbsolutePath() : "N/A");

                if (canBeReadAsFile) {
                    schematronResource = SchematronResourceXSLT.fromFile(asFile);
                } else {
                    schematronResource = SchematronResourceXSLT.fromClassPath(((ClassPathResource) schemaResource).getPath());
                }
            } else {
                checkArgument(schemaResource.exists(), "Schematron file '%s' (resolved to absolute path '%s') does not exist.", schemaResource.getURI(), canBeReadAsFile ? asFile.getAbsolutePath() : "N/A");
                if (asFile != null) {
                    schematronResource = SchematronResourceSCH.fromFile(asFile);
                } else {
                    schematronResource = SchematronResourceSCH.fromClassPath(((ClassPathResource) schemaResource).getPath());
                }

            }
            if (!schematronResource.isValidSchematron())
                throw new IllegalArgumentException(
                        String.format("Invalid %s Schematron file '%s' (resolved to absolute path '%s').", isXSLT ? "XSLT" : "SCH", schemaResource, canBeReadAsFile ? asFile.getAbsolutePath() : "N/A")
                );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            delta = System.currentTimeMillis() - delta;
            log.info(MarkerFactory.getMarker("PERFORMANCE"), "Loaded '{}' in {}ms.", schemaResource.toString(), delta);
        }
    }

    @Override
    public List<IConversionIssue> validate(byte[] xml) {
        List<IConversionIssue> errors = new ArrayList<>();
        SchematronOutputType schematronOutput;

        try {
            StreamSource source = new StreamSource(new ByteArrayInputStream(xml));
            schematronOutput = schematronResource.applySchematronValidationToSVRL(source);
        } catch (Exception e) {
            errors.add(ConversionIssue.newWarning(e, "Error during Schematron Validation.",
                    callingLocation,
                    defaultAction,
                    ErrorCode.Error.INVALID,
                    Pair.of(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())));
            return errors;
        }


        List<Object> firedRuleAndFailedAssert = new ArrayList<>();

        if (schematronOutput != null) {
            try {
                List<Object> asserts = schematronOutput.getActivePatternAndFiredRuleAndFailedAssert();
                firedRuleAndFailedAssert.addAll(asserts);
                log.trace(asserts.toString());
            } catch (Exception e) {
                errors.add(ConversionIssue.newWarning(e, "Error during Schematron result registration.",
                        callingLocation,
                        defaultAction,
                        ErrorCode.Error.INVALID,
                        Pair.of(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())));
                return errors;
            }
        } else {
            final String message = "Schematron parsing failed. File: " + schematronResource.getID();
            log.error(message);
            errors.add(ConversionIssue.newError(new EigorRuntimeException(message, callingLocation, defaultAction, ErrorCode.Error.INVALID, Pair.of(ErrorMessage.OFFENDINGITEM_PARAM, schematronResource.getID()))));
        }


        for (Object obj : firedRuleAndFailedAssert) {
            if (obj instanceof FailedAssert) {
                FailedAssert failedAssert = (FailedAssert) obj;

                Exception cause = new Exception(
                        failedAssert.getLocation() + " failed test: " + failedAssert.getTest()
                );

                String ruleDescriptionFromSchematron = failedAssert.getText().trim().replaceAll("\\n", " ").replaceAll(" {2,}", " ");
                String offendingElement = failedAssert.getLocation().trim();
                EigorException error = new EigorException(
                        ErrorMessage.builder()
                                .message(String.format("Schematron failed assert '%s' on XML element at '%s'.",
                                        ruleDescriptionFromSchematron,
                                        offendingElement)
                                )
                                .location(callingLocation)
                                .action(defaultAction)
                                .error(ErrorCode.Error.INVALID)
                                .build(),
                        cause
                );

                if ("fatal".equals(failedAssert.getFlag())) {
                    errors.add(ConversionIssue.newError(error));
                } else {
                    errors.add(ConversionIssue.newWarning(error));
                }
            }
        }
        return errors;
    }

    public void setDefaultAction(ErrorCode.Action defaultAction) {
        this.defaultAction = defaultAction;
    }
}
