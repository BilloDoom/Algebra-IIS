package good.stuff.backend.soap;

import good.stuff.backend.model.SearchResult;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import java.util.List;


@WebService(
        targetNamespace = "http://soap.backend.stuff.good/",
        name = "CountrySearchService",
        serviceName = "CountrySearchService"
)
public interface CountrySearchService {

    @WebMethod
    @WebResult(name = "SearchResult")
    SearchResult searchCountriesByTerm(@WebParam(name = "term") String term);
}
