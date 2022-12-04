module com.example.gamepbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.gamepbo to javafx.fxml;
    exports com.example.gamepbo;
}