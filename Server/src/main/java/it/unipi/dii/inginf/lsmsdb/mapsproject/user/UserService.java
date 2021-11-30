package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

public class UserService {

    /**
     * return the User associated to the given credentials if exists, else return Null
     * @param username A string containing the given username from the user
     * @param password a string containing the given password from the user
     * //@exception Any exception
     * @return User object or null if not found
     */
    public static User login(String username, String password){
        UserManager um = UserManagerFactory.getUserManager();
        //here we should cypher the password
        String encryptedPwd = password;
        User u = um.login(username, encryptedPwd);
        return u;
    }

    public static boolean save(User newUser) {
        //...
        return true;
    }
}
