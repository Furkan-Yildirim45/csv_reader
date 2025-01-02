package com.example.models;

import org.bson.Document;

import java.util.List;

public class CsvFile {
    private String fileName;
    private List<String[]> content;

    public CsvFile(String fileName, List<String[]> content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String[]> getContent() {
        return content;
    }

    // MongoDB'ye eklemek için Document'e dönüştürme metodu
    public Document toDocument() {
        return new Document()
                .append("fileName", fileName)
                .append("content", content);
    }
}
