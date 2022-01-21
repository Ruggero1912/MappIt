package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;

import java.util.List;

public interface UserManager {

    /**
     * return the User associated to the given username if exists, else return Null
     * @param username A string containing the given username from the user
     * @return User object or null if not found
     */
    User getUserFromUsername(String username);

    /**
     * return all the Users in database
     * @return User List or null if there are no users
     */
    List<User> getAllUser();

    /**
     * return true if the User with given username already exists, else return false
     * @param username A string containing the given username from the user
     * @return true if given username is already taken, false otherwise
     */
    boolean checkDuplicateUsername(String username);

    /**
     * return true if the User with given email already exists, else return false
     * @param email A string containing the given email from the user
     * @return true if given email is already taken, false otherwise
     */
    boolean checkDuplicateEmail(String email);

    /**
     * return the stored User, else return null
     * @param newUser object with information required for registration process
     * @return stored User, else return null
     */
    User storeUser(RegistrationUser newUser);

    /**
     * return null if it does not exists an user with that id, otherwise returns the associated user
     * @param id the id of the requested user
     * @return associated User object or null if not found
     */
    User getUserFromId(String id);

    /**
     * return true if the user deletion process is successful, else return false
     * @param id is the id of the user to be deleted
     * @return true if the deletion is successful, false otherwise
     */
    boolean deleteUserFromId(String id);

    /**
     * return true if given password hash match stored hash, else return false
     * @param id of the current user
     * @param newPassword
     * @return true if password change has been successful, false otherwise
     */
    boolean changePassword(String id, String newPassword);

    /**
     * return true if followers counter was correctly increased, else return false
     * @param user we want to increase the followers number
     * @param k value of the increment or decrement
     * @return true if followers counter was correctly increased, false otherwise
     */
    boolean updateFollowersCounter(User user, int k);

    /**
     * return a list of the most active users ordered by the number of posts written, given a certain category
     * @param activityFilter: the name of the activity that the returned places should fit or "any"
     * @param maxQuantity: the maximum number of users instances to be returned
     * @return a list of Document containing the user's aggregate values
     */
    List<Document> retrieveMostActiveUsers(String activityFilter, int maxQuantity);
}
