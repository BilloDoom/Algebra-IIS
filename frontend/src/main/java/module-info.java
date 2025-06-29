module good.stuff.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens good.stuff.frontend to javafx.fxml;
    exports good.stuff.frontend;
}