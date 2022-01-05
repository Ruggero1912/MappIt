package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.util.List;
import java.util.logging.Logger;

public class PlaceService {

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

    public static List<Place> getPlacesInRadius(Coordinate coordinate, Double radius, String orderBy, String activityFilter) {
        //...
        return null;
    }

    public static List<Place> getPlacesInRadius(Coordinate coordinate, Double radius, String orderBy) {
        //...
        return null;
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
