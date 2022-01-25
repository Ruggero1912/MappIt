package it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.bson.Document;

import javax.xml.crypto.Data;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class MongoConnection {

    //private static final Logger LOGGER = Logger.getLogger( MongoConnection.class.getName() );

    private static final String MongoURI = PropertyPicker.getProperty(PropertyPicker.MongoURI);
    private static final String DatabaseName = PropertyPicker.getProperty(PropertyPicker.MongoDBName);
    private static final MongoConnection obj = new MongoConnection(); //we can always use the same as the threads are managed by the library

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoConnection(){
        //LOGGER.log(Level.FINEST, "using {0} as MongoURI", new Object[]{MongoURI});
        mongoClient = MongoClients.create((MongoURI));
        database = mongoClient.getDatabase(DatabaseName);
    }

    public static MongoConnection getObj(){
        return obj;
    }

    public static MongoDatabase getDatabase(){
        return obj.database;
    }

    public static MongoCollection getCollection(String collectionName){
        return getDatabase().getCollection(collectionName);
    }

    public enum Collections{
        USERS("user"),
        PLACES("place"),
        POSTS("post"),
        ACTIVITIES("activity");

        private String name;

        Collections(String n){
            this.name = n;
        }

        public String toString(){
            return this.name;
        }
    }
}
