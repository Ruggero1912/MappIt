package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Updates.set;

public class PlaceManagerMongoDB implements PlaceManager{

    private static final Logger LOGGER = Logger.getLogger(PlaceManagerMongoDB.class.getName());

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
        Bson idFilter = Filters.eq(Place.KEY_ID, objId);
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
     * it orders the result set by the default criteria, which in case of use of the 'near' filter, is the distance
     * the result set quantity is limited to the value of PlaceManager.DEFAULT_MAXIMUM_QUANTITY
     * @param filters : a Bson object representing the filters to use to query the place Collection
     * @return null if empty set, else a List of Places respecting the given filters
     */
    private List<Place> queryPlaceCollection(Bson filters){
        return queryPlaceCollection(filters, null, DEFAULT_MAXIMUM_QUANTITY);
    }
    /**
     * use this method to query the Place Collection for Places instances
     * the result set quantity is limited to the value of PlaceManager.DEFAULT_MAXIMUM_QUANTITY
     * @param filters : a Bson object representing the filters to use to query the place Collection
     * @param sort : the Bson object representing the required sort criteria
     * @return null if empty set, else a List of Places respecting the given filters
     */
    private List<Place> queryPlaceCollection(Bson filters, Bson sort){
        return queryPlaceCollection(filters, sort, DEFAULT_MAXIMUM_QUANTITY);
    }
    /**
     * use this method to query the Place Collection for Places instances
     * it orders the result set by the default criteria, which in case of use of the 'near' filter, is the distance
     * @param filters : a Bson object representing the filters to use to query the place Collection
     * @param maxQuantity : the maximum quantity of Places to be returned
     * @return null if empty set, else a List of Places respecting the given filters
     */
    private List<Place> queryPlaceCollection(Bson filters, int maxQuantity){
        return queryPlaceCollection(filters, null, maxQuantity);
    }
    /**
     * use this method to query the Place Collection for Places instances
     * @param filters : a Bson object representing the filters to use to query the place Collection
     * @param sort : a Bson object representing the order by criteria to be used for the result set
     * @param maxQuantity : the maximum quantity of Places to be returned
     * @return null if empty set, else a List of Places respecting the given filters
     */
    private List<Place> queryPlaceCollection(Bson filters, Bson sort, int maxQuantity){
        if(maxQuantity <= 0){
            maxQuantity = DEFAULT_MAXIMUM_QUANTITY;
        }
        List<Place> places = new ArrayList<>();
        FindIterable<Document> iterable = placeCollection.find(filters).sort(sort).limit(maxQuantity);
        iterable.forEach(doc -> places.add(new Place(doc)));
        return places;
    }
    /**
     * It uses the $near Mongo Operator. The results will be ordered by distance by default
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radiusInKm (in km) the radius of the zone in which to search
     * @return a Bson object representing the distance filter
     */
    private Bson nearFilterRadiusInKm(Coordinate coordinates, Double radiusInKm){
        return Filters.near(Place.KEY_LOC, coordinates.toPoint(), radiusInKm * 1000.0, null);
    }
    /**
     * It uses the $in Mongo Operator. The results will have the specified activity in the fits array
     * @param activityName: the name of the activity that should be in the fits of the returned Places or "any"
     *                     NOTE that the activityName should be validated in advance
     * @return a Bson object representing the activity filter, null if the filter is not applicable
     */
    private Bson ActivityFilter(String activityName){
        if(activityName == null || activityName == PlaceService.noActivityFilterKey){
            return null;
        }
        BasicDBObject filter=new BasicDBObject();
        filter.put(Place.KEY_FITS, activityName);
        return filter;
    }

    private Bson popularityFilter(int minimumNumberOfPosts){
        if(minimumNumberOfPosts <= 0){
            minimumNumberOfPosts = 1;
        }
        //Bson minimumNumberOfPosts = Filters.expr(Filters.gte(Aggregates.size(Place.KEY_POSTS_ARRAY), minimumNumberOfPosts));
        //Bson hasPosts = Filters.and(Filters.type(Place.KEY_POSTS_ARRAY, 'array'), Filters.ne(Place.KEY_POSTS_ARRAY, {}));
        //the following checks if it exists the nth element of the posts array, where n == minimumNumberOfPosts
        Bson minimumNumberOfPostsFilter = Filters.exists(Place.KEY_POSTS_ARRAY + "." + minimumNumberOfPosts);

        //NOTE: to determine which Place is popular maybe we could also return the Places
        //      whose post received the greatest number of interactions
        return minimumNumberOfPostsFilter;
    }

    /**
     * generates the Bson that represent the required order by criteria
     * @param criteria a String that indicates the formal orderBy criteria
     * @return the Bson object representing the specified order by criteria
     */
    private Bson orderBy(String criteria){
        if(criteria == PlaceService.ORDER_CRITERIA_DISTANCE || ! PlaceService.orderByCriterias.contains(criteria)){
            return null;
        }
        if(criteria == PlaceService.ORDER_CRITERIA_POPULARITY){
            //NOTE: to determine which Place is popular maybe we could also return the Places
            //      whose post received the greatest number of interactions
            return Sorts.descending(Place.KEY_FAVOURITES);
        }
        return null;
    }

    @Override
    public List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderByCriteria) {
        return this.queryPlaceCollection(nearFilterRadiusInKm(coordinates, radius), orderBy(orderByCriteria));
    }

    @Override
    public List<Place> getPlacesInRadiusFilteredByFits(Coordinate coordinates, Double radius, String orderByCriteria, String activityName) {
        Bson radiusFilter = nearFilterRadiusInKm(coordinates, radius);
        Bson activityFilter = ActivityFilter(activityName);
        return this.queryPlaceCollection(Filters.and(radiusFilter, activityFilter), orderBy(orderByCriteria));
    }

    @Override
    public List<Place> getPopularPlaces(String activityName, int maxQuantity){
        Bson activityFilter = ActivityFilter(activityName);
        return this.queryPlaceCollection(Filters.and(activityFilter, popularityFilter(1)), orderBy(PlaceService.ORDER_CRITERIA_POPULARITY), maxQuantity);
    }

    @Override
    public boolean updateFavouriteCounter(String placeId, int k) {
        if(placeId == "" || k>1 || k<-1 || k==0)
            return false;
        Bson idFilter = Filters.eq(Place.KEY_ID, new ObjectId(placeId));
        UpdateResult res = placeCollection.updateOne(idFilter, Updates.inc(Place.NEO_KEY_FAVS, k));
        return res.wasAcknowledged();
    }
}
