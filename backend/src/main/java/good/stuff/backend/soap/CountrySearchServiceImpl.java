package good.stuff.backend.soap;

import good.stuff.backend.model.Country;
import good.stuff.backend.model.CountryList;
import good.stuff.backend.model.SearchResult;
import jakarta.jws.WebService;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "good.stuff.backend.soap.CountrySearchService")
public class CountrySearchServiceImpl implements CountrySearchService {

    private final File xmlFile = new File("data/countries/countries.xml");

    @Override
    public SearchResult searchCountriesByTerm(String term) {
        try {
            List<String> validationErrors = validateXmlWithJaxb(xmlFile);
            if (!validationErrors.isEmpty()) {
                return new SearchResult(null, validationErrors);
            }

            List<Country> filteredCountries = filterXmlWithXPathAsObjects(xmlFile, term);

            CountryList filteredCountryList = new CountryList(filteredCountries);

            return new SearchResult(filteredCountryList, List.of());

        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResult(null, List.of("Error: " + e.getMessage()));
        }
    }


    private List<Country> filterXmlWithXPathAsObjects(File xmlFile, String term) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(xmlFile);

        XPath xpath = XPathFactory.newInstance().newXPath();

        XPathExpression expr = xpath.compile(
                "//Country[contains(translate(Name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + term.toLowerCase() + "')"
                        + " or contains(translate(Code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + term.toLowerCase() + "')]"
        );

        org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        JAXBContext jc = JAXBContext.newInstance(Country.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Country country = (Country) unmarshaller.unmarshal(nodes.item(i));
            countries.add(country);
        }

        return countries;
    }

    private List<String> validateXmlWithJaxb(File xmlFile) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(CountryList.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        try (InputStream xsdStream = getClass().getClassLoader().getResourceAsStream("schema/country.xsd")) {
            if (xsdStream == null) {
                throw new RuntimeException("Could not find country.xsd in classpath 'schema/' folder");
            }
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new StreamSource(xsdStream));
            unmarshaller.setSchema(schema);
        }

        List<String> errors = new ArrayList<>();

        unmarshaller.setEventHandler(event -> {
            errors.add(event.getMessage());
            return true; // collect all errors
        });

        unmarshaller.unmarshal(xmlFile);

        return errors;
    }
}
