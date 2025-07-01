package good.stuff.backend.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@XmlRootElement(name = "SearchResult")
public class SearchResult {

    private CountryList countries;

    private List<String> validationErrors;

    @XmlElement(name = "Countries")
    public CountryList getCountries() {
        return countries;
    }

    @XmlElement(name = "ValidationError")
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
