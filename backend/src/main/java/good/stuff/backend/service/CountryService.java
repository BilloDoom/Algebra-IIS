package good.stuff.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thaiopensource.validate.ValidationDriver;
import good.stuff.backend.model.Country;
import good.stuff.backend.model.CountryList;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CountryService {

    private final Schema schema;

    public CountryService() throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File xsdFile = new ClassPathResource("schema/country.xsd").getFile();
        this.schema = factory.newSchema(xsdFile);
    }

    public void validateWithXsd(String xml) throws Exception {
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml)));
    }

    public void validateWithRng(String xml) throws Exception {
        InputStream rngInput = getClass().getClassLoader().getResourceAsStream("schema/country.rng");
        if (rngInput == null) throw new FileNotFoundException("RNG schema not found.");

        ValidationDriver driver = new ValidationDriver();
        if (!driver.loadSchema(new InputSource(rngInput))) {
            throw new SAXException("Failed to load RNG schema.");
        }

        if (!driver.validate(new InputSource(new StringReader(xml)))) {
            throw new SAXException("XML validation failed against RNG.");
        }
    }

    public void saveXmlToDisk(String xml) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(CountryList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        CountryList newCountryList = (CountryList) unmarshaller.unmarshal(new StringReader(xml));

        File countriesDir = new File("data/countries");
        if (!countriesDir.exists() && !countriesDir.mkdirs()) {
            throw new IOException("Failed to create directory for country XMLs.");
        }

        File countriesFile = new File(countriesDir, "countries.xml");
        List<Country> countriesList;

        if (countriesFile.exists()) {
            countriesList = getAllCountriesFromFile(countriesFile);
        } else {
            countriesList = new ArrayList<>();
        }

        for (Country newCountry : newCountryList.getCountries()) {
            boolean exists = countriesList.stream()
                    .anyMatch(c -> c.getCode() != null && c.getCode().equals(newCountry.getCode()));
            if (!exists) {
                countriesList.add(newCountry);
            }
        }

        CountryList countriesWrapper = new CountryList(countriesList);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        try (FileWriter writer = new FileWriter(countriesFile)) {
            marshaller.marshal(countriesWrapper, writer);
        }

        System.out.println("Saved countries to: " + countriesFile.getAbsolutePath());
    }

    private List<Country> getAllCountriesFromFile(File countriesFile) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(CountryList.class);
        try (FileReader reader = new FileReader(countriesFile)) {
            CountryList countryList = (CountryList) context.createUnmarshaller().unmarshal(reader);
            return countryList.getCountries() != null ? countryList.getCountries() : new ArrayList<>();
        }
    }

    private Country convertXmlToCountry(String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Country.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Country) unmarshaller.unmarshal(new StringReader(xml));
    }
}
