package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerNeo4j;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    /**
     * return the User associated to the given credentials if password hashes match, return Null otherwise
     * @param username A string containing the given username from the user
     * @param password a string containing the given password from the user
     * //@exception Any exception
     * @return User object if credentials match or null otherwise
     */
    public static User login(String username, String password) {
        UserManager um = UserManagerFactory.getUserManager();

        User u = um.getUserFromUsername(username);
        if (u != null) {
            String pwdHash = u.getPassword();
            if (UserService.checkPassword(password, pwdHash)) {
                return u;
            }
        }

        return null;
    }

    /**
     * return null if the username or email already exist, otherwise return inserted user instance
     * @param newRegistrationUser object with information required for registration process
     * //@exception Any exception
     * @return user object or null if already exists
     */
    public static User register(RegistrationUser newRegistrationUser) throws DatabaseErrorException, DatabaseConstraintViolation {

        //check restrictions on password
        String password = newRegistrationUser.getPassword();
        if( password == null || password.length() <= 4){
            return null;
        }

        //strings for duplicates
        String username = newRegistrationUser.getUsername();
        String email = newRegistrationUser.getEmail();

        UserManager um = UserManagerFactory.getUserManager();

        //try to find an existing user associated to the given username, if it exists return true
        boolean duplicateUsername = um.checkDuplicateUsername(username);
        //try to find an existing user associated to the given email, if it exists return true
        boolean duplicateEmail = um.checkDuplicateEmail(email);

        if(duplicateUsername || duplicateEmail ){
            return null;
        }

       // try to add the new User on Mongo DB
       User insertedUserInMongo = um.storeUser(newRegistrationUser);

        if(insertedUserInMongo == null){
            LOGGER.log(Level.SEVERE, "Error during registration: Mongo insertion failed!");
            throw new DatabaseErrorException("Mongo insertion failed");
        }

        // try to add the new User on Neo4j
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        User insertedUserInNeo4j;

        try {
            insertedUserInNeo4j = usm.storeUser(insertedUserInMongo);
        } catch (DatabaseConstraintViolation dcv) {
            LOGGER.log(Level.SEVERE, "Error during registration: Neo4j insertion failed due to constraint violation!");
            throw new DatabaseConstraintViolation("Neo4j constraint violation");
        } catch (Neo4jException ne){
            throw ne;
        }

        if(insertedUserInNeo4j == null){
            // we have to delete the user inside Mongo because Neo4j insertion failed
            delete(insertedUserInMongo);

            LOGGER.log(Level.SEVERE, "Error during registration: Neo4j insertion failed!");
            throw new DatabaseErrorException("Neo4j insertion failed");
        }

        return insertedUserInNeo4j;
    }

    /**
     * return all the Users in database
     * @return User List or null if there are no users
     */
    public static List<User> getAllUsers(){
        UserManager um = UserManagerFactory.getUserManager();
        return um.getAllUser();
    }

    /**
     * return null if it does not exist a user with that id, otherwise returns the associated user
     * @param id the id of the requested user
     * @return associated User object or null if not found
     */
    public static User getUserFromId(String id){
        UserManager um = UserManagerFactory.getUserManager();
        return um.getUserFromId(id);
    }

    /**
     * return hashed password
     * @param password from the user
     * @return hashed password to store
     */
    public static String passwordEncryption(String password){

        String pwdHash = BCrypt.hashpw(password, BCrypt.gensalt());
        return pwdHash;
    }

    /**
     * return true if given password hash match stored hash, else return false
     * @param password from the user
     * @param passwordHash stored in the DB
     * @return true if passwords match o false otherwise
     */
    public static boolean checkPassword(String password, String passwordHash){
        try {
            return BCrypt.checkpw(password, passwordHash);
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
     * return true if given password hash match stored hash, else return false
     * @param userToDelete is the user to be deleted
     * @return true if deletion has been successful, false otherwise
     */
    public static boolean delete(User userToDelete) throws DatabaseErrorException {
        if(userToDelete == null || userToDelete.getId() == null)
            return false;

        UserManager um = UserManagerFactory.getUserManager();
        String userId = userToDelete.getId();
        boolean UserDeletedFromMongo = um.deleteUserFromId(userId);

        //TODO: handle the deletion of all the posts published by the user to delete

        if(!UserDeletedFromMongo){
            LOGGER.log(Level.SEVERE, "Error during delete: Mongo user deletion failed!");
            throw new DatabaseErrorException("Mongo user deletion failed");
        }

        //Try to delete also in Neo4j for maintaining the db consistency
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        boolean UserDeletedFromNeo4j;
        try{
            UserDeletedFromNeo4j = usm.deleteUserFromId(userId);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during delete: Neo4j user deletion failed!");
            return false;
        }

        return UserDeletedFromNeo4j;
    }

    /**
     * return true if given password hash match stored hash, else return false
     * @param userID is the ID of the current user
     * @param newPassword
     * @return true if password change has been successful, false otherwise
     */
    public static boolean updatePassword(String userID, String newPassword){
        if(newPassword == "" || userID == "" || newPassword.length() <= 4){
            return false;
        }

        UserManager um = UserManagerFactory.getUserManager();
        boolean ret = um.changePassword(userID, newPassword);
        return ret;
    }

    /**
     * returns the list of places that the specified user has added to its favourites
     * @param user the user owner of the favourites list
     * @return list of Places or null if the list is empty or if the user param is null
     */
    public static List<Place> getFavouritePlaces(User user){
        if(user == null){
            return null;
        }
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        return usm.retrieveFavouritePlaces(user);
    }
    /**
     * returns the list of places that the specified user has visited
     * @param user the user owner of the favourites list
     * @return list of Places or null if the list is empty or if the user param is null
     */
    public static List<Place> getVisitedPlaces(User user){
        if(user == null){
            return null;
        }
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        return usm.retrieveVisitedPlaces(user);
    }
    /**
     * adds the specified place to the favourite places of the specified user
     * @param user the user owner of the favourites list
     * @param place the place to add
     * @return true if the place is correctly added, else false
     */
    public static boolean addPlaceToFavourites(User user, Place place) throws DatabaseErrorException {
        if(user == null || place == null){
            return false;
        }

        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        boolean addedNewFavPlaceNeo4j = usm.storeNewFavouritePlace(user, place);
        if(!addedNewFavPlaceNeo4j){
            LOGGER.log(Level.SEVERE, "Error during insert: Neo4j new favourite place failed!");
            throw new DatabaseErrorException("Neo4j new favourite place insert failed");
        }

        PlaceManager pm = PlaceManagerFactory.getPlaceManager();
        boolean incrementFavCounterMongo;
        String placeId = place.getId();
        try{
            incrementFavCounterMongo = pm.updateFavouriteCounter(placeId, 1);
            if(!incrementFavCounterMongo){
                //we have to delete the relationship in Neo4j
                LOGGER.log(Level.SEVERE, "Error during new favourite place removing: MongoDB update failed!");
                usm.deleteFavouritePlace(user, place);
                return false;
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during delete: Neo4j user deletion failed!");
            return false;
        }

        return true;
    }
    /**
     * removes the specified place from the favourite places of the specified user
     * @param user the user owner of the favourites list
     * @param place the place to add
     * @return true if the place is correctly removed, else false
     */
    public static boolean removePlaceFromFavourites(User user, Place place) throws DatabaseErrorException {
        if(user == null || place == null){
            return false;
        }
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        boolean removedFavPlaceNeo4j = usm.deleteFavouritePlace(user, place);
        if(!removedFavPlaceNeo4j){
            LOGGER.log(Level.SEVERE, "Error during insert: Neo4j new favourite place failed!");
            throw new DatabaseErrorException("Neo4j new favourite place insert failed");
        }

        PlaceManager pm = PlaceManagerFactory.getPlaceManager();
        boolean decrementFavCounterMongo;
        String placeId = place.getId();
        try{
            decrementFavCounterMongo = pm.updateFavouriteCounter(placeId, -1);
            if(!decrementFavCounterMongo){
                //we have to delete the relationship in Neo4j
                LOGGER.log(Level.SEVERE, "Error during new favourite place adding: MongoDB update failed!");
                usm.storeNewFavouritePlace(user, place);
                return false;
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during delete: Neo4j user deletion failed!");
            return false;
        }

        return true;
    }
    /**
     * adds the place to the visited places of the specified user
     * @param user the user owner of the visited list
     * @param place the place to add
     * @return true if the place is correctly added, else false
     */
    public static boolean addPlaceToVisited(User user, Place place, LocalDateTime timestampVisit){
        if(user == null || place == null){
            return false;
        }
        if(timestampVisit == null){
            timestampVisit = LocalDateTime.now();
        }
        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        return usm.storeNewVisitedPlace(user, place, timestampVisit);
    }

    /**
     * makes the user follows the userToFollow
     * @param user the user that follows
     * @param userToFollow the user to follow
     * @param timestampFollow the timestamp related to the follow relationship
     * @return true if the user correctly follows the userToFollow, else false
     */
    public static boolean followUser( User user, User userToFollow, LocalDateTime timestampFollow){
        if(userToFollow == null || user == null){
            return false;
        }
        if(timestampFollow == null){
            timestampFollow = LocalDateTime.now();
        }

        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        boolean storedInNeo = usm.storeNewFollower(user, userToFollow, timestampFollow);
        if(!storedInNeo){
            LOGGER.log(Level.SEVERE, "Error during following: Neo4j relationship insertion failed!");
            return false;
        }

        UserManager um = UserManagerFactory.getUserManager();
        boolean updatedInMongo = um.updateFollowersCounter(userToFollow, 1);
        if(!updatedInMongo){
            //we have to delete the relationship in Neo4j
            LOGGER.log(Level.SEVERE, "Error during following: MongoDB update failed!");
            usm.deleteFollower(user, userToFollow);
            return false;
        }
        return true;
    }

    /**
     * makes the user unfollows the userToUnfollow
     * @param user the user that unfollows
     * @param userToUnfollow the user to unfollow
     * @return true if the user unfollows the userToUnfollow, else false
     */
    public static boolean unfollowUser( User user, User userToUnfollow){
        if(userToUnfollow == null || user == null){
            return false;
        }

        UserManager um = UserManagerFactory.getUserManager();
        boolean updatedFollowCounterInMongo = um.updateFollowersCounter(userToUnfollow, -1);
        if(!updatedFollowCounterInMongo){
            LOGGER.log(Level.SEVERE, "Error during unfollowing: MongoDB update failed!");
            return false;
        }

        UserSocialManager usm = UserSocialManagerFactory.getUserManager();
        boolean deletedFollowRelationshipNeo = usm.deleteFollower(user, userToUnfollow);
        if(!deletedFollowRelationshipNeo){
            //restore the follower counter value
            LOGGER.log(Level.SEVERE, "Error during unfollowing: Neo4j relationship deletion failed!");
            um.updateFollowersCounter(userToUnfollow, 1);
            return false;
        }

        return true;
    }
}
