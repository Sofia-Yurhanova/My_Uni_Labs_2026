module org.example.lab6 {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    opens org.example.lab6 to javafx.fxml, javafx.base;
    exports org.example.lab6;
}