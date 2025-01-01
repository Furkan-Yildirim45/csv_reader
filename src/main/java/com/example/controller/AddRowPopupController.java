package com.example.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AddRowPopupController {

    @FXML
    private HBox fieldsContainer; // TextField'ların yer alacağı konteyner

    private PrimaryController parentController;
    private Stage popupStage;
    private List<TextField> textFields = new ArrayList<>();

    public void setParentController(PrimaryController parentController) {
        this.parentController = parentController;
        createTextFields();
    }

    private void createTextFields() {
        // Sütun başlıklarına göre TextField'lar oluşturulacak
        List<String> columnHeaders = Arrays.asList("Column 1", "Column 2", "Column 3"); // Başlıklar burada manuel tanımlandı

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
