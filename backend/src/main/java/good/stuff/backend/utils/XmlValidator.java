package good.stuff.backend.utils;

import good.stuff.backend.model.CountryList;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.util.ValidationEventCollector;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class XmlValidator {
    public static void main(String[] args) throws JAXBException, SAXException {
        File xmlFile = new File("request.xml");   // your XML file
        File xsdFile = new File("schema.xsd");    // your XSD file

        JAXBContext jc = JAXBContext.newInstance(CountryList.class); // Replace with your JAXB root class
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = sf.newSchema(xsdFile);
        unmarshaller.setSchema(schema);

        ValidationEventCollector vec = new ValidationEventCollector();
        unmarshaller.setEventHandler(vec);

        try {
            unmarshaller.unmarshal(xmlFile);
        } catch (JAXBException e) {
            System.out.println("XML not valid: " + e.getLinkedException().getMessage());
        }

        if (vec.hasEvents()) {
            for (ValidationEvent event : vec.getEvents()) {
                System.out.println("Validation warning/error: " + event.getMessage());
            }
        } else {
            System.out.println("XML is valid against the XSD.");
        }
    }
}

