package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;

import java.util.List;

public interface PlaceManager {

    public static final int DEFAULT_MAXIMUM_QUANTITY = 100;

    /**
     * return null if it does not exist a place with that id, otherwise returns the associated place
     * @param id the id of the requested place
     * @return associated Place object or null if not found
     */
    Place getPlaceFromId(String id);

    /**
     * returns a list of places that are in the radius, ordered by a certain criteria
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radius (in km) the radius of the zone in which to search
     * @param orderByCriteria the criteria for which the results should be ordinated
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderByCriteria);

    /**
     * returns a list of places that are in the radius, ordered by a certain criteria
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radius (in km) the radius of the zone in which to search
     * @param orderByCriteria the criteria for which the results should be ordinated
     * @param activityFilter the activity that the returned Places should fit
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadiusFilteredByFits(Coordinate coordinates, Double radius, String orderByCriteria, String activityFilter);

    /**
     * returns a list of places from all over the world ordered by popularity
     * @param activityName can be "any" or the name of an activity that should be in the fits of the returned Places
     * @param maxQuantity
     * @return a List of Places ordered by popularity
     */
    List<Place> getPopularPlaces(String activityName, int maxQuantity);

    /**
     * return true if the increment/decrement of the favs counter value is successfull, else return false
     * @param placeId of the place
     * @param k will be +1 or -1 respectively if the method is called from addPlaceToFavourites or removePlaceToFavourites
     * @return true if increment has been successful, false otherwise
     */
    boolean updateFavouriteCounter(String placeId, int k);
}
