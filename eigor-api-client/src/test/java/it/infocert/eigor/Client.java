package it.infocert.eigor;

import it.infocert.eigor.api.ConversionRepository;
import it.infocert.eigor.api.configuration.DefaultEigorConfigurationLoader;
import it.infocert.eigor.api.configuration.EigorConfiguration;
import it.infocert.eigor.converter.cen2fattpa.Cen2FattPA;
import it.infocert.eigor.converter.cii2cen.Cii2Cen;
import it.infocert.eigor.converter.ubl2cen.Ubl2Cen;
import org.junit.Test;
import org.reflections.Reflections;

import java.io.IOException;

public class Client {

    @Test public void setUpAndUseTheAPI() throws IOException {

        EigorConfiguration configuration = new DefaultEigorConfigurationLoader().loadConfiguration();
        Reflections reflections = new Reflections("it.infocert");

        ConversionRepository conversionRepository =
                new ConversionRepository.Builder()
                        .register(new Cii2Cen(reflections, configuration))
                        .register(new Ubl2Cen(reflections, configuration))
                        .register(new Cen2FattPA(reflections, configuration))
                        .build();

        // yet to be completed...

    }

}