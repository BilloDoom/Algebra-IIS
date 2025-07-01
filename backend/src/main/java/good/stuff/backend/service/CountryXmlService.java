package good.stuff.backend.service;

import good.stuff.backend.model.Country;
import good.stuff.backend.model.CountryList;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryXmlService {

    private static final String FILE_PATH = "data/countries/countries.xml";

    private CountryList countries;

    @PostConstruct
    public void init() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                // Initialize empty if file missing
                countries = new CountryList();
                saveToFile();
            } else {
                JAXBContext context = JAXBContext.newInstance(CountryList.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                countries = (CountryList) unmarshaller.unmarshal(file);
            }
        } catch (JAXBException e) {
            throw new RuntimeException("Error reading XML file", e);
        }
    }

    public List<Country> getAll() {
        return countries.getCountries();
    }

    public Optional<Country> getById(String code) {
        return countries.getCountries().stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public Country create(Country country) {
        // Avoid duplicate codes
        if (getById(country.getCode()).isPresent()) {
            throw new RuntimeException("Country with code " + country.getCode() + " already exists");
        }
        countries.getCountries().add(country);
        saveToFile();
        return country;
    }

    public Optional<Country> update(String code, Country updatedCountry) {
        Optional<Country> existingOpt = getById(code);
        if (existingOpt.isPresent()) {
            Country existing = existingOpt.get();
            existing.setName(updatedCountry.getName());
            existing.setUrlCode(updatedCountry.getUrlCode());
            saveToFile();
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    public boolean delete(String code) {
        List<Country> filtered = countries.getCountries().stream()
                .filter(c -> !c.getCode().equalsIgnoreCase(code))
                .collect(Collectors.toList());

        if (filtered.size() == countries.getCountries().size()) {
            // no removal happened
            return false;
        }

        countries.setCountries(filtered);
        saveToFile();
        return true;
    }

    private void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(CountryList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(countries, new File(FILE_PATH));
        } catch (JAXBException e) {
            throw new RuntimeException("Error saving XML file", e);
        }
    }
}
