package good.stuff.backend.service;


import good.stuff.backend.model.weather.CityWeather;
import good.stuff.backend.model.weather.WeatherData;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final String DHMZ_URL = "https://vrijeme.hr/hrvatska_n.xml";

    public List<CityWeather> getWeatherByCityName(String cityName) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String xmlData = restTemplate.getForObject(DHMZ_URL, String.class);

        JAXBContext jaxbContext = JAXBContext.newInstance(WeatherData.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        WeatherData weatherData = (WeatherData) unmarshaller.unmarshal(new StringReader(xmlData));

        return weatherData.getCities().stream()
                .filter(city -> city.getCityName().toLowerCase().contains(cityName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
