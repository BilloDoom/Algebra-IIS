package good.stuff.backend.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@XmlRootElement(name = "Country")
@XmlAccessorType(XmlAccessType.FIELD)
public class Country {
    @XmlElement
    private String Code;

    @XmlElement
    private String UrlCode;

    @XmlElement
    private String Name;
}

