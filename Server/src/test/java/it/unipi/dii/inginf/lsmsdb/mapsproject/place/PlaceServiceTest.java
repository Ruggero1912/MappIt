package it.unipi.dii.inginf.lsmsdb.mapsproject.place;

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
}
