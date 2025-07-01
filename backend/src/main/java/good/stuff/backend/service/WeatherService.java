package good.stuff.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import good.stuff.backend.model.weather.CityWeather;
import good.stuff.backend.model.weather.WeatherData;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final String DHMZ_URL = "https://vrijeme.hr/hrvatska_n.xml";

    public Map<String, String> getTemperature(String cityName) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String xmlData = restTemplate.getForObject(DHMZ_URL, String.class);

        JAXBContext jaxbContext = JAXBContext.newInstance(WeatherData.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        WeatherData weatherData = (WeatherData) unmarshaller.unmarshal(new StringReader(xmlData));

        return weatherData.getCities().stream()
                .filter(city -> city.getCityName() != null
                        && city.getTemperature() != null
                        && city.getCityName().toLowerCase().contains(cityName.toLowerCase()))
                .collect(Collectors.toMap(
                        CityWeather::getCityName,
                        CityWeather::getTemperature,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
