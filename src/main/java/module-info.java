module com.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example to javafx.fxml;
    exports com.example;

    opens com.example.controller to javafx.fxml;  // Burada controller paketini FXML'e açıyoruz

}
