package good.stuff.backend.controller;

import good.stuff.backend.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping(path = "/xsd", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> uploadXmlWithXsd(@RequestBody String xmlContent) {
        try {
            countryService.validateWithXsd(xmlContent);
            countryService.saveXmlToDisk(xmlContent);
            return ResponseEntity.ok("Country validated with XSD and saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("XSD Validation Error: " + e.getMessage());
        }
    }

    @PostMapping(path = "/rng", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> uploadXmlWithRng(@RequestBody String xmlContent) {
        try {
            countryService.validateWithRng(xmlContent);
            countryService.saveXmlToDisk(xmlContent);
            return ResponseEntity.ok("Country validated with RNG and saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("RNG Validation Error: " + e.getMessage());
        }
    }
}
