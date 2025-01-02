package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AddRowPopupController {

    @FXML
    private HBox fieldsContainer; // TextField'ların yer alacağı konteyner

    @FXML
    private Button addButton;

    private PrimaryController parentController;
    private Stage popupStage;
    private List<TextField> textFields = new ArrayList<>();

    public void initialize() {
        addButton.setOnAction(event -> addRowToTableView());
    }

    public void setParentController(PrimaryController parentController, List<String> headers) {
        this.parentController = parentController;
        createTextFields(headers);
    }

    private void createTextFields(List<String> headers) {
        // Sütun başlıklarına göre TextField'lar oluşturulacak
        List<String> columnHeaders = headers; // Başlıklar burada manuel tanımlandı

        for (String header : columnHeaders) {
            TextField textField = new TextField();
            textField.setPromptText(header); // TextField'a placeholder ekliyoruz
            textFields.add(textField);
            fieldsContainer.getChildren().add(textField); // TextField'ı HBox'a ekliyoruz
        }
    }

    // Ekle butonuna tıklanınca bu metot çalışacak
    @FXML
    private void addRowToTableView() {
        String[] newRow = new String[textFields.size()];

        for (int i = 0; i < textFields.size(); i++) {
            newRow[i] = textFields.get(i).getText(); // TextField'lardan veriyi alıyoruz
        }

        parentController.addRowToTableView(newRow); // Veriyi ana TableView'a ekliyoruz
        popupStage.close(); // Popup'ı kapatıyoruz
    }

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }
}
