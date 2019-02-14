package it.infocert.eigor.converter.commons.cen2peppol;

import it.infocert.eigor.api.CustomMapping;
import it.infocert.eigor.api.IConversionIssue;
import it.infocert.eigor.api.configuration.EigorConfiguration;
import it.infocert.eigor.api.errors.ErrorCode;
import it.infocert.eigor.model.core.datatypes.Identifier;
import it.infocert.eigor.model.core.enums.Iso31661CountryCodes;
import it.infocert.eigor.model.core.model.BG0000Invoice;
import it.infocert.eigor.model.core.model.BG0007Buyer;
import it.infocert.eigor.model.core.model.BG0008BuyerPostalAddress;
import it.infocert.eigor.model.core.model.BG0009BuyerContact;
import it.infocert.eigor.model.core.model.BT0044BuyerName;
import it.infocert.eigor.model.core.model.BT0048BuyerVatIdentifier;
import it.infocert.eigor.model.core.model.BT0049BuyerElectronicAddressAndSchemeIdentifier;
import it.infocert.eigor.model.core.model.BT0050BuyerAddressLine1;
import it.infocert.eigor.model.core.model.BT0052BuyerCity;
import it.infocert.eigor.model.core.model.BT0053BuyerPostCode;
import it.infocert.eigor.model.core.model.BT0055BuyerCountryCode;
import it.infocert.eigor.model.core.model.BT0056BuyerContactPoint;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.util.List;

import static it.infocert.eigor.model.core.InvoiceUtils.evalExpression;

public class AccountCustomerParty implements CustomMapping<Document> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final String ACCOUNTING_CUSTOMER_PARTY = "AccountingCustomerParty";
	private final String PARTY = "Party";
	private final String Endpoint = "EndpointID";

	@Override
	public void map(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation, EigorConfiguration eigorConfiguration) {

		final Element root = document.getRootElement();
		final Element partyElm;
		final Element supplier = root.getChild(ACCOUNTING_CUSTOMER_PARTY);
		Element accountSupplierPartyElm = new Element(ACCOUNTING_CUSTOMER_PARTY);

		if (root != null) {
			if (!invoice.getBG0007Buyer().isEmpty()) {
				
				
				BG0007Buyer buyer = invoice.getBG0007Buyer(0);
//				 if (!buyer.getBT0048BuyerVatIdentifier().isEmpty() || !buyer.getBT0049BuyerElectronicAddressAndSchemeIdentifier().isEmpty()) {
				if (supplier == null) {

					partyElm = new Element(PARTY);
					//            accountSupplierPartyElm.addContent(partyElm);

				} else {
					partyElm = supplier.getChild(PARTY);
					//            accountSupplierPartyElm.addContent(partyElm);
				}

//				BG0007Buyer buyer = evalExpression( () -> invoice.getBG0007Buyer(0) );

				if(buyer!=null) {

					String identifierText;
					String identificationSchemaStr;

					if (buyer.getBT0049BuyerElectronicAddressAndSchemeIdentifier().isEmpty()) {
						identifierText = "NA";
						identificationSchemaStr = "0130";
					} else {
						BT0049BuyerElectronicAddressAndSchemeIdentifier bt49 = buyer.getBT0049BuyerElectronicAddressAndSchemeIdentifier(0);
						identifierText = bt49.getValue().getIdentifier();
						identificationSchemaStr = bt49.getValue().getIdentificationSchema();
					}

					Element endpointElm = new Element(Endpoint);
					endpointElm.setText(identifierText);
					endpointElm.setAttribute("schemeID", identificationSchemaStr);
					partyElm.addContent(endpointElm);
					accountSupplierPartyElm.addContent(partyElm);
					root.addContent(accountSupplierPartyElm);
					
					
	                 if (!buyer.getBT0046BuyerIdentifierAndSchemeIdentifier().isEmpty()) {
	                        final Identifier identifier = buyer.getBT0046BuyerIdentifierAndSchemeIdentifier(0).getValue();
	                        final Element partyIdentification = Optional.fromNullable(partyElm.getChild("PartyIdentification")).or(new Supplier<Element>() {
	                            @Override
	                            public Element get() {
	                                final Element p = new Element("PartyIdentification");
	                                partyElm.addContent(p);
	                                return p;
	                            }
	                        });

	                        final Element id = Optional.fromNullable(partyIdentification.getChild("ID")).or(new Supplier<Element>() {
	                            @Override
	                            public Element get() {
	                                final Element i = new Element("ID");
	                                partyIdentification.addContent(i);
	                                return i;
	                            }
	                        });

	                        final String ide = identifier.getIdentifier();
	                        final String schema = identifier.getIdentificationSchema();
	                        id.setText(String.format("%s:%s", schema, ide));
//	                        if (schema != null) id.setAttribute("schemeID", schema);
	                    }

	                    if (!buyer.getBG0008BuyerPostalAddress().isEmpty()) {
	                        final BG0008BuyerPostalAddress bg0008 = buyer.getBG0008BuyerPostalAddress(0);

	                        Element postalAddress = partyElm.getChild("PostalAddress");
	                        if (postalAddress == null) {
	                            postalAddress = new Element("PostalAddress");
	                            partyElm.addContent(postalAddress);
	                        }

	                        if (!bg0008.getBT0050BuyerAddressLine1().isEmpty()) {
	                            BT0050BuyerAddressLine1 bt0050 = bg0008.getBT0050BuyerAddressLine1(0);
	                            Element streetName = new Element("StreetName");
	                            streetName.setText(bt0050.getValue());
	                            postalAddress.addContent(streetName);
	                        }

	                        if (!bg0008.getBT0052BuyerCity().isEmpty()) {
	                            BT0052BuyerCity bt0052 = bg0008.getBT0052BuyerCity(0);
	                            Element cityName = new Element("CityName");
	                            cityName.setText(bt0052.getValue());
	                            postalAddress.addContent(cityName);
	                        }

	                        if (!bg0008.getBT0053BuyerPostCode().isEmpty()) {
	                            BT0053BuyerPostCode bt0053 = bg0008.getBT0053BuyerPostCode(0);
	                            Element postalZone = new Element("PostalZone");
	                            postalZone.setText(bt0053.getValue());
	                            postalAddress.addContent(postalZone);
	                        }

	                        if (!bg0008.getBT0055BuyerCountryCode().isEmpty()) {
	                            BT0055BuyerCountryCode bt0055 = bg0008.getBT0055BuyerCountryCode(0);
	                            Element country = postalAddress.getChild("Country");
	                            if (country == null) {
	                                country = new Element("Country");
	                                postalAddress.addContent(country);
	                            }
	                            Element identificationCode = new Element("IdentificationCode");
	                            Iso31661CountryCodes code = bt0055.getValue();
	                            identificationCode.setText(code.name());
	                            country.addContent(identificationCode);
	                        }
	                    }

	                    if (!buyer.getBT0048BuyerVatIdentifier().isEmpty()) {
	                        BT0048BuyerVatIdentifier buyerVatIdentifier = buyer.getBT0048BuyerVatIdentifier(0);
	                        Element companyID = new Element("CompanyID");
	                        Identifier identifier = buyerVatIdentifier.getValue();
//	                        if (identifier.getIdentificationSchema() != null) {
//	                            companyID.setAttribute("schemeID", identifier.getIdentificationSchema());
//	                        }
//	                        if (identifier.getIdentifier() != null) {
//	                            companyID.addContent(identifier.getIdentifier());
//	                        }
	                        String companyIDValue = "";
	                        if (identifier.getIdentificationSchema() != null) {
	                            companyIDValue += identifier.getIdentificationSchema();
	                        }
	                        if (identifier.getIdentifier() != null) {
	                            companyIDValue += identifier.getIdentifier();
	                        }
	                        companyID.addContent(companyIDValue);
	                        Element partyTaxScheme = partyElm.getChild("PartyTaxScheme");
	                        if (partyTaxScheme == null) {
	                            partyTaxScheme = new Element("PartyTaxScheme");
	                            partyElm.addContent(partyTaxScheme);
	                        }
	                        partyTaxScheme.addContent(companyID);
	                        Element taxScheme = new Element("TaxScheme");
	                        Element taxSchemeId = new Element("ID");
	                        taxSchemeId.setText("VAT");
	                        taxScheme.addContent(taxSchemeId);
	                        partyTaxScheme.addContent(taxScheme);

	                    }

	                    Element partyLegalEntity = partyElm.getChild("PartyLegalEntity");
	                    if (partyLegalEntity == null) {
	                        partyLegalEntity = new Element("PartyLegalEntity");
	                        partyElm.addContent(partyLegalEntity);
	                    }

	                    Element registrationName = new Element("RegistrationName");
	                    if (!buyer.getBG0009BuyerContact().isEmpty()) {
	                        BG0009BuyerContact bg0009 = buyer.getBG0009BuyerContact(0);
	                        if (!bg0009.getBT0056BuyerContactPoint().isEmpty()) {
	                            BT0056BuyerContactPoint bt0056 = bg0009.getBT0056BuyerContactPoint(0);
	                            registrationName.setText(bt0056.getValue());
	                            partyLegalEntity.addContent(registrationName);
	                        }
	                    } else if (!buyer.getBT0044BuyerName().isEmpty()) {
	                        BT0044BuyerName bt0044 = buyer.getBT0044BuyerName(0);
	                        registrationName.setText(bt0044.getValue());
	                        partyLegalEntity.addContent(registrationName);
	                    }
					
					
				}
		//	}
		}
	}
}
}