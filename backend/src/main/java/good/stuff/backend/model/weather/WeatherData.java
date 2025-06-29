package good.stuff.backend.model.weather;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "Hrvatska")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeatherData {

    @XmlElement(name = "Grad")
    private List<CityWeather> cities;
}
