package good.stuff.backend.service;

import good.stuff.backend.model.Country;
import good.stuff.backend.model.CountryList;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CountryDataSeeder {

    private final File countriesFile = new File("data/countries/countries.xml");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String RAPIDAPI_KEY = "04d9e6b491msh3b7e5870403e7cfp10839bjsnee2a1afd6966";

    @PostConstruct
    public void seedData() throws Exception {
        if (countriesFile.exists()) {
            System.out.println("Countries file already exists, skipping seeding.");
            return;
        }

        File outputDir = countriesFile.getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("Could not create directory: " + outputDir.getAbsolutePath());
        }

        String[] exampleDomains = {"google.com", "amazon.com", "facebook.com"};

        JAXBContext jaxbContext = JAXBContext.newInstance(CountryList.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        HttpClient client = HttpClient.newHttpClient();
        List<Country> countryList = new ArrayList<>();
        Set<String> addedCountryCodes = new HashSet<>();

        for (String domain : exampleDomains) {
            String url = "https://similarweb-traffic.p.rapidapi.com/traffic?domain=" + domain;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-rapidapi-key", RAPIDAPI_KEY)
                    .header("x-rapidapi-host", "similarweb-traffic.p.rapidapi.com")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch data for domain " + domain + ": HTTP " + response.statusCode());
                continue;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode topCountries = root.path("Countries");

            if (!topCountries.isArray() || topCountries.isEmpty()) {
                System.err.println("No country data found in response for domain: " + domain);
                continue;
            }

            for (JsonNode countryNode : topCountries) {
                String code = countryNode.path("Code").asText(null);
                String urlCode = countryNode.path("UrlCode").asText(null);
                String name = countryNode.path("Name").asText(null);

                if (code == null || name == null || addedCountryCodes.contains(code)) {
                    continue;
                }

                countryList.add(new Country(code, urlCode, name));
                addedCountryCodes.add(code);
                System.out.println("Added country: " + name);
            }
        }

        CountryList countries = new CountryList(countryList);
        try (FileWriter writer = new FileWriter(countriesFile)) {
            marshaller.marshal(countries, writer);
            System.out.println("Saved countries to: " + countriesFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to write countries XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
