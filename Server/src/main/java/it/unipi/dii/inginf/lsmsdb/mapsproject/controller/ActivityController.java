package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private static final Logger LOGGER = Logger.getLogger( ActivityController.class.getName() );

    /**
     * return all the Activities in the database
     * notes = "This method retrieve information about all the activities available")
     */
    @ApiOperation(value = "Get all the available Activities", notes = "This method returns all the feasible activities for a post")
    @GetMapping(value = "/activities/all", produces = "application/json")
    public ResponseEntity<?> getActivities() {
        ResponseEntity<?> result;

        try{
            List<String> activities = ActivityService.getActivitiesNames();
            result = ResponseEntity.ok(activities);
            if(activities==null) {
                LOGGER.log(Level.WARNING, "Empty activities list");
                result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not retrieve activities\"}");
            }
        } catch (Exception e){
            LOGGER.log(Level.WARNING, "Error: could not parse activities, an exception has occurred: " + e);
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not parse activities, and exception has occurred\"}");
        }
        return result;
    }

}
