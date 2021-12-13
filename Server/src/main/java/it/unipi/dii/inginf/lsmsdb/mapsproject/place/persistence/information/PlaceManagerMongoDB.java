package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;

public class PlaceManagerMongoDB implements PlaceManager{

    private static final String IDKEY = "_id";
    private static final String PLACENAMEKEY = "name";

    private MongoCollection placeCollection;

    public PlaceManagerMongoDB(){
        placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
    }
}
