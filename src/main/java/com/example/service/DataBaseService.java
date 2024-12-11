package com.example.service;

import com.example.models.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class DataBaseService {
    private static final String CONNECTION_STRING = "mongodb+srv://admin:admin123@cluster0.w6861.mongodb.net/";
    private static final String DATABASE_NAME = "Java";
    private static DataBaseService instance;

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private DataBaseService() {
        mongoClient = MongoClients.create(CONNECTION_STRING);
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public static DataBaseService getInstance() {
        if (instance == null) {
            instance = new DataBaseService();
        }
        return instance;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    // Read all documents from a collection
    public List<Document> getAllDocuments(String collectionName) {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.find().forEach(documents::add);
        return documents;
    }

    // Insert a document into a collection
    public void insertDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(document);
    }

    // Update a document in a collection
    public void updateDocument(String collectionName, String key, Object value, String updateKey, Object updateValue) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.updateOne(Filters.eq(key, value), Updates.set(updateKey, updateValue));
    }

    // Delete a document from a collection
    public void deleteDocument(String collectionName, String key, Object value) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.deleteOne(Filters.eq(key, value));
    }

    // Example methods specific to 'user' and 'csv_files' collections

    public List<Document> getAllUsers() {
        return getAllDocuments("users");
    }

    public List<Document> getAllCsvFiles() {
        return getAllDocuments("csv_files");
    }

    public void addUser(Document user) {
        insertDocument("users", user);
    }

    public void addCsvFile(Document csvFile) {
        insertDocument("csv_files", csvFile);
    }

    public void updateUser(String key, Object value, String updateKey, Object updateValue) {
        updateDocument("users", key, value, updateKey, updateValue);
    }

    public void updateCsvFile(String key, Object value, String updateKey, Object updateValue) {
        updateDocument("csv_files", key, value, updateKey, updateValue);
    }

    public void deleteUser(String key, Object value) {
        deleteDocument("users", key, value);
    }

    public void deleteCsvFile(String key, Object value) {
        deleteDocument("csv_files", key, value);
    }

    public void closeConnection() {
        mongoClient.close();
    }

    public User loginUser(String email, String password) {
        MongoCollection<Document> collection = getCollection("users");
        
        // Kullanıcıyı e-posta ve şifre ile sorgula
        Document userDoc = collection.find(Filters.and(
            Filters.eq("email", email),
            Filters.eq("password_hash", password)
        )).first();
        
        if (userDoc != null) {
            // MongoDB'den dönen Document'i User modeline dönüştür
            return new User(
                userDoc.getObjectId("_id"),
                userDoc.getString("username"),
                userDoc.getString("email"),
                userDoc.getString("password_hash"),
                userDoc.get("createdAt"), // createdAt değeri LocalDateTime olacak
                userDoc.getList("csv_files", ObjectId.class)
            );
        } else {
            // Kullanıcı bulunmazsa null döndür
            return null;
        }
    }
}