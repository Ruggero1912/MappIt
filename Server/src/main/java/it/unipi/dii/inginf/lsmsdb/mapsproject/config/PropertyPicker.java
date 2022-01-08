package it.unipi.dii.inginf.lsmsdb.mapsproject.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyPicker {

    private static final Logger LOGGER = Logger.getLogger( PropertyPicker.class.getName() );
    Properties properties = new Properties();
    final String fileName = "application.properties";
    public static final String defaultPicKey = "defaults.user.profilepic";
    public static final String documentDBkey = "persistence.db.kind.information";
    public static final String documentDbMongoKey = "mongodb";

    public static final String graphDBkey = "persistence.db.kind.social";
    public static final String graphDbNeoKey = "neo4j";

    public static final String MongoURI = "persistence.db.mongo.URI";
    public static final String MongoDBName = "persistence.db.mongo.dbName";

    public static final String Neo4jURI = "persistence.db.neo4j.URI";
    public static final String Neo4jUsername = "persistence.db.neo4j.username";
    public static final String Neo4jPassword = "persistence.db.neo4j.password";

    public static final String userCollection = "persistence.db.mongo.collection.user";
    public final static String placeCollection = "persistence.db.mongo.collection.place";

    private PropertyPicker() {
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.log( Level.SEVERE, "Could not open {0}: \n {1}", new Object[]{fileName, e.toString()});
        }
    }

    private String getPropertyPriv(String propertyName){
        String ret = this.properties.getProperty(propertyName);
        if(ret==null){
            LOGGER.warning("Property " + propertyName + " not found in " + fileName);
        }
        return ret;
    }

    public static String getProperty(String propertyName){
        PropertyPicker p = new PropertyPicker();
        LOGGER.log(Level.FINEST, "getProperty on " + propertyName);
        return p.getPropertyPriv(propertyName);
    }

    public static String getCollectionPropertyKey(String collection, String attribute){
        return getProperty(collection + "." + attribute);
    }
}
