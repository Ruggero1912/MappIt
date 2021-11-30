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

}
