package good.stuff.backend.controller;

import good.stuff.backend.model.Country;
import good.stuff.backend.service.CountryXmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/xml-countries")
public class CountryXmlController {

    private final CountryXmlService countryXmlService;

    @Autowired
    public CountryXmlController(CountryXmlService countryXmlService) {
        this.countryXmlService = countryXmlService;
    }

    @GetMapping
    public List<Country> getAll() {
        return countryXmlService.getAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Country> getById(@PathVariable String code) {
        return countryXmlService.getById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Country country) {
        try {
            Country created = countryXmlService.create(country);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<Country> update(@PathVariable String code, @RequestBody Country country) {
        return countryXmlService.update(code, country)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (countryXmlService.delete(code)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
