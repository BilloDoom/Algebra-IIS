package good.stuff.frontend;

public class Country {
    private String code;
    private String urlCode;
    private String name;

    public Country() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getUrlCode() { return urlCode; }
    public void setUrlCode(String urlCode) { this.urlCode = urlCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
