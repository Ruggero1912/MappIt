package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class UserSocialManagerNeo4jTest {

    UserSocialManagerNeo4j user;

    @BeforeEach
    void init(){
        user = new UserSocialManagerNeo4j();
    }

    @DisplayName("Test UserSocialManager.storeUser already inserted user is passed")
    @Test
    void GIVEN_store_user_WHEN_already_inserted_user_is_passed_THEN_throws_exception(){
        User usr = UserService.getUserFromId("61d3428101336eeafcb438e5");
        Assertions.assertThrows( DatabaseConstraintViolation.class, () -> {
            user.storeUser(usr);
        });
    }

    @DisplayName("Test UserSocialManager.storeUser empty user is passed")
    @Test
    void GIVEN_store_user_WHEN_empty_user_is_passed_THEN_throws_exception() {
        User usr = mock(User.class);
        try {
            assertNull(user.storeUser(usr));
        } catch (DatabaseConstraintViolation e) {
            e.printStackTrace();
        }
    }

    @DisplayName("Test UserSocialManager.retrieveFavouritePlaces when empty user is passed")
    @Test
    void GIVEN_retrieve_favourite_places_WHEN_empty_user_is_passed_THEN_return_empty_list() {
        User usr = mock(User.class);
        List<Place> places = user.retrieveFavouritePlaces(usr);
        places.isEmpty();
    }

    @DisplayName("Test UserSocialManager.retrieveVisitedPlaces when empty user is passed")
    @Test
    void GIVEN_retrieve_visited_places_WHEN_empty_user_is_passed_THEN_return_empty_list() {
        User usr = mock(User.class);
        List<Place> places = (user.retrieveVisitedPlaces(usr));
        places.isEmpty();
    }

    @DisplayName("Test UserSocialManager.addPlaceToFavourites normal behaviour")
    @Test
    void GIVEN_add_favourite_place_WHEN_correct_parameters_are_passed_THEN_return_true() {
        User u = UserService.getUserFromId("61d3428101336eeafcb438e5");
        Place p = PlaceService.getPlaceFromId("61d340a6a1dc9d89ac02fa03");
        boolean res = false;
        try {
            res = UserService.addPlaceToFavourites(u,p);
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(res);
    }

    @DisplayName("Test UserSocialManager.addPlaceToFavourites when empty user/place is passed")
    @Test
    void GIVEN_add_favourite_place_WHEN_empty_user_or_empty_place_passed_THEN_return_false() {
        User u = null;
        Place p = null;
        boolean res = false;
        try {
            res = UserService.addPlaceToFavourites(u,p);
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
        }
        Assertions.assertFalse(res);
    }

    @DisplayName("Test UserSocialManager.removePlaceFromFavourites")
    @Test
    void GIVEN_remove_place_from_favourites_WHEN_correct_parameters_are_passed_THEN_return_true() {
        User u = UserService.getUserFromId("61d3428101336eeafcb438e5");
        Place p = PlaceService.getPlaceFromId("61d340a6a1dc9d89ac02fa03");
        boolean res = false;
        try {
            res = UserService.removePlaceFromFavourites(u,p);
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(res);
    }

    @DisplayName("Test UserSocialManager.addPlaceToVisited")
    @Test
    void GIVEN_add_visited_place_WHEN_correct_parameters_are_passed_THEN_return_true() {
        User u = UserService.getUserFromId("61d3428101336eeafcb438e5");
        Place p = PlaceService.getPlaceFromId("61d340a6a1dc9d89ac02fa03");
        boolean res = UserService.addPlaceToVisited(u,p, LocalDateTime.now());
        Assertions.assertTrue(res);
    }

    @DisplayName("Test UserSocialManager.addPlaceToVisited when empty user/place is passed")
    @Test
    void GIVEN_add_visited_place_WHEN_empty_user_or_empty_place_passed_THEN_return_false() {
        User u = null;
        Place p = null;
        boolean res = UserService.addPlaceToVisited(u,p, null);
        Assertions.assertFalse(res);
    }
}
