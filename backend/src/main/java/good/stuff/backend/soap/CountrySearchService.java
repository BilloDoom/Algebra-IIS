package good.stuff.backend.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import java.util.List;

@WebService
public interface CountrySearchService {

    @WebMethod
    @WebResult(name = "Country")
    List<String> searchCountriesByTerm(@WebParam(name = "term") String term);
}
