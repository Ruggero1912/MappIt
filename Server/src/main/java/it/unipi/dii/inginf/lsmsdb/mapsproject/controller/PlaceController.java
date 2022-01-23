package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.UndefinedActivityException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Coordinate;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

    private static final Logger LOGGER = Logger.getLogger( PlaceController.class.getName() );

    @ApiOperation(value = "Get information of a specific place", notes = "This method retrieve information of a specific place, given its _id")
    @GetMapping(value = "/place/{id}", produces = "application/json")
    public ResponseEntity<?> getPlaceById(@PathVariable(value = "id") String placeId) {
        ResponseEntity<?> result;

        try{
            Place place = PlaceService.getPlaceFromId(placeId);
            result = ResponseEntity.ok(place);
            if(place==null) {
                LOGGER.log(Level.WARNING, "Error: could not find place (id=" + placeId + ")");
                result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find place\"}");
            }
        } catch (Exception e){
            LOGGER.log(Level.WARNING, "Error: could not parse place, an exception has occurred: " + e);
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not parse place, and exception has occurred\"}");
        }
        return result;
    }

    // popular places in a given radius (nearby or near to a given point)
    @ApiOperation(value = "returns a list of places near the given coordinates in a certain radius (in km), if an activity is specified, returns only the ones that fits to that activity")
    @GetMapping(value = "/places/nearby", produces = "application/json")
    public ResponseEntity<?> nearbyPlaces(@RequestParam Double lat, @RequestParam Double lon,
                                    @RequestParam( defaultValue = "10.0") Double radius,
                                    @RequestParam( defaultValue = "any") String activityFilter,
                                    @RequestParam( defaultValue = "distance") String orderBy) {
        ResponseEntity<?> result;
        List<Place> nearbyPlaces;

        try {
            //TODO: check the validity of the given activity
            // check the validity of the given radius (in km)
            // parse the coordinates from lon, lat
            Coordinate coordinates = new Coordinate(lat, lon);

            // the orderBy String content is checked by PlaceService
            if (activityFilter == "any") {
                nearbyPlaces = PlaceService.getPlacesInRadius(coordinates, radius, orderBy);
            } else {
                nearbyPlaces = PlaceService.getPlacesInRadius(coordinates, radius, orderBy, activityFilter);
            }

            result = ResponseEntity.status(HttpStatus.OK).body(nearbyPlaces);
        }catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting nearby places\"}");
        }

        return result;
    }
    // most popular places
    // most popular places for given category
    @ApiOperation(value = "returns a list of popular places")
    @GetMapping(value = "/places/most-popular", produces = "application/json")
    public ResponseEntity<?> popularPlaces(@RequestParam( defaultValue = "any", name = "activity") String activityFilter, @RequestParam(defaultValue = "0", name = "limit") int maxQuantity) {
        ResponseEntity<?> result;

        try {
            List<Place> popularPlaces = PlaceService.getPopularPlaces(activityFilter, maxQuantity);
            result = ResponseEntity.status(HttpStatus.OK).body(popularPlaces);
        }catch(UndefinedActivityException u){
            result = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\" : \"The specified activity '" + activityFilter + "' was not recognised!\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting popular places\"}");
        }

        return result;
    }

    // suggested places for the current user
    @ApiOperation(value = "returns a list of suggested places for the current user")
    @GetMapping(value = "/places/suggested", produces = "application/json")
    public ResponseEntity<?> suggestedPlaces() {
        ResponseEntity<?> result;
        try {
            UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userSpring.getApplicationUser();
            List<Place> suggestedPlaces = PlaceService.getSuggestedPlaces(currentUser);
            result = ResponseEntity.status(HttpStatus.OK).body(suggestedPlaces);
        }catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting suggested places\"}");
        }

        return result;
    }

    // add to favourite a place (wants the id of the place)
    // remove from favourite a place (wants the id of the place)
    @ApiOperation(value = "adds/removes the specified place to the favourite places of the currently logged in user")
    @PostMapping(value = "/place/{id}/favourites/{action}", produces = "application/json")
    public ResponseEntity<?> handleFavourites(@PathVariable(name="id") String placeId, @PathVariable(name = "action") String action) {
        ResponseEntity<?> result;

        //should retrieve the place object (and check if it exists)
        Place place = PlaceService.getPlaceFromId(placeId);
        if(place == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\":\"the specified place does not exist\"}");
        }
        //retrieve the current user
        UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User u = userSpring.getApplicationUser();

        List<String> actions = new ArrayList<String>();
        actions.add("add");
        actions.add("remove");
        if( ! actions.contains(action)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\":\"action not allowed\"}");
        }

        try {
            if (action == "add")
                UserService.addPlaceToFavourites(u, place);
            else if (action == "remove")
                UserService.removePlaceFromFavourites(u, place);
            result = ResponseEntity.status(HttpStatus.OK).body("{\"Success\":\" correctly updated the favourites\"}");
        }catch (DatabaseErrorException e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in querying Databases\"}");
        }catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in updating favourites\"}");
        }

        return result;
    }
    // add to visited a place (wants the id of the place)
    @ApiOperation(value = "adds the specified place to the visited places of the currently logged in user")
    @PostMapping(value = "/place/{id}/visit", produces = "application/json")
    public ResponseEntity<?> handleVisited(@PathVariable(name="id") String placeId, @RequestBody(required = false) LocalDateTime localDateTime) {
        ResponseEntity<?> result;

        if(localDateTime == null){
            // the time of the visit will be considered now
            localDateTime = LocalDateTime.now();
        }
        try {
            //should retrieve the place object (and check if it exists)
            Place place = PlaceService.getPlaceFromId(placeId);
            if (place == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\":\"the specified place does not exist\"}");
            }
            //retrieve the current user
            UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User u = userSpring.getApplicationUser();
            UserService.addPlaceToVisited(u, place, localDateTime);
            result = ResponseEntity.status(HttpStatus.OK).body("{\"Success\":\" correctly added the visited place\"}");
        }catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in inserting visited place\"}");
        }

        return result;
    }
}