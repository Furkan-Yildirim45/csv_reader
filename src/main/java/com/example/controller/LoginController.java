package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private PrimaryController primaryController;

    public void setPrimaryController(PrimaryController controller) {
        this.primaryController = controller;
    }

    @FXML
    public void initialize() {
    }

    // private void handleLogin() {
    //     String username = usernameField.getText();
    //     String password = passwordField.getText();

    //     if (username.isEmpty() || password.isEmpty()) {
    //         errorLabel.setText("Username and password cannot be empty!");
    //         errorLabel.setVisible(true);
    //         return;
    //     }

    //     if (primaryController != null && primaryController.authenticateUser(username, password)) {
    //         primaryController.updateUserInfo(username);
    //         loginButton.getScene().getWindow().hide(); // Popup'u kapat
    //     } else {
    //         errorLabel.setText("Invalid credentials!");
    //         errorLabel.setVisible(true);
    //     }
    // }
}
