package good.stuff.backend.soap;

import good.stuff.backend.model.Country;
import good.stuff.backend.model.CountryList;
import jakarta.jws.WebService;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "good.stuff.backend.soap.CountrySearchService")
public class CountrySearchServiceImpl implements CountrySearchService {

    private final File xmlFile = new File("data/countries.xml");

    @Override
    public List<String> searchCountriesByTerm(String term) {
        try {
            // 1. Fetch country data from REST API filtered by term
            fetchAndSaveCountryData(term);

            // 2. Validate XML with XSD from resources folder, parse to JAXB objects
            List<Country> validCountries = validateAndParseCountries(xmlFile, "countries.xsd");

            // 3. Filter valid countries by term (case-insensitive)
            List<String> filteredXmlStrings = new ArrayList<>();
            for (Country c : validCountries) {
                if (c.getCode().toLowerCase().contains(term.toLowerCase()) ||
                        c.getName().toLowerCase().contains(term.toLowerCase())) {
                    // Convert Country object back to XML snippet string
                    filteredXmlStrings.add(convertCountryToXml(c));
                }
            }
            return filteredXmlStrings;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error: " + e.getMessage());
        }
    }

    private void fetchAndSaveCountryData(String term) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = "https://your-rest-api.com/countries?search=" + term;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", "YOUR_API_KEY") // if needed
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch countries: HTTP " + response.statusCode());
        }

        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(response.body());
        }
    }

    private List<Country> validateAndParseCountries(File xmlFile, String xsdResourcePath) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(CountryList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        // Load XSD schema from resources folder
        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try (InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(xsdResourcePath)) {
            if (xsdStream == null) {
                throw new RuntimeException("XSD schema file not found in resources: " + xsdResourcePath);
            }
            Schema schema = sf.newSchema(new javax.xml.transform.stream.StreamSource(xsdStream));
            unmarshaller.setSchema(schema);
        }

        unmarshaller.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(ValidationEvent event) {
                System.err.println("Validation error: " + event.getMessage());
                return false;  // stop unmarshalling on error
            }
        });

        CountryList countries = (CountryList) unmarshaller.unmarshal(xmlFile);
        return countries.getCountries();
    }

    private String convertCountryToXml(Country country) throws Exception {
        // Simple marshalling of single Country object to XML string
        JAXBContext context = JAXBContext.newInstance(Country.class);
        java.io.StringWriter sw = new java.io.StringWriter();
        context.createMarshaller().marshal(country, sw);
        return sw.toString();
    }
}
