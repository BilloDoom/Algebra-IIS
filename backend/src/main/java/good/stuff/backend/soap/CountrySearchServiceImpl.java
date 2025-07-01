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
            fetchAndSaveCountryData(term);
            return filterXmlWithXPath(xmlFile,term);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error: " + e.getMessage());
        }
    }

    private void fetchAndSaveCountryData(String term) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = "https://localhost:8080/countries?search=" + term;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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

    private List<String> filterXmlWithXPath(File xmlFile, String term) throws Exception {
        List<String> results = new ArrayList<>();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(xmlFile);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//Country[contains(translate(Name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + term.toLowerCase() + "') or contains(translate(Code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + term.toLowerCase() + "')]");

        org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            results.add(nodeToString(node));
        }

        return results;
    }

    private String nodeToString(org.w3c.dom.Node node) throws Exception {
        javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(node), new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }

}
