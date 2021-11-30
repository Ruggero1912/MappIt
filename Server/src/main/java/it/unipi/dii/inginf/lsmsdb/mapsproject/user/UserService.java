package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

import java.util.Date;

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
    /**
     * return null if the username already exists, otherwise return the created User object
     * @param username A string containing the given username from the user
     * @param password a string containing the given password from the user
     * //@exception Any exception
     * @return User object or null if already exists
     */
    public static User register(String username, String password, String name, String surname, String email, Date birthDate) {
        final User.Role defaultRole = User.Role.USER;
        final String defaultProfilePic = PropertyPicker.getProperty(PropertyPicker.defaultPicKey);

        UserManager um = UserManagerFactory.getUserManager();
        //TODO:
        //try to find an existing user associated to the given username, if it exists, return null
        //make this check also on the email address
        //User u = um.register(params..);
        return null;
    }
}
