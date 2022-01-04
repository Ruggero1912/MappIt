package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceManagerMongoDB implements PlaceManager{

    private static final Logger LOGGER = Logger.getLogger(PlaceManagerMongoDB.class.getName());

    private static final String IDKEY = "_id";
    private static final String PLACENAMEKEY = "name";

    private MongoCollection placeCollection;

    public PlaceManagerMongoDB(){
        placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
    }


    @Override
    public Place getPlaceFromId(String id) {
        //...
        return null;
    }

    @Override
    public List<Place> getPlacesGivenCoordinate(Coordinate coordinate) {
        //...
        return null;
    }

    @Override
    public List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity) {
        //...
        return null;
    }
}
