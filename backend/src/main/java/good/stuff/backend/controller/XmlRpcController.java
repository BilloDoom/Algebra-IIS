package good.stuff.backend.controller;

import good.stuff.backend.service.WeatherService;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;

@RestController
public class XmlRpcController {

    private final WeatherService weatherService;
    private final SAXBuilder saxBuilder = new SAXBuilder();
    private final XMLOutputter xmlOutputter;

    public XmlRpcController(WeatherService weatherService) {
        this.weatherService = weatherService;

        Format fmt = Format.getRawFormat()
                .setOmitDeclaration(false)
                .setEncoding("UTF-8");
        this.xmlOutputter = new XMLOutputter(fmt);
    }

    @PostMapping(value = "/xmlrpc", consumes = "text/xml", produces = "text/xml")
    public void handleXmlRpc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String xmlIn;
        try (InputStream is = req.getInputStream()) {
            xmlIn = new String(is.readAllBytes(), resp.getCharacterEncoding());
        }

        Document reqDoc = saxBuilder.build(new StringReader(xmlIn));
        Element stringEl = reqDoc.getRootElement()
                .getChild("params")
                .getChild("param")
                .getChild("value")
                .getChild("string");
        String cityName = stringEl.getText().trim();

        Map<String, String> results = weatherService.getTemperature(cityName);

        Document resDoc = new Document();
        Element methodResponse = new Element("methodResponse");
        resDoc.setRootElement(methodResponse);

        Element params = new Element("params");
        methodResponse.addContent(params);

        Element param = new Element("param");
        params.addContent(param);

        Element value = new Element("value");
        param.addContent(value);

        Element struct = new Element("struct");
        value.addContent(struct);

        for (Map.Entry<String, String> e : results.entrySet()) {
            Element member = new Element("member");
            struct.addContent(member);

            member.addContent(new Element("name").setText(e.getKey()));

            Element memberValue = new Element("value");
            memberValue.addContent(new Element("string").setText(e.getValue()));
            member.addContent(memberValue);
        }

        resp.setStatus(200);
        resp.setContentType("text/xml;charset=UTF-8");
        try (OutputStream os = resp.getOutputStream()) {
            xmlOutputter.output(resDoc, os);
        }
    }
}
