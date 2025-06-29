package good.stuff.backend.model.weather;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class CityWeather {

    @XmlElement(name = "GradIme")
    private String cityName;

    @XmlElement(name = "Temp")
    private String temperature;
}