package com.example.models;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class User {
    private ObjectId id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private List<ObjectId> csvFiles;

    // Constructor
    public User(ObjectId id, String username, String email, String passwordHash, Object createdAt, List<ObjectId> csvFiles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        
        // Eğer createdAt bir String ise, bunu LocalDateTime'a çevir
        if (createdAt instanceof String) {
            this.createdAt = LocalDateTime.parse((String) createdAt);
        } else if (createdAt instanceof java.util.Date) {
            // Eğer createdAt bir Date objesi ise, bunu LocalDateTime'a çevir
            this.createdAt = ((java.util.Date) createdAt).toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        }
        
        this.csvFiles = csvFiles;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ObjectId> getCsvFiles() {
        return csvFiles;
    }

    public void setCsvFiles(List<ObjectId> csvFiles) {
        this.csvFiles = csvFiles;
    }
}
