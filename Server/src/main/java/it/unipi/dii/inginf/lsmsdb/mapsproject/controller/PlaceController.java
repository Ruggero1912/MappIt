package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // popular places in a given radius (nearby or near to a given point)
    @ApiOperation(value = "returns a list of places near the given coordinates in a certain radius (in km), if an activity is specified, returns only the ones that fits to that activity")
    @GetMapping(value = "/places/nearby", produces = "application/json")
    public List<Place> nearbyPlaces(@RequestParam Double lat, @RequestParam Double lon,
                                    @RequestParam( defaultValue = "10.0") Double radius,
                                    @RequestParam( defaultValue = "any") String activityFilter,
                                    @RequestParam( defaultValue = "distance") String orderBy) {
        // check the validity of the given activity
        // check the validity of the given radius (in km)
        // parse the coordinates from lon, lat
        Coordinate coordinates = new Coordinate(lat, lon);
        List<String> orderingMethods = new ArrayList<String>();
        // TODO: the ordering methods array should be declared inside PlaceService
        orderingMethods.add("distance");
        orderingMethods.add("popularity");
        if(! orderingMethods.contains(orderBy)){
            // the case in which the user has specified an invalid ordering method
            //we use the default ordering method
            orderBy = "distance";
        }
        if(activityFilter == "any"){
          return PlaceService.getPlacesInRadius(coordinates, radius, orderBy);
        }
        else{
          return PlaceService.getPlacesInRadius(coordinates, radius, orderBy, activityFilter);
        }
    }
    // most popular places
    // most popular places for given category
    @ApiOperation(value = "returns a list of popular places")
    @GetMapping(value = "/places/most-popular", produces = "application/json")
    public List<Place> popularPlaces(@RequestParam( defaultValue = "any") String activityFilter) {
    return PlaceService.getPopularPlaces(activityFilter);
    }

    // suggested places for the current user
    @ApiOperation(value = "returns a list of suggested places for the current user")
    @GetMapping(value = "/places/suggested", produces = "application/json")
    public List<Place> suggestedPlaces() {
        //should retrieve the current user
        User u = new User(); // TODO: call the method that returns the instance of the currently logged in user
        return PlaceService.getSuggestedPlaces(u);
    }
    // your favourite places
    // places that are favourites of a given user (it should receive the id of the user)
    @ApiOperation(value = "returns the list of favourite places for the specified user or for the current if no userId is specified")
    @GetMapping(value = "/places/favourites", produces = "application/json")
    public List<Place> favouritePlaces(@RequestParam( defaultValue = "current") String userId) {
        User u;
        if(userId == "current"){
            //should retrieve the current user
            u = new User(); // TODO: call the method that returns the instance of the currently logged in user
        }else{
            u = UserService.getUserFromId(userId);
        }
        // NOTE: u could be null, getFavouritePlaces should handle this case
        return UserService.getFavouritePlaces(u);
    }
    // places that you visited
    // places visited by a given user
    @ApiOperation(value = "returns the list of visited places for the specified user or for the current if no userId is specified")
    @GetMapping(value = "/places/visited", produces = "application/json")
    public List<Place> visitedPlaces(@RequestParam( defaultValue = "current") String userId) {
        User u;
        if(userId == "current"){
            //should retrieve the current user
            u = new User(); // TODO: call the method that returns the instance of the currently logged in user
        }else{
            u = UserService.getUserFromId(userId);
        }
        // NOTE: u could be null, getVisitedPlaces should handle this case
        return UserService.getVisitedPlaces(u);
    }

    // add to favourite a place (wants the id of the place)
    // remove from favourite a place (wants the id of the place)
    @ApiOperation(value = "adds the specified place to the favourite places of the currently logged in user")
    @PostMapping(value = "/place/{id}/favourites/{action}", produces = "application/json")
    public ResponseEntity handleFavourites(@PathVariable(name="id") String placeId, @PathVariable(name = "action") String action) {
        //should retrieve the place object (and check if it exists)
        Place place = PlaceService.getPlaceFromId(placeId);
        if(place == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"the specified place does not exist\"}");
        }
        //should retrieve the current user
        User u = new User(); // TODO: call the method that returns the instance of the currently logged in user
        List<String> actions = new ArrayList<String>();
        actions.add("add");
        actions.add("remove");
        if( ! actions.contains(action)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"action not allowed\"}");
        }
        // TODO: handle the outcome of the method call and answer to the client consequently
        if(action == "add")
            UserService.addPlaceToFavourites(u, place);
        else if(action == "remove")
            UserService.removePlaceFromFavourites(u, place);

        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("{\"error\":\"handle correctly the response\"}");
    }
    // add to visited a place (wants the id of the place)
    // remove from visited a place (wants the id of the place) -> not implemented
    @ApiOperation(value = "adds the specified place to the visited places of the currently logged in user")
    @PostMapping(value = "/place/{id}/visited", produces = "application/json")
    public ResponseEntity handleVisited(@PathVariable(name="id") String placeId, @RequestBody(required = false) LocalDateTime localDateTime) {
        if(localDateTime == null){
            // the time of the visit will be considered now
            localDateTime = LocalDateTime.now();
        }
        //should retrieve the place object (and check if it exists)
        Place place = PlaceService.getPlaceFromId(placeId);
        if(place == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"the specified place does not exist\"}");
        }
        //should retrieve the current user
        User u = new User(); // TODO: call the method that returns the instance of the currently logged in user

        // TODO: handle the outcome of the method call and answer to the client consequently
        UserService.addPlaceToVisited(u, place, localDateTime);
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("{\"error\":\"handle correctly the response\"}");
    }
}