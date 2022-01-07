package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSocialManager {

    /**
     * return the stored User, else return null
     * @param newUser object with information required for registration process
     * @return stored User, else return null
     */
    User storeUser(User newUser) throws DatabaseConstraintViolation;

    /**
     * returns a List of places marked as Favourite by the given user, else return null
     * @param user object for whom we want to know the favourite places
     * @return the List f the favourite places if any, else return null
     */
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    List<Place> retrieveFavouritePlaces(User user);

    /**
     * returns the list of places that the specified user has visited
     * @param user the user owner of the favourites list
     * @return list of Places or null if the list is empty or if the user param is null
     */
    //TODO: decide whether it would make sense to make this method return List<PlacePreview> instead of List<Place> as Neo4j only have place's id & name available
    List<Place> retrieveVisitedPlaces(User user);

    /**
     * checks whether a relationship between a user and a place already exists
     * @param user node
     * @param place node
     * @return true if there is already a relationship connecting the two nodes
     */
    boolean checkAlreadyExistingRelationship(User user, Place place, String relationshipKind);

    /**
     * adds the specified place to the favourite places of the specified user
     * @param user the user owner of the favourites list
     * @param place the place to add
     * @return true if the place is correctly added, else false
     */
    boolean storeNewFavouritePlace(User user, Place place);

    /**
     * removes the specified place from the favourite places of the specified user
     * @param user the user owner of the favourites list
     * @param place the place to add
     * @return true if the place is correctly removed, else false
     */
    boolean deleteFavouritePlace(User user, Place place);

    /**
     * adds the place to the visited places of the specified user
     * @param user the user owner of the visited list
     * @param place the place to add
     * @return true if the place is correctly added, else false
     */
    boolean storeNewVisitedPlace(User user, Place place, LocalDateTime timestampVisit);
}
