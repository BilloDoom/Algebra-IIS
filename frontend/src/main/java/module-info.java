module good.stuff.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.xml;


    opens good.stuff.frontend to javafx.fxml;
    exports good.stuff.frontend;
}