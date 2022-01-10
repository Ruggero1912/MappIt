package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.ActivityService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PlaceService {

    public static final String ORDER_CRITERIA_DISTANCE = "distance";
    public static final String ORDER_CRITERIA_POPULARITY = "popularity";
    public static final List<String> orderByCriterias = Arrays.asList(ORDER_CRITERIA_DISTANCE, ORDER_CRITERIA_POPULARITY);
    public static final String defaultOrderByCriteria = ORDER_CRITERIA_DISTANCE;
    public static final double defaultSearchRadius = 10.0;
    public static final int DEFAULT_MAXIMUM_QUANTITY = 100;
    public static final int LIMIT_MAXIMUM_QUANTITY = 300;
    public static final String noActivityFilterKey = "any";

    public static final int DEFAULT_MAX_HOW_MANY_SUGGESTED = 10;

    private static final Logger LOG = Logger.getLogger(PlaceService.class.getName());

    /**
     * return null if it does not exist a place with that id, otherwise returns the associated place
     * @param id the id of the requested place
     * @return associated Place object or null if not found
     */
    public static Place getPlaceFromId(String id){
        PlaceManager um = PlaceManagerFactory.getPlaceManager();
        return um.getPlaceFromId(id);
    }

    /**
     * returns a list of places that are in the radius, ordered by a certain criteria
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radius (in km) the radius of the zone in which to search
     * @param orderBy the criteria for which the results should be ordinated
     * @param activityFilter the activity that the returned Places should fit
     * @return a list of Place object, else null if empty set
     */
    public static List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderBy, String activityFilter) {
        if( ! orderByCriterias.contains(orderBy)){
            orderBy = defaultOrderByCriteria;
        }
        if(activityFilter != noActivityFilterKey){
            if( ! ActivityService.checkIfActivityExists(activityFilter)){
                activityFilter = noActivityFilterKey;
            }
        }
        if(radius <= 0.0){
            radius = defaultSearchRadius;
        }
        // how should we check the correctness of the provided coordinates? We assume that those are always valid
        PlaceManager pm = PlaceManagerFactory.getPlaceManager();
        if(activityFilter == noActivityFilterKey){
            return pm.getPlacesInRadius(coordinates, radius, orderBy);
        }else{
            return pm.getPlacesInRadiusFilteredByFits(coordinates, radius, orderBy, activityFilter);
        }
    }

    public static List<Place> getPlacesInRadius(Coordinate coordinate, Double radius, String orderBy) {
        return getPlacesInRadius(coordinate, radius, orderBy, noActivityFilterKey);
    }

    public static List<Place> getSuggestedPlaces(User user){
        return null;
    }

    /**
     * return a list of the most popular Place objects ordered by the number of posts and the value of the favourite counter
     * @param activityFilter: the name of the activity that the returned places should fit or "any" if are required the absolute most popular places
     * @param maxQuantity: the maximum number of places instances to be returned
     * @return a list Place objects
     */
    public static List<Place> getPopularPlaces(String activityFilter, int maxQuantity) {
        if(activityFilter != noActivityFilterKey){
            if( ! ActivityService.checkIfActivityExists(activityFilter)){
                activityFilter = noActivityFilterKey;
            }
        }
        if(maxQuantity <= 0){
            maxQuantity = DEFAULT_MAXIMUM_QUANTITY;
        }else if(maxQuantity > LIMIT_MAXIMUM_QUANTITY){
            maxQuantity = LIMIT_MAXIMUM_QUANTITY;
        }
        PlaceManager pm = PlaceManagerFactory.getPlaceManager();
        return pm.getPopularPlaces(activityFilter, maxQuantity);
    }
}
