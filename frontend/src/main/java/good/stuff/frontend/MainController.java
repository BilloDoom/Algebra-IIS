package good.stuff.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
    private ListView<Country> countryListView;

    @FXML
    private TextField txtCode;

    @FXML
    private TextField txtUrlCode;

    @FXML
    private TextField txtName;

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
