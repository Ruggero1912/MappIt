package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
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
        if(id.equals(""))
            return  null;

        ObjectId objId;
        try{
            objId = new ObjectId(id);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }
        Bson idFilter = Filters.eq(IDKEY, objId);
        MongoCursor<Document> cursor = placeCollection.find(idFilter).cursor();
        if(!cursor.hasNext()){
            return null;
        }else{
            Document placeDoc = cursor.next();
            Place ret = new Place(placeDoc);
            return ret;
        }
    }

    /**
     * use this method to query the Place Collection for Places instances
     * @param filters : a Bson object representing the filters to use to query the place Collection
     * @return null if empty set, else a List of Places respecting the given filters
     */
    private List<Place> queryPlaceCollection(Bson filters){
        List<Place> places = new ArrayList<>();
        FindIterable<Document> iterable = placeCollection.find(filters);
        iterable.forEach(doc -> places.add(new Place(doc)));
        return places;
    }

    @Override
    public List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderBy) {
        Double radiusInMeters = radius * 1000.0;
        Point point = coordinates.toPoint();
        Bson radiusFilter = Filters.near(Place.KEY_LOC, point, radiusInMeters, null);
        return this.queryPlaceCollection(radiusFilter);
    }

    @Override
    public List<Place> getPlacesInRadiusFilteredByFits(Coordinate coordinates, Double radius, String orderBy, String activityName) {
        Double radiusInMeters = radius * 1000.0;
        Point point = coordinates.toPoint();
        Bson radiusFilter = Filters.near(Place.KEY_LOC, point, radiusInMeters, null);
        Bson activityFilter = Filters.in(Place.KEY_FITS, activityName);
        Bson filter = Filters.and(radiusFilter, activityFilter);
        return this.queryPlaceCollection(filter);
    }

    @Override
    public List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity) {
        //...
        return null;
    }
}
