package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.ActivityService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PlaceService {

    public static List<String> orderByCriterias = Arrays.asList("distance", "popularity");
    public static String defaultOrderByCriteria = "distance";
    public static double defaultSearchRadius = 10.0;
    public static String noActivityFilterKey = "any";

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
     * @return a list Place objects
     * note: to determine which Place is popular maybe we could also return the Places whose post received the greatest number of interactions
     */
    public static List<Place> getPopularPlaces(String activityFilter) {
        // TODO: should check the value of activityFilter to determine if is "any" or if it is a valid activity or none of them
        return null;
    }
}
