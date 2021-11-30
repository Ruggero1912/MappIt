package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

public interface UserManager {

    /**
     * return the User associated to the given credentials if exists, else return Null
     * @param username A string containing the given username from the user
     * @param encryptedPassword a string containing the given password from the user
     * @return User object or null if not found
     */
    public User login(String username, String encryptedPassword);


    /**
     * return null if it does not exists an user with that id, otherwise returns the associated user
     * @param id the id of the requested user
     * @return associated User object or null if not found
     */
    public User getUserFromId(String id);

}
