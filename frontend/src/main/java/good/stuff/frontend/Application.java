package good.stuff.frontend;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Algebra-IIS");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getResourceAsStream("emoji-cool.png"))));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}