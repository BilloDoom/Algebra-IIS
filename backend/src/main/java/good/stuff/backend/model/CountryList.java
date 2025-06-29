package good.stuff.backend.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@XmlRootElement(name = "Countries")
public class CountryList {

    private List<Country> countries;

    @XmlElement(name = "Country")
    public List<Country> getCountries() {
        return countries;
    }

}
