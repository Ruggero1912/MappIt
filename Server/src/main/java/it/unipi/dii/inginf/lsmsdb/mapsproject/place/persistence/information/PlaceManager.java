package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;

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
     * @param orderBy the criteria for which the results should be ordinated
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadius(Coordinate coordinates, Double radius, String orderBy);

    /**
     * returns a list of places that are in the radius, ordered by a certain criteria
     * @param coordinates a Coordinates object representing the center of the zone in which it searches
     * @param radius (in km) the radius of the zone in which to search
     * @param orderBy the criteria for which the results should be ordinated
     * @param activityFilter the activity that the returned Places should fit
     * @return a list of Place object, else null if empty set
     */
    List<Place> getPlacesInRadiusFilteredByFits(Coordinate coordinates, Double radius, String orderBy, String activityFilter);



    List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity);

}
