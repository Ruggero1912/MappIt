package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

public interface UserManager {

    /**
     * return the User associated to the given username if exists, else return Null
     * @param username A string containing the given username from the user
     * @return User object or null if not found
     */
    User getUserFromUsername(String username);

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

}
