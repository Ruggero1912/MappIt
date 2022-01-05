package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

    @ApiOperation(value = "Get information of a specific place", notes = "This method retrieve information of a specific place, given its _id")
    @GetMapping(value = "/place/{id}", produces = "application/json")
    public Place getPlaceById(@PathVariable(value = "id") String placeId) {
        return PlaceService.getPlaceFromId(placeId);
    }

    @ApiOperation(value = "Get a list of places nearby a certain radius, centered on the given coordinate")
    @GetMapping(value = "/place", produces = "application/json")
    public List<Place> getPlacesGivenCoordinate(Coordinate coordinate) {
        return PlaceService.getPlacesGivenCoordinate(coordinate);
    }

    @ApiOperation(value = "Get a list of places nearby the given coordinate which also fit with the specified activity")
    @GetMapping(value = "/place", produces = "application/json")
    public List<Place> getPlacesGivenCoordinateAndActivity(Coordinate coordinate, String activity) {
        return PlaceService.getPlacesGivenCoordinateAndActivity(coordinate, activity);
    }

    // suggested places for the current user

    // popular places in a given radius (nearby or near to a given point) 

    // your favourites places

    // places that are favourites of a given user (it should receive the id of the user)

    // most popular places

    // most popular places for given category

    // places that you visited

    // places visited by a given user

    // add to favourite a place (wants the id of the place)

    // add to visited a place (wants the id of the pleace)

    // remove from favourite a place (wants the id of the place)

    // remove from visited a place (wants the id of the pleace)
}