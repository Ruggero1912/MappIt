package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

import java.time.LocalDate;
import java.util.Date;

public interface UserManager {

    /**
     * return the User associated to the given credentials if exists, else return Null
     * @param username A string containing the given username from the user
     * @param encryptedPassword a string containing the given password from the user
     * @return User object or null if not found
     */
    User login(String username, String encryptedPassword);

    /**
     * return true if the User with given username already exists, else return false
     * @param username A string containing the given username from the user
     * @param email A string containing the given email from the user
     * @return true if User already exists or false if not exists
     */
    boolean checkDuplicateUser( String username, String email);

    /**
     * return the stored User, else return null
     * @param username A string containing the given username from the user
     * @param passwordHash a string containing the given passwordHash from the user
     * @param email A string containing the given email from the user
     * @param name a string containing the given name from the user
     * @param surname a string containing the given surname from the user
     * @param birthDate a date containing the given birthDate from the user
     * @param defRole a default role given to the user
     * @param defPic a default image given to the user
     * @return stored User, else return null
     */
    User storeUser(String username, String passwordHash, String name, String surname, String email, LocalDate birthDate, User.Role defRole, Image defPic);

    /**
     * return null if it does not exists an user with that id, otherwise returns the associated user
     * @param id the id of the requested user
     * @return associated User object or null if not found
     */
    User getUserFromId(String id);

}
