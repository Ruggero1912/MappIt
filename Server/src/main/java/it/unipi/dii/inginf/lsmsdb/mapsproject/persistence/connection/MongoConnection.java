package it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoConnection {

    private static final String MongoURI = PropertyPicker.getProperty(PropertyPicker.MongoURI);
    private static final String DatabaseName = PropertyPicker.getProperty(PropertyPicker.MongoDBName);


    public static void mongoDBFirstTest(){
        try (MongoClient mongoClient = MongoClients.create(MongoURI)) {
            MongoDatabase database = mongoClient.getDatabase(DatabaseName);
            MongoCollection<Document> collection = database.getCollection("user");
            Document doc = collection.find(eq("username", "fenomp")).first();
            System.out.println(doc.toJson());
        }
    }
}
