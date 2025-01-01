package com.example.service;

import com.example.models.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class DataBaseService {
    private static DataBaseService instance;
    private static final String DATABASE_NAME = "Java"; // Uygulama veritabanı adı

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String connectionString;

    // Private constructor to prevent direct instantiation
    private DataBaseService() {
        this.connectionString = "mongodb+srv://admin:admin123@cluster0.w6861.mongodb.net/";
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(DATABASE_NAME); // Java veritabanına bağlanıyoruz
        System.out.println("db ye bağlandı");
    }

    // Singleton pattern for getting the instance
    public static DataBaseService getInstance() {
        if (instance == null) {
            instance = new DataBaseService();  // Create the instance with a default constructor
        }
        return instance;
    }

    // Get a collection from the database
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
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

    // Example methods specific to 'users' collection
    public List<Document> getAllUsers() {
        return getAllDocuments("users");
    }

    // Add a new user and authorize them in the database
    public void addUser(User user) {
        // Admin bağlantısını kullanarak kullanıcıyı yetkilendir
        try {
            // Yeni kullanıcıyı yetkilendirmek için komut oluşturuyoruz
            Document createUserCommand = new Document("createUser", user.getUsername())
                .append("pwd", user.getPasswordHash()) // Hashlenmiş şifre
                .append("roles", List.of(
                    new Document("role", "readWrite").append("db", DATABASE_NAME) // Kullanıcı rolü
                ));

            database.runCommand(createUserCommand);  

            System.out.println("Kullanıcı yetkilendirildi.");
        } catch (Exception e) {
            System.err.println("Kullanıcı yetkilendirilirken hata oluştu: " + e.getMessage());
        }

        // Kullanıcıyı "users" koleksiyonuna da ekliyoruz (uygulama veritabanı için)
        Document document = new Document()
            .append("username", user.getUsername())
            .append("email", user.getEmail())
            .append("password_hash", user.getPasswordHash())
            .append("createdAt", user.getCreatedAt())
            .append("csv_files", user.getCsvFiles());

        insertDocument("users", document); // Uygulama koleksiyonuna ekleme
    }

    public boolean isEmailTaken(String email) {
        MongoCollection<Document> collection = getCollection("users");
        Document userDoc = collection.find(Filters.eq("email", email)).first();
        return userDoc != null;
    }


    public User loginUser(String email, String password) {
        MongoCollection<Document> collection = getCollection("users");

        // Kullanıcıyı e-posta ile sorgula
        Document userDoc = collection.find(Filters.and(
            Filters.eq("email", email),
            Filters.eq("password_hash", password)
        )).first();

        if (password.equals(userDoc.get("password_hash")) && email.equals(userDoc.get("email"))) {
            return new User(
                userDoc.getObjectId("_id"),
                userDoc.getString("username"),
                userDoc.getString("email"),
                userDoc.getString("password_hash"),
                userDoc.get("createdAt"), // createdAt değeri LocalDateTime olacak
                userDoc.getList("csv_files", ObjectId.class)
            );
        }

        // Kullanıcı bulunmazsa veya şifre yanlışsa null döndür
        return null;
    }

    // Get all documents from a collection
    public List<Document> getAllDocuments(String collectionName) {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.find().forEach(documents::add);
        return documents;
    }

    public void closeConnection() {
        mongoClient.close();
    }
}
