package good.stuff.backend.soap;

import jakarta.xml.ws.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapConfig {

    @Bean
    public Endpoint countrySearchEndpoint() {
        Endpoint endpoint = Endpoint.create(new CountrySearchServiceImpl());
        endpoint.publish("http://localhost:9090/CountrySearchService");
        return endpoint;
    }
}
