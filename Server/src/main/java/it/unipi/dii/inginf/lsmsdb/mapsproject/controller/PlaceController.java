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
    @GetMapping(value = "/place/{placeId}", produces = "application/json")
    public ResponseEntity<?> getPlaceById(@PathVariable(value = "placeId") String placeId) {
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
                                    @RequestParam( defaultValue = "distance") String orderBy,
                                    @RequestParam(defaultValue = "0", name = "limit") int maxQuantity) {
        ResponseEntity<?> result;
        List<Place> nearbyPlaces;

        try {
            // check the validity of the given radius (in km)
            if(radius > 100){
                result = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\" : \"The specified radius '" + radius + "' km is too high!\"}");
                return result;
            }else if(radius < 0){
                result = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\" : \"The specified radius '" + radius + "' is a negative number\"}");
                return result;
            }
            // parse the coordinates from lon, lat
            Coordinate coordinates = new Coordinate(lat, lon);

            // the orderBy String content is checked by PlaceService
            if (activityFilter == "any") {
                nearbyPlaces = PlaceService.getPlacesInRadius(coordinates, radius, orderBy, maxQuantity);
            } else {
                nearbyPlaces = PlaceService.getPlacesInRadius(coordinates, radius, orderBy, activityFilter, maxQuantity);
            }

            result = ResponseEntity.status(HttpStatus.OK).body(nearbyPlaces);
        }catch(UndefinedActivityException u){
            result = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\" : \"The specified activity '" + activityFilter + "' was not recognised!\"}");
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


    /**
     * @param placeName is the place name suffix from which the method will search
     * @param maxQuantity is the quantity of places to be returned
     * notes = "This method retrieve places that has a name which is equal or that contains the one given")
     */
    @GetMapping(value = "/places/find", produces = "application/json")
    public ResponseEntity<?> findPlaces(@RequestParam(defaultValue = "placeName") String placeName, @RequestParam(defaultValue = "10", name = "limit") int maxQuantity) {
        ResponseEntity<?> result;
        try{
            List<Place> placesMatching = PlaceService.findPlacesFromName(placeName, maxQuantity);
            result = ResponseEntity.ok(placesMatching);
            if(placesMatching==null) {
                LOGGER.log(Level.WARNING, "Error: could not find any places with the specified name");
                result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find any places with the specified name\"}");
            }
        }catch (Exception e){
            LOGGER.log(Level.WARNING, "Error: could not find places with that name, an exception has occurred: " + e);
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find places with that name, and exception has occurred\"}");
        }
        return result;
    }
}