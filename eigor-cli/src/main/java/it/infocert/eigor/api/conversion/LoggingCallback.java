package it.infocert.eigor.api.conversion;

import java.io.File;

public class LoggingCallback extends AbstractConversionCallback {

    private final File outputFolderFile;
    private LogSupport logSupport;

    public LoggingCallback(File outputFolderFile) {
        this.outputFolderFile = outputFolderFile;
    }

    @Override
    public void onStartingConversion(ConversionContext ctx) throws Exception {
        logSupport = new LogSupport();
        if (logSupport.isLogbackSupportActive()) {
            logSupport.addLogger(new File(outputFolderFile, "invoice-transformation.log"));
        }
    }

    @Override
    public void onTerminatedConversion(ConversionContext ctx) throws Exception {
        if (logSupport != null) {
            logSupport.removeLogger();
        }
    }
}
