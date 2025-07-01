package good.stuff.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static good.stuff.frontend.Utils.prettyFormatXml;

public class MainController {

    @FXML
    private Button logoutButton;

    @FXML
    private TextField txtfWeather;

    @FXML
    private TextArea txtaWeather;

    @FXML
    private Button searchWeather;

    @FXML
    private CheckBox instaSearchWeather;

    @FXML
    private Button fetchButton;

    @FXML
    private Button createButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private RadioButton xsdRadio;

    @FXML
    private RadioButton rngRadio;

    @FXML
    private ToggleGroup schemaToggleGroup;

    @FXML
    private Button sendRawXmlButton;

    @FXML
    private TextArea textAreaRawXml;

    @FXML
    private ListView<Country> countryListView;

    @FXML
    private TextField txtCode;

    @FXML
    private TextField txtUrlCode;

    @FXML
    private TextField txtName;

    @FXML
    private TextField soapInputField;

    @FXML
    private Button soapSearchButton;

    @FXML
    private CheckBox soapInstaUpdateCheckBox;

    @FXML
    private TextArea soapOutputArea;

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private Country selectedCountry;

    @FXML
    public void initialize() {
        fetchButton.setOnAction(event -> fetchCountries());

        searchWeather.setOnAction(e -> fetchWeather());

        txtfWeather.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                if (instaSearchWeather.isSelected()) {
                    fetchWeather();
                }
            } else {
                txtaWeather.clear();
            }
        });

        countryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCountry = newSelection;
                txtCode.setText(selectedCountry.getCode());
                txtUrlCode.setText(selectedCountry.getUrlCode());
                txtName.setText(selectedCountry.getName());

                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        createButton.setOnAction(e -> {
            if (!areFieldsValid()) {
                showAlert("Missing fields", "Please fill in all fields.");
                return;
            }

            Country newCountry = new Country();
            newCountry.setCode(txtCode.getText().trim());
            newCountry.setUrlCode(txtUrlCode.getText().trim());
            newCountry.setName(txtName.getText().trim());

            try {
                HttpResponse<String> response = ApiClient.post("http://localhost:8080/api/xml-countries", newCountry);

                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    showAlert("Success", "Country created.");
                    fetchCountries();
                    clearFields();
                } else {
                    showAlert("Create Failed", response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to create country: " + ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            if (selectedCountry == null) {
                showAlert("No country selected", "Please select a country to update.");
                return;
            }

            if (!areFieldsValid()) {
                showAlert("Missing fields", "Please fill in all fields.");
                return;
            }

            Country updated = new Country();
            updated.setCode(txtCode.getText().trim());
            updated.setUrlCode(txtUrlCode.getText().trim());
            updated.setName(txtName.getText().trim());

            try {
                String url = "http://localhost:8080/api/xml-countries/" + selectedCountry.getCode();
                HttpResponse<String> response = ApiClient.put(url, updated);

                if (response.statusCode() == 200) {
                    showAlert("Success", "Country updated.");
                    fetchCountries();
                    clearFields();
                } else {
                    showAlert("Update Failed", response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to update country: " + ex.getMessage());
            }
        });

        deleteButton.setOnAction(e -> {
            if (selectedCountry == null) {
                showAlert("No country selected", "Please select a country to delete.");
                return;
            }

            try {
                String url = "http://localhost:8080/api/xml-countries/" + selectedCountry.getCode();
                HttpResponse<String> response = ApiClient.delete(url);

                if (response.statusCode() == 204) {
                    showAlert("Success", "Country deleted.");
                    fetchCountries();
                    clearFields();
                } else {
                    showAlert("Delete Failed", response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to delete country: " + ex.getMessage());
            }
        });

        soapSearchButton.setOnAction(event -> {
            try {
                String term = soapInputField.getText();
                if (term == null || term.isBlank()) {
                    soapOutputArea.setText("Please enter a search term.");
                    return;
                }
                String soapResponse = callSoapService(term);
                String prettyXml = prettyFormatXml(soapResponse);
                soapOutputArea.setText(prettyXml);
            } catch (Exception e) {
                soapOutputArea.setText("Error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        sendRawXmlButton.setOnAction(event -> sendXmlToValidator());
    }

    private void sendXmlToValidator() {
        String xmlContent = textAreaRawXml.getText().trim();
        if (xmlContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter XML content to validate.");
            return;
        }

        String url = null;
        if (xsdRadio.isSelected()) {
            url = "http://localhost:8080/api/countries/xsd";
        } else if (rngRadio.isSelected()) {
            url = "http://localhost:8080/api/countries/rng";
        } else {
            showAlert(Alert.AlertType.ERROR, "Selection Required", "Please select either XSD or RNG validation.");
            return;
        }

        String finalUrl = url;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(finalUrl))
                            .header("Content-Type", "application/xml")
                            .POST(HttpRequest.BodyPublishers.ofString(xmlContent))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        showAlertLater(Alert.AlertType.INFORMATION, "Success", response.body());
                    } else {
                        showAlertLater(Alert.AlertType.ERROR, "Validation Failed", response.body());
                    }
                } catch (Exception e) {
                    showAlertLater(Alert.AlertType.ERROR, "Error", "Exception: " + e.getMessage());
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    // Run alert on JavaFX Application Thread
    private void showAlertLater(Alert.AlertType type, String title, String message) {
        javafx.application.Platform.runLater(() -> showAlert(type, title, message));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String callSoapService(String term) throws IOException {
        String soapEndpointUrl = "http://localhost:9090/CountrySearchService";
        String soapAction = "";

        String soapRequestBody =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:gs=\"http://soap.backend.stuff.good/\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <gs:searchCountriesByTerm>\n" +
                        "         <term>" + escapeXml(term) + "</term>\n" +
                        "      </gs:searchCountriesByTerm>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";

        URL url = new URL(soapEndpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        if (!soapAction.isEmpty()) {
            connection.setRequestProperty("SOAPAction", soapAction);
        }
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapRequestBody.getBytes("UTF-8"));
        }

        int responseCode = connection.getResponseCode();

        InputStream inputStream;
        if (responseCode == 200) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        return readStreamAsString(inputStream);
    }

    private String readStreamAsString(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("auth-view.fxml"));
            Scene authScene = new Scene(loader.load());
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(authScene);
            stage.setTitle("Login - Algebra IIS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchWeather() {
        String city = txtfWeather.getText().trim();
        if (city.isEmpty()) {
            txtaWeather.setText("Please enter a city name.");
            return;
        }

        if (txtaWeather.getText().isEmpty()) {
            txtaWeather.setText("Loading weather for " + city + "...");
        }

        String xmlRequest = "<?xml version=\"1.0\"?>\n" +
                "<methodCall>\n" +
                "  <methodName>getTemperature</methodName>\n" +
                "  <params>\n" +
                "    <param>\n" +
                "      <value><string>" + city + "</string></value>\n" +
                "    </param>\n" +
                "  </params>\n" +
                "</methodCall>";


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/xmlrpc"))
                .header("Content-Type", "text/xml")
                .POST(HttpRequest.BodyPublishers.ofString(xmlRequest))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    String formattedResult = parseXmlRpcResponse(responseBody);
                    Platform.runLater(() -> txtaWeather.setText(formattedResult));
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> txtaWeather.setText("Failed to fetch weather: " + e.getMessage()));
                    return null;
                });
    }

    private String parseXmlRpcResponse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

            NodeList memberNodes = doc.getElementsByTagName("member");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < memberNodes.getLength(); i++) {
                Element member = (Element) memberNodes.item(i);

                String cityName = member.getElementsByTagName("name").item(0).getTextContent();
                String temp = member.getElementsByTagName("string").item(0).getTextContent();

                sb.append(cityName).append(": ").append(temp).append(" °C\n");
            }

            return sb.toString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private void fetchCountries() {
        try {
            String url = "http://localhost:8080/api/xml-countries";
            var response = ApiClient.get(url);

            if (response.statusCode() == 200) {
                List<Country> countries = mapper.readValue(response.body(), new TypeReference<>() {});
                countryListView.getItems().setAll(countries);
            } else {
                System.err.println("Failed to fetch countries: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean areFieldsValid() {
        return !txtCode.getText().trim().isEmpty()
                && !txtUrlCode.getText().trim().isEmpty()
                && !txtName.getText().trim().isEmpty();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        txtCode.clear();
        txtUrlCode.clear();
        txtName.clear();
        selectedCountry = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
