package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

public class PlaceServiceTest {


    @DisplayName("Test PlaceService.getPlacesInRadius")
    @Test
    void GIVEN_getPlacesInRadius_WHEN_correct_parameters_are_passed_THEN_return_list_of_places(){
        Coordinate coord = new Coordinate(43.6949891,10.3944083 );
        Double radius = 10.0;
        String orderBy = PlaceService.defaultOrderByCriteria;
        List<Place> places = PlaceService.getPlacesInRadius(coord, radius, orderBy);
        for(Place p : places){
            System.out.println(p.toString());
        }
    }

    @DisplayName("Test PlaceService.getSuggestedPlaces correct behaviour")
    @Test
    void GIVEN_get_Suggested_Places_WHEN_correct_parameters_are_passed_THEN_return_list_of_suggested_places(){
        User u = UserService.getUserFromId("61d3428101336eeafcb438e5");
        List<Place> places = PlaceService.getSuggestedPlaces(u);
        for(Place p : places){
            System.out.println(p.toString());
        }
    }

    @DisplayName("Test PlaceService.getPopularPlaces correct behaviour")
    @Test
    void GIVEN_get_popular_places_WHEN_correct_parameters_are_passed_THEN_return_list_of_popular_places(){
        User u = UserService.getUserFromId("61d3428101336eeafcb438e5");
        List<Place> places = PlaceService.getPopularPlaces("generic", 10);
        for(Place p : places){
            System.out.println(p.toString());
        }
    }
}
