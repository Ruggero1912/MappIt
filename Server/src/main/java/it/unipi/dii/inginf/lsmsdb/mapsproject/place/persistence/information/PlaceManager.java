package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;

import java.util.List;

public interface PlaceManager {

    Place getPlaceFromId(String id);

    List<Place> getPlacesGivenCoordinate(Coordinate coordinate);

    List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity);

}
