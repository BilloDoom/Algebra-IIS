package good.stuff.frontend;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthViewController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store JWT token after login
    private String jwtToken;

    // Backend URL (adjust as needed)
    private final String BASE_URL = "http://localhost:8080/auth";

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> login());
        registerButton.setOnAction(e -> register());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.", "red");
            return;
        }

        Map<String, String> credentials = Map.of("username", username, "password", password);
        try {
            String requestBody = objectMapper.writeValueAsString(credentials);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                Map<?, ?> respMap = objectMapper.readValue(response.body(), Map.class);
                                jwtToken = (String) respMap.get("accessToken");
                                ApiClient.setToken(jwtToken);

                                Platform.runLater(() -> {
                                    showMessage("Login successful!", "green");
                                    openMainApp();
                                });
                            } catch (IOException ex) {
                                Platform.runLater(() -> showMessage("Failed to parse login response", "red"));
                            }
                        } else {
                            Platform.runLater(() -> showMessage("Login failed: " + response.body(), "red"));
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showMessage("Error: " + ex.getMessage(), "red"));
                        return null;
                    });

        } catch (Exception e) {
            showMessage("Error preparing login request: " + e.getMessage(), "red");
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.", "red");
            return;
        }

        Map<String, String> credentials = Map.of("username", username, "password", password);
        try {
            String requestBody = objectMapper.writeValueAsString(credentials);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            Platform.runLater(() -> showMessage("Registration successful! Please login.", "green"));
                        } else {
                            Platform.runLater(() -> showMessage("Register failed: " + response.body(), "red"));
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showMessage("Error: " + ex.getMessage(), "red"));
                        return null;
                    });

        } catch (Exception e) {
            showMessage("Error preparing register request: " + e.getMessage(), "red");
        }
    }

    private void showMessage(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    private void openMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Main App");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
