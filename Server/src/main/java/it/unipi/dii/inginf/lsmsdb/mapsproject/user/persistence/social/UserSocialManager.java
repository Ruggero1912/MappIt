package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManagerFactory;
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
     * return true if the user deletion process is successful, else return false
     * @param id is the id of the user to be deleted
     * @return true if the deletion is successful, false otherwise
     */
    boolean deleteUserFromId(String id) throws DatabaseErrorException;

    /**
     * returns a List of places marked as Favourite by the given user, else return null
     * @param user object for whom we want to know the favourite places
     * @return the List f the favourite places if any, else return null
     */
    List<Place> retrieveFavouritePlaces(User user);

    /**
     * returns the list of places that the specified user has visited
     * @param user the user owner of the favourites list
     * @return list of Places or null if the list is empty or if the user param is null
     */
    List<Place> retrieveVisitedPlaces(User user);

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

    /**
     * makes the user follows the userToFollow
     * @param user the user that follows
     * @param userToFollow the user to follow
     * @param timestampFollow the timestamp related to the follow relationship
     * @return true if the user correctly follows the userToFollow, else false
     */
    boolean storeNewFollower(User user, User userToFollow, LocalDateTime timestampFollow);

    /**
     * makes the user unfollows the userToUnfollow
     * @param user user that unfollows
     * @param userToUnfollow user to unfollow
     * @return LocalDateTime containing the timestamp of the deleted relationship if the user correctly unfollows the userToUnfollow, else null
     */
    boolean deleteFollower(User user, User userToUnfollow);

    /**
     * return all the posts of an user in the db, given the user obj
     * @return Posts List or null if there are no posts
     */
    List<Post> retrieveAllPosts(User user);
}
