package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

import java.time.LocalDate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    /**
     * return the User associated to the given credentials if exists, else return Null
     * @param username A string containing the given username from the user
     * @param encryptedPassword a string containing the given password from the user
     * //@exception Any exception
     * @return User object or null if not found
     */
    public static User login(String username, String encryptedPassword){
        UserManager um = UserManagerFactory.getUserManager();
        //here we should cypher the password
        User u = um.login(username, encryptedPassword);
        return u;
    }

    /**
     * return null if the username or email already exist, otherwise return inserted user instance
     * @param username A string containing the given username from the user
     * @param passwordHash a string containing the given password from the user
     * @param name a string containing the given name from the user
     * @param surname a string containing the given surname from the user
     * @param email a string containing the given email from the user
     * @param birthDate a date containing the given birthDate from the user
     * //@exception Any exception
     * @return user object or null if already exists
     */
    public static User register(String username, String passwordHash, String name, String surname, String email, LocalDate birthDate) {
        LOG.log(Level.SEVERE, "Call to UserService.register()");
        final User.Role defaultRole = User.Role.USER;
        final Image defaultProfilePic = new Image();
        UserManager um = UserManagerFactory.getUserManager();

        //TODO:
        //try to find an existing user associated to the given username, if it exists, return null
        //make this check also on the email address

        boolean duplicateExists = um.checkDuplicateUser(username, email);
        if(duplicateExists){
            return null;
        }

        User insertedUser = um.storeUser(username, passwordHash, name, surname, email, birthDate, defaultRole, defaultProfilePic);
        return insertedUser;
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
}
