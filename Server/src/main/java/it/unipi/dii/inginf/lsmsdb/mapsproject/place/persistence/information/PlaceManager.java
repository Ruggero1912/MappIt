package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.util.List;

public interface PlaceManager {


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
     * @param howMany is the quantity of places to be returned
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderByCriteria, int howMany);

    /**
     * returns a list of places that are in the radius, ordered by a certain criteria
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radius (in km) the radius of the zone in which to search
     * @param orderByCriteria the criteria for which the results should be ordinated
     * @param activityFilter the activity that the returned Places should fit
     * @param howMany is the quantity of places to be returned
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadiusFilteredByFits(Coordinate coordinates, Double radius, String orderByCriteria, String activityFilter, int howMany);

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


    /**
     * @param placeName is the place name suffix from which the method will search
     * @param howMany is the quantity of places to be returned
     * notes = "This method retrieve places that has an name which is equal or that contains the one given")
     */
    List<Place> retrievePlacesFromName(String placeName, int howMany);
}
