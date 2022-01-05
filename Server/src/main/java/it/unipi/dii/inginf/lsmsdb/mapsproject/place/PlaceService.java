package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;

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


    public static List<Place> getPlacesGivenCoordinate(Coordinate coordinate) {
        //...
        return null;
    }

    public static List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity) {
        //...
        return null;
    }
}
