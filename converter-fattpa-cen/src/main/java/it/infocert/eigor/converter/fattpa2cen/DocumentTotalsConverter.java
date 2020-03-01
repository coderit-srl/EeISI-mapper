package it.infocert.eigor.converter.fattpa2cen;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import it.infocert.eigor.api.ConversionIssue;
import it.infocert.eigor.api.CustomMapping;
import it.infocert.eigor.api.EigorRuntimeException;
import it.infocert.eigor.api.IConversionIssue;
import it.infocert.eigor.api.configuration.EigorConfiguration;
import it.infocert.eigor.api.conversion.ConversionFailedException;
import it.infocert.eigor.api.conversion.converter.TypeConverter;
import it.infocert.eigor.api.errors.ErrorCode;
import it.infocert.eigor.api.errors.ErrorMessage;
import it.infocert.eigor.converter.fattpa2cen.converters.ItalianNaturaToUntdid5305DutyTaxFeeCategoriesConverter;
import it.infocert.eigor.model.core.enums.Untdid5305DutyTaxFeeCategories;
import it.infocert.eigor.model.core.enums.Untdid7161SpecialServicesCodes;
import it.infocert.eigor.model.core.model.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class DocumentTotalsConverter implements CustomMapping<Document> {
    private final static Logger log = LoggerFactory.getLogger(DocumentTotalsConverter.class);

    private AttachmentUtil aUtil = null;

    @Override
    public void map(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation, @NotNull EigorConfiguration eigorConfiguration) {

        if (aUtil == null) {
            Preconditions.checkArgument(eigorConfiguration != null, "Please provide a not null configuration.");
            aUtil = new AttachmentUtil(eigorConfiguration.getMandatoryFile("eigor.workdir"));
        }
        addInvoiceTotalAmountVat(invoice, document, errors, callingLocation);
        addInvoiceTotalAmountWithVatDefault(invoice, document, errors, callingLocation);
        addAdditionalSupportingDocument(invoice, document, errors, callingLocation);

    }


    private void addAdditionalSupportingDocument(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation) {
        Element rootElement = document.getRootElement();
        Element fatturaElettronicaBody = rootElement.getChild("FatturaElettronicaBody");
        Element fatturaElettronicaHeader = rootElement.getChild("FatturaElettronicaHeader");
        if (fatturaElettronicaHeader != null) {
            Element datiTrasmissione = fatturaElettronicaHeader.getChild("DatiTrasmissione");

            if (datiTrasmissione != null) {

                Element idTrasmittente = datiTrasmissione.getChild("IdTrasmittente");
                try {
                    if (idTrasmittente != null) {
                        Element idPaese = idTrasmittente.getChild("IdPaese");
                        if (idPaese != null) {
                            aUtil.addValuesToAttachment(invoice, "1.1.1.1 IdPaese: " + idPaese.getText(), errors);

                        }
                        Element idCodice = idTrasmittente.getChild("IdCodice");
                        if (idCodice != null) {
                            aUtil.addValuesToAttachment(invoice, "1.1.1.2 IdCodice: " + idCodice.getText(), errors);

                        }
                    }
                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, idTrasmittente.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));

                }
                Element progressivoInvio = datiTrasmissione.getChild("ProgressivoInvio");
                try {
                    if (progressivoInvio != null) {

                        aUtil.addValuesToAttachment(invoice, "1.1.2 ProgressivoInvio: " + progressivoInvio.getText(), errors);

                    }


                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, progressivoInvio.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));

                }
                Element formatoTrasmissione = datiTrasmissione.getChild("FormatoTrasmissione");
                try {
                    if (formatoTrasmissione != null) {
                        aUtil.addValuesToAttachment(invoice, "1.1.3 FormatoTrasmissione: " + formatoTrasmissione.getText(), errors);

                    }


                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, formatoTrasmissione.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));

                }


                Element contattiTrasmittente = datiTrasmissione.getChild("ContattiTrasmittente");
                try {
                    if (contattiTrasmittente != null) {
                        Element telefono = contattiTrasmittente.getChild("Telefono");
                        if (telefono != null) {
                            aUtil.addValuesToAttachment(invoice, "1.1.5.1 Telefono: " + telefono.getText(), errors);

                        }
                        Element email = contattiTrasmittente.getChild("Email");
                        if (email != null) {
                            aUtil.addValuesToAttachment(invoice, "1.1.5.2 Email: " + email.getText(), errors);

                        }
                    }
                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, contattiTrasmittente.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));

                }
            }
            Element terzoIntermediarioOSoggettoEmittente = fatturaElettronicaHeader.getChild("TerzoIntermediarioOSoggettoEmittente");
            if (terzoIntermediarioOSoggettoEmittente != null) {
                try {
                    Element datiAnagrafici = terzoIntermediarioOSoggettoEmittente.getChild("DatiAnagrafici");
                    if (datiAnagrafici != null) {
                        Element idFiscaleIVA = datiAnagrafici.getChild("IdFiscaleIVA");

                        if (idFiscaleIVA != null) {

                            Element idPaese = idFiscaleIVA.getChild("IdPaese");
                            if (idPaese != null) {
                                aUtil.addValuesToAttachment(invoice, "1.5.1.1.1 IdPaese: " + idPaese.getText(), errors);

                            }
                            Element idCodice = idFiscaleIVA.getChild("IdCodice");
                            if (idCodice != null) {
                                aUtil.addValuesToAttachment(invoice, "1.5.1.1.2 IdCodice: " + idCodice.getText(), errors);

                            }
                        }
                        Element codiceFiscale = datiAnagrafici.getChild("CodiceFiscale");
                        if (codiceFiscale != null) {
                            aUtil.addValuesToAttachment(invoice, "1.5.1.2 CodiceFiscale: " + codiceFiscale.getText(), errors);

                            Element anagrafica = datiAnagrafici.getChild("Anagrafica");
                            if (anagrafica != null) {
                                Element denominazione = anagrafica.getChild("Denominazione");
                                if (denominazione != null) {
                                    aUtil.addValuesToAttachment(invoice, "1.5.1.3.1 Denominazione: " + denominazione.getText(), errors);

                                }
                                Element nome = anagrafica.getChild("Nome");
                                if (nome != null) {
                                    aUtil.addValuesToAttachment(invoice, "1.5.1.3.2 Nome: " + nome.getText(), errors);

                                }
                                Element cognome = anagrafica.getChild("Cognome");
                                if (cognome != null) {
                                    aUtil.addValuesToAttachment(invoice, "1.5.1.3.3 Cognome: " + cognome.getText(), errors);

                                }
                                Element titolo = anagrafica.getChild("Titolo");
                                if (titolo != null) {
                                    aUtil.addValuesToAttachment(invoice, "1.5.1.3.4 Titolo: " + titolo.getText(), errors);

                                }
                                Element codEORI = anagrafica.getChild("CodEORI");
                                if (codEORI != null) {
                                    aUtil.addValuesToAttachment(invoice, "1.5.1.3.5 CodEORI: " + codEORI.getText(), errors);

                                }
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, terzoIntermediarioOSoggettoEmittente.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));
                }
            }
        }
        //       <DatiGenerali>
        //      <DatiGeneraliDocumento>
        //      <DatiRitenuta><!--to be defined, not anymore on BG-21 but on BG-25-->
        //			<TipoRitenuta>RT01</TipoRitenuta>
        //			<ImportoRitenuta>200.00</ImportoRitenuta><!-- optional to BT-113 with text on BT-20-->
        //			<AliquotaRitenuta>20.00</AliquotaRitenuta>
        //			<CausalePagamento>A</CausalePagamento>
        //		</DatiRitenuta>
        if (fatturaElettronicaBody != null) {
            Element datiGenerali = fatturaElettronicaBody.getChild("DatiGenerali");
            if (datiGenerali != null) {
                Element datiGeneraliDocumento = datiGenerali.getChild("DatiGeneraliDocumento");
                if (datiGeneraliDocumento != null) {
                    Element datiRitenuta = datiGeneraliDocumento.getChild("DatiRitenuta");
                    if (datiRitenuta != null) {
                        Element importoRitenuta = datiRitenuta.getChild("ImportoRitenuta");
                        if (importoRitenuta != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.5.2 ImportoRitenuta: " + importoRitenuta.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, importoRitenuta.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                    }

                    //DATI VEICOLI
                    Element datiVeicoli = fatturaElettronicaBody.getChild("DatiVeicoli");
                    if (datiVeicoli != null) {
                        try {
                            Element data = datiVeicoli.getChild("Data");
                            if (data != null) {
                                aUtil.addValuesToAttachment(invoice, "2.3.1 Data: " + data.getText(), errors);
                            }
                            Element totalePercorso = datiVeicoli.getChild("TotalePercorso");
                            if (totalePercorso != null) {
                                aUtil.addValuesToAttachment(invoice, "2.3.2 TotalePercorso: " + totalePercorso.getText(), errors);
                            }
                        } catch (IllegalArgumentException e) {
                            EigorRuntimeException ere = new EigorRuntimeException(
                                    e,
                                    ErrorMessage.builder()
                                            .message(e.getMessage())
                                            .location(ErrorCode.Location.FATTPA_IN)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .addParam(ErrorMessage.OFFENDINGITEM_PARAM, datiVeicoli.toString())
                                            .build());
                            errors.add(ConversionIssue.newError(ere));
                        }
                    }
                }
            }
        }
        //SoggettoEmittente
        if (fatturaElettronicaHeader != null) {
            Element soggettoEmittente = fatturaElettronicaHeader.getChild("SoggettoEmittente");
            if (soggettoEmittente != null) {
                try {
                    aUtil.addValuesToAttachment(invoice, "1.6 SoggettoEmittente: " + soggettoEmittente.getText(), errors);
                } catch (IllegalArgumentException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(
                            e,
                            ErrorMessage.builder()
                                    .message(e.getMessage())
                                    .location(ErrorCode.Location.FATTPA_IN)
                                    .action(ErrorCode.Action.HARDCODED_MAP)
                                    .error(ErrorCode.Error.ILLEGAL_VALUE)
                                    .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                    .addParam(ErrorMessage.OFFENDINGITEM_PARAM, soggettoEmittente.toString())
                                    .build());
                    errors.add(ConversionIssue.newError(ere));
                }
            }
        }

        //          <TipoRitenuta>RT01</TipoRitenuta>
        //			<ImportoRitenuta>200.00</ImportoRitenuta><!-- optional to BT-113 with text on BT-20-->
        //			<AliquotaRitenuta>20.00</AliquotaRitenuta>
        //			<CausalePagamento>A</CausalePagamento>
        if (fatturaElettronicaBody != null) {
            Element datiGenerali = fatturaElettronicaBody.getChild("DatiGenerali");
            if (datiGenerali != null) {
                Element datiGeneraliDocumento = datiGenerali.getChild("DatiGeneraliDocumento");
                if (datiGeneraliDocumento != null) {
                    Element datiRitenuta = datiGeneraliDocumento.getChild("DatiRitenuta");
                    if (datiRitenuta != null) {
                        Element tipoRitenuta = datiRitenuta.getChild("TipoRitenuta");
                        if (tipoRitenuta != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.5.1 TipoRitenuta: " + tipoRitenuta.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, tipoRitenuta.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                        Element importoRitenuta = datiRitenuta.getChild("ImportoRitenuta");
                        if (importoRitenuta != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.5.2 ImportoRitenuta: " + importoRitenuta.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, importoRitenuta.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                        Element aliquotaRitenuta = datiRitenuta.getChild("AliquotaRitenuta");
                        if (aliquotaRitenuta != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.5.3 AliquotaRitenuta: " + aliquotaRitenuta.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, aliquotaRitenuta.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                        Element causalePagamento = datiRitenuta.getChild("CausalePagamento");
                        if (causalePagamento != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.5.4 CausalePagamento: " + causalePagamento.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, causalePagamento.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                    }
                    //Aggiungere 2.1.1.7.6 Ritenuta
                    //FatturaElettronicaBody/DatiGenerali/DatiGeneraliDocumento/DatiCassaPrevidenziale/Ritenuta
                    Element datiCassaPrevidenziale = datiGeneraliDocumento.getChild("DatiCassaPrevidenziale");
                    if (datiCassaPrevidenziale != null) {
                        Element ritenuta = datiCassaPrevidenziale.getChild("Ritenuta");
                        if (ritenuta != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.1.1.7.6 Ritenuta: " + ritenuta.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, ritenuta.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                    }
                }//datiGeneraliDocumento

            }//if dati_generali
            //2.2.1.13	FatturaElettronicaBody/DatiBeniServizi/DettaglioLinee/Ritenuta

            //2.2.1.13 Ritenuta concatenate with 2.2.1.1 NumeroLinea
            //2.2.1.1	FatturaElettronicaBody/DatiBeniServizi/DettaglioLinee/NumeroLinea


            Element datiBeniServizi = fatturaElettronicaBody.getChild("DatiBeniServizi");
            if (datiBeniServizi != null) {

                Element dettaglioLinee = datiBeniServizi.getChild("DettaglioLinee");
                if (dettaglioLinee != null) {
                    Element ritenuta = dettaglioLinee.getChild("Ritenuta");
                    if (ritenuta != null) {

                        Element numeroLinea = dettaglioLinee.getChild("NumeroLinea");
                        if (numeroLinea != null) {
                            try {
                                aUtil.addValuesToAttachment(invoice, "2.2.1.13 Ritenuta: " + ritenuta.getText() + " 2.2.1.1 NumeroLinea: " + numeroLinea.getText(), errors);
                            } catch (IllegalArgumentException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(ErrorCode.Location.FATTPA_IN)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, ritenuta.toString() + numeroLinea.toString())
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        }
                    }
                }

            }//IF datibeniservizi

            if (datiGenerali != null) {
                Element datiOrdineAcquisto = datiGenerali.getChild("DatiOrdineAcquisto");
                if (datiOrdineAcquisto != null) {


                    try {
                        Element codiceCommessaConvenzione = datiOrdineAcquisto.getChild("CodiceCommessaConvenzione");
                        if (codiceCommessaConvenzione != null) {
                            aUtil.addValuesToAttachment(invoice, "2.1.2.5 CodiceCommessaConvenzione: " + codiceCommessaConvenzione.getText(), errors);
                        }
                        Element codiceCUP = datiOrdineAcquisto.getChild("CodiceCUP");
                        if (codiceCUP != null) {
                            aUtil.addValuesToAttachment(invoice, "2.1.2.6 CodiceCUP: " + codiceCUP.getText(), errors);
                        }
                        Element codiceCIG = datiOrdineAcquisto.getChild("CodiceCIG");
                        if (codiceCIG != null) {
                            aUtil.addValuesToAttachment(invoice, "2.1.2.7 CodiceCIG: " + codiceCIG.getText(), errors);
                        }
                    } catch (IllegalArgumentException e) {
                        EigorRuntimeException ere = new EigorRuntimeException(
                                e,
                                ErrorMessage.builder()
                                        .message(e.getMessage())
                                        .location(ErrorCode.Location.FATTPA_IN)
                                        .action(ErrorCode.Action.HARDCODED_MAP)
                                        .error(ErrorCode.Error.ILLEGAL_VALUE)
                                        .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                        .addParam(ErrorMessage.OFFENDINGITEM_PARAM, datiOrdineAcquisto.toString())
                                        .build());
                        errors.add(ConversionIssue.newError(ere));
                    }
                }
            }
        }// if fatturaelettronicabody
    }

    private void addInvoiceTotalAmountVat(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation) {

        Element rootElement = document.getRootElement();
        Element fatturaElettronicaBody = rootElement.getChild("FatturaElettronicaBody");


        if (fatturaElettronicaBody != null) {
            BG0022DocumentTotals totals;
            if (invoice.getBG0022DocumentTotals().isEmpty()) {
                totals = new BG0022DocumentTotals();
                invoice.getBG0022DocumentTotals().add(totals);
            } else {
                totals = invoice.getBG0022DocumentTotals(0);
            }

            final Element datiBeniServizi = fatturaElettronicaBody.getChild("DatiBeniServizi");
            if (datiBeniServizi != null) {

                //Fix 325
                BigDecimal totaleImposta = BigDecimal.ZERO;
                BT0110InvoiceTotalVatAmount invoiceTotalVatAmount = null;
                List<Element> datiRiepilogoList = datiBeniServizi.getChildren("DatiRiepilogo");
                for (Element datoRiepilogo : datiRiepilogoList) {


                    if (datoRiepilogo != null) {
                        Element imposta = datoRiepilogo.getChild("Imposta");


                        BigDecimal impostaD = new BigDecimal(imposta.getValue());
                        totaleImposta = totaleImposta.add(new BigDecimal(imposta.getValue()));
                    }
                }
                //END Fix 325


                List<BT0110InvoiceTotalVatAmount> invoiceTotalVatAmountList = totals.getBT0110InvoiceTotalVatAmount();


                totals.getBT0110InvoiceTotalVatAmount().add(new BT0110InvoiceTotalVatAmount(totaleImposta));
                //      invoice.getBG0022DocumentTotals().add(totals);

            }
        }
    }


    private void addInvoiceTotalAmountWithVatDefault(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation) {

        Element rootElement = document.getRootElement();
        Element fatturaElettronicaBody = rootElement.getChild("FatturaElettronicaBody");


        if (fatturaElettronicaBody != null) {
            BG0022DocumentTotals totals;
            if (invoice.getBG0022DocumentTotals().isEmpty()) {
                totals = new BG0022DocumentTotals();
                invoice.getBG0022DocumentTotals().add(totals);
            } else {
                totals = invoice.getBG0022DocumentTotals(0);
            }


            Element datiGenerali = fatturaElettronicaBody.getChild("DatiGenerali");


            final List<BT0112InvoiceTotalAmountWithVat> amountsWithVat = totals.getBT0112InvoiceTotalAmountWithVat();
            if (datiGenerali != null) {
                List<Element> datiGeneraliDocumenti = datiGenerali.getChildren();
                for (Element datiGeneraliDocumento : datiGeneraliDocumenti) {
                    if (datiGeneraliDocumento.getName().equals("DatiGeneraliDocumento")) {
                        Element importoTotaleDocumento = datiGeneraliDocumento.getChild("ImportoTotaleDocumento");
                        if (importoTotaleDocumento == null) {
                            final Element datiBeniServizi = fatturaElettronicaBody.getChild("DatiBeniServizi");
                            if (datiBeniServizi != null) {


                                final Element datiRiepilogo = datiBeniServizi.getChild("DatiRiepilogo");
                                if (datiRiepilogo != null) {

                                    final Optional<Element> imponibileImporto = Optional.fromNullable(datiRiepilogo.getChild("ImponibileImporto"));
                                    final Optional<Element> imposta = Optional.fromNullable(datiRiepilogo.getChild("Imposta"));
                                    final Function<Element, BigDecimal> function = new Function<Element, BigDecimal>() {
                                        @Override
                                        public BigDecimal apply(Element input) {
                                            return new BigDecimal(input.getText());
                                        }
                                    };

                                    final BigDecimal imponibileD = imponibileImporto.transform(function).or(BigDecimal.ZERO);
                                    final BigDecimal impostaD = imposta.transform(function).or(BigDecimal.ZERO);


                                    amountsWithVat.add(new BT0112InvoiceTotalAmountWithVat(imponibileD.add(impostaD)));
                                }
                            }
                        } else {
                            log.error("ImportoTotaleDocumento [BT-112] isn't present but ImponibileImporto [BT-109] and Imposta [BT-110] (used to calculate the default value)" +
                                    "are missing too.");
                        }


                        Element datiBollo = datiGeneraliDocumento.getChild("DatiBollo");
                        if (datiBollo != null) {

                            Element bolloVirtuale = datiBollo.getChild("BolloVirtuale");
                            Element importoBollo = datiBollo.getChild("ImportoBollo");

                            if (bolloVirtuale != null) {

                                BG0021DocumentLevelCharges bg0021 = newBg0021ForBolloVirtuale(errors, callingLocation, bolloVirtuale, importoBollo);

                                invoice.getBG0021DocumentLevelCharges().add(bg0021);
                            }
                        }

                        List<Element> datiCassaPrevidenzialeList = datiGeneraliDocumento.getChildren("DatiCassaPrevidenziale");
                        for (Element datiCassaPrevidenziale : datiCassaPrevidenzialeList) {
                            BG0021DocumentLevelCharges bg0021 = new BG0021DocumentLevelCharges();
                            invoice.getBG0021DocumentLevelCharges().add(bg0021);

                            Element tipoCassa = datiCassaPrevidenziale.getChild("TipoCassa");
                            if (tipoCassa != null) {
                                BT0105DocumentLevelChargeReasonCode bt0105 = new BT0105DocumentLevelChargeReasonCode(Untdid7161SpecialServicesCodes.ABK);
                                bg0021.getBT0105DocumentLevelChargeReasonCode().add(bt0105);
                            }

                            Element alCassa = datiCassaPrevidenziale.getChild("AlCassa");
                            if (alCassa != null) {
                                try {
                                    BT0101DocumentLevelChargePercentage bt0101 = new BT0101DocumentLevelChargePercentage(new BigDecimal(alCassa.getValue()));
                                    bg0021.getBT0101DocumentLevelChargePercentage().add(bt0101);
                                } catch (NumberFormatException e) {
                                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                                            .location(callingLocation)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .build());
                                    errors.add(ConversionIssue.newError(ere));
                                }
                            }

                            Element importoContributoCassa = datiCassaPrevidenziale.getChild("ImportoContributoCassa");
                            if (importoContributoCassa != null) {
                                try {
                                    BT0099DocumentLevelChargeAmount bt0099 = new BT0099DocumentLevelChargeAmount(new BigDecimal(importoContributoCassa.getValue()));
                                    bg0021.getBT0099DocumentLevelChargeAmount().add(bt0099);
                                } catch (NumberFormatException e) {
                                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                                            .location(callingLocation)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .build());
                                    errors.add(ConversionIssue.newError(ere));
                                }
                            }

                            Element imponibileCassa = datiCassaPrevidenziale.getChild("ImponibileCassa");
                            if (imponibileCassa != null) {
                                try {
                                    BT0100DocumentLevelChargeBaseAmount bt0100 = new BT0100DocumentLevelChargeBaseAmount(new BigDecimal(imponibileCassa.getValue()));
                                    bg0021.getBT0100DocumentLevelChargeBaseAmount().add(bt0100);
                                } catch (NumberFormatException e) {
                                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                                            .location(callingLocation)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .build());
                                    errors.add(ConversionIssue.newError(ere));
                                }
                            }

                            Element aliquotaIVA = datiCassaPrevidenziale.getChild("AliquotaIVA");
                            if (aliquotaIVA != null) {
                                try {
                                    BT0103DocumentLevelChargeVatRate bt0103 = new BT0103DocumentLevelChargeVatRate(new BigDecimal(aliquotaIVA.getValue()));
                                    bg0021.getBT0103DocumentLevelChargeVatRate().add(bt0103);
                                } catch (NumberFormatException e) {
                                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                                            .location(callingLocation)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .build());
                                    errors.add(ConversionIssue.newError(ere));
                                }

                            }

                        /*    Element ritenuta = datiCassaPrevidenziale.getChild("Ritenuta");
                            if (ritenuta != null) {
                                aUtil.addValuesToAttachment(invoice, "Ritenuta: " + ritenuta.getValue(), errors);
                            }*/

                            Element natura = datiCassaPrevidenziale.getChild("Natura");
                            if (natura != null) {
                                TypeConverter<String, Untdid5305DutyTaxFeeCategories> stringUntdid5305 =
                                        ItalianNaturaToUntdid5305DutyTaxFeeCategoriesConverter.newConverter();
                                try {
                                    BT0102DocumentLevelChargeVatCategoryCode bt0102 =
                                            new BT0102DocumentLevelChargeVatCategoryCode(stringUntdid5305.convert(natura.getText()));
                                    bg0021.getBT0102DocumentLevelChargeVatCategoryCode().add(bt0102);
                                } catch (ConversionFailedException e) {
                                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                                            .location(callingLocation)
                                            .action(ErrorCode.Action.HARDCODED_MAP)
                                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                            .build());
                                    errors.add(ConversionIssue.newError(ere));
                                }
                            }

//                            Element riferimentoAmministrazione = datiCassaPrevidenziale.getChild("RiferimentoAmministrazione");
//                            if (riferimentoAmministrazione != null) {
//                                aUtil.addValuesToAttachment(invoice, "RiferimentoAmministrazione: " + riferimentoAmministrazione.getValue(), errors);
//                            }
                        }

                        Element datiRitenuta = datiGeneraliDocumento.getChild("DatiRitenuta");
                        if (datiRitenuta != null) {
                            BG0021DocumentLevelCharges bg21ForRitenuta = newBg21ForRitenuta(invoice, errors, callingLocation, datiRitenuta);
                            invoice.getBG0021DocumentLevelCharges().add(bg21ForRitenuta);
                        }
                    }
                }
            }

            List<BG0021DocumentLevelCharges> bg0021DocumentLevelCharges = invoice.getBG0021DocumentLevelCharges();
            BigDecimal sumOfBT0021 = new BigDecimal(0);
            for (int i = 0; i < bg0021DocumentLevelCharges.size(); i++) {
                sumOfBT0021 = sumOfBT0021.add(bg0021DocumentLevelCharges.get(i).getBT0099DocumentLevelChargeAmount().get(0).getValue());
            }
            totals.getBT0108SumOfChargesOnDocumentLevel().add(new BT0108SumOfChargesOnDocumentLevel(sumOfBT0021));

            List<BG0020DocumentLevelAllowances> bg0020DocumentLevelAllowances = invoice.getBG0020DocumentLevelAllowances();
            BigDecimal sumOfBT0020 = new BigDecimal(0);
            for (int i = 0; i < bg0020DocumentLevelAllowances.size(); i++) {
                sumOfBT0020 = sumOfBT0020.add(bg0020DocumentLevelAllowances.get(i).getBT0092DocumentLevelAllowanceAmount().get(0).getValue());
            }
            totals.getBT0107SumOfAllowancesOnDocumentLevel().add(new BT0107SumOfAllowancesOnDocumentLevel(sumOfBT0020));

            BigDecimal invoiceNetTotal = totals.getBT0106SumOfInvoiceLineNetAmount().size() > 0 ? totals.getBT0106SumOfInvoiceLineNetAmount().get(0).getValue() : new BigDecimal(0);
            totals.getBT0109InvoiceTotalAmountWithoutVat().add(new BT0109InvoiceTotalAmountWithoutVat((invoiceNetTotal.subtract(sumOfBT0020)).add(sumOfBT0021)));

            final Element datiPagamento = fatturaElettronicaBody.getChild("DatiPagamento");
            if (datiPagamento != null) {
                final Element dettaglioPagamento = datiPagamento.getChild("DettaglioPagamento");
                if (dettaglioPagamento != null) {
                    final Element importoPagamento = dettaglioPagamento.getChild("ImportoPagamento");
                    if (!totals.getBT0112InvoiceTotalAmountWithVat().isEmpty()) {
                        final BigDecimal amountWithVat = totals.getBT0112InvoiceTotalAmountWithVat(0).getValue();

                        if (importoPagamento != null) {
                            String text = importoPagamento.getText();
                            try {
                                final BigDecimal importoD = new BigDecimal(text);
                                if (totals.getBT0113PaidAmount().isEmpty() || totals.getBT0113PaidAmount().get(0).getValue() != amountWithVat.subtract(importoD)) {
                                    totals.getBT0113PaidAmount().clear();
                                    totals.getBT0113PaidAmount().add(new BT0113PaidAmount(amountWithVat.subtract(importoD)));
                                }
                                totals.getBT0115AmountDueForPayment().add(new BT0115AmountDueForPayment(importoD));
                            } catch (NumberFormatException e) {
                                EigorRuntimeException ere = new EigorRuntimeException(
                                        e,
                                        ErrorMessage.builder()
                                                .message(e.getMessage())
                                                .location(callingLocation)
                                                .action(ErrorCode.Action.HARDCODED_MAP)
                                                .error(ErrorCode.Error.ILLEGAL_VALUE)
                                                .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                                                .addParam(ErrorMessage.OFFENDINGITEM_PARAM, text)
                                                .build());
                                errors.add(ConversionIssue.newError(ere));
                            }
                        } else {
                            final List<BT0113PaidAmount> paidAmounts = totals.getBT0113PaidAmount();
                            final List<BT0114RoundingAmount> roundingAmounts = totals.getBT0114RoundingAmount();
                            if (!amountsWithVat.isEmpty() && !paidAmounts.isEmpty() && !roundingAmounts.isEmpty()) {
                                final BigDecimal bt113 = totals.getBT0113PaidAmount(0).getValue();
                                final BigDecimal bt114 = totals.getBT0114RoundingAmount(0).getValue();
                                totals.getBT0115AmountDueForPayment().add(new BT0115AmountDueForPayment(amountWithVat.add(bt113).subtract(bt114)));
                            } else {
                                log.debug("One of [BT-112], [BT-113] or [BT-114] is missing. BT-112: {}, BT-113: {}, BT-114: {}", amountsWithVat.size(), paidAmounts.size(), roundingAmounts.size());
                            }
                        }
                    }

                }
            }
        }
    }

    @NotNull
    private BG0021DocumentLevelCharges newBg0021ForBolloVirtuale(List<IConversionIssue> errors, ErrorCode.Location callingLocation, Element bolloVirtuale, Element importoBollo) {

        BG0021DocumentLevelCharges bg0021 = new BG0021DocumentLevelCharges();

        BT0099DocumentLevelChargeAmount bt0099 = new BT0099DocumentLevelChargeAmount(BigDecimal.ZERO);
        bg0021.getBT0099DocumentLevelChargeAmount().add(bt0099);

        BT0101DocumentLevelChargePercentage bt0101 = new BT0101DocumentLevelChargePercentage(BigDecimal.ZERO);
        bg0021.getBT0101DocumentLevelChargePercentage().add(bt0101);

        BT0102DocumentLevelChargeVatCategoryCode bt0102 = new BT0102DocumentLevelChargeVatCategoryCode(Untdid5305DutyTaxFeeCategories.E);
        bg0021.getBT0102DocumentLevelChargeVatCategoryCode().add(bt0102);

        BT0103DocumentLevelChargeVatRate bt103 = new BT0103DocumentLevelChargeVatRate(BigDecimal.ZERO);
        bg0021.getBT0103DocumentLevelChargeVatRate().add(bt103);

        String bolloVirtualeText = bolloVirtuale.getText();
        if ("SI".equals(bolloVirtualeText)) {
            BT0104DocumentLevelChargeReason bt0104 = new BT0104DocumentLevelChargeReason(bolloVirtualeText);
            bg0021.getBT0104DocumentLevelChargeReason().add(bt0104);
        } else {
            BT0104DocumentLevelChargeReason bt0104 = new BT0104DocumentLevelChargeReason("BT-100 represents Bollo amount, Bollo virtuale assolto ai sensi dell’ art. 6, c. 2 del DM 17 giugno 2014");
            bg0021.getBT0104DocumentLevelChargeReason().add(bt0104);
        }

        if (importoBollo != null) {
            try {
                BT0100DocumentLevelChargeBaseAmount bt0100 = new BT0100DocumentLevelChargeBaseAmount(new BigDecimal(importoBollo.getText()));
                bg0021.getBT0100DocumentLevelChargeBaseAmount().add(bt0100);
            } catch (NumberFormatException e) {
                EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                        .location(callingLocation)
                        .action(ErrorCode.Action.HARDCODED_MAP)
                        .error(ErrorCode.Error.ILLEGAL_VALUE)
                        .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                        .build());
                errors.add(ConversionIssue.newError(ere));
            }
        }
        return bg0021;
    }

    @NotNull
    private BG0021DocumentLevelCharges newBg21ForRitenuta(BG0000Invoice invoice, List<IConversionIssue> errors, ErrorCode.Location callingLocation, Element datiRitenuta) {

        BG0021DocumentLevelCharges bg0021 = new BG0021DocumentLevelCharges();

        {
            BT0099DocumentLevelChargeAmount bt0099 = new BT0099DocumentLevelChargeAmount(BigDecimal.ZERO);
            bg0021.getBT0099DocumentLevelChargeAmount().add(bt0099);
        }

        {
            Element importoRitenuta = datiRitenuta.getChild("ImportoRitenuta");
            if (importoRitenuta != null) {
                try {
                    BigDecimal importoRitenutaValue = new BigDecimal(importoRitenuta.getText());

                    BT0100DocumentLevelChargeBaseAmount bt0100 = new BT0100DocumentLevelChargeBaseAmount(importoRitenutaValue);
                    bg0021.getBT0100DocumentLevelChargeBaseAmount().add(bt0100);
                    BT0113PaidAmount bt0113 = new BT0113PaidAmount(importoRitenutaValue);
                    invoice.getBG0022DocumentTotals(0).getBT0113PaidAmount().add(bt0113);
                } catch (NumberFormatException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                            .location(callingLocation)
                            .action(ErrorCode.Action.HARDCODED_MAP)
                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                            .build());
                    errors.add(ConversionIssue.newError(ere));
                }
            }
        }

        {
            Element aliquotaRitenuta = datiRitenuta.getChild("AliquotaRitenuta");
            if (aliquotaRitenuta != null) {
                BT0101DocumentLevelChargePercentage bt0101 = null;
                try {
                    bt0101 = new BT0101DocumentLevelChargePercentage(new BigDecimal(aliquotaRitenuta.getText()));
                } catch (NumberFormatException e) {
                    EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage())
                            .location(callingLocation)
                            .action(ErrorCode.Action.HARDCODED_MAP)
                            .error(ErrorCode.Error.ILLEGAL_VALUE)
                            .addParam(ErrorMessage.SOURCEMSG_PARAM, e.getMessage())
                            .build());
                    errors.add(ConversionIssue.newError(ere));
                }
                bg0021.getBT0101DocumentLevelChargePercentage().add(bt0101);
            }
        }

        {
            BT0102DocumentLevelChargeVatCategoryCode bt0102 = new BT0102DocumentLevelChargeVatCategoryCode(Untdid5305DutyTaxFeeCategories.E);
            bg0021.getBT0102DocumentLevelChargeVatCategoryCode().add(bt0102);
        }

        {
            BT0103DocumentLevelChargeVatRate bt0103 = new BT0103DocumentLevelChargeVatRate(BigDecimal.ZERO);
            bg0021.getBT0103DocumentLevelChargeVatRate().add(bt0103);
        }

        {
            Element tipoRitenuta = datiRitenuta.getChild("TipoRitenuta");
            Element causalePagamento = datiRitenuta.getChild("CausalePagamento");
            BT0104DocumentLevelChargeReason bt0104 = null;
            if (tipoRitenuta != null) {
                if (causalePagamento != null) {
                    bt0104 = new BT0104DocumentLevelChargeReason(String.format("%s %s", tipoRitenuta.getText(), causalePagamento.getText()));
                } else {
                    bt0104 = new BT0104DocumentLevelChargeReason(tipoRitenuta.getText());
                }
            } else if (causalePagamento != null) {
                bt0104 = new BT0104DocumentLevelChargeReason(causalePagamento.getText());
            }
            if (bt0104 != null) {
                bg0021.getBT0104DocumentLevelChargeReason().add(bt0104);
            }
        }

        if (invoice.getBT0020PaymentTerms().isEmpty()) {
            BT0020PaymentTerms bt0020 = new BT0020PaymentTerms("BT-113 represents Withholding tax amount");
            invoice.getBT0020PaymentTerms().add(bt0020);
        } else {
            BT0020PaymentTerms bt0020 =
                    new BT0020PaymentTerms(invoice.getBT0020PaymentTerms().get(0).getValue() + ", BT-113 represents Withholding tax amount");
            invoice.getBT0020PaymentTerms().set(0, bt0020);
        }
        return bg0021;
    }
}
