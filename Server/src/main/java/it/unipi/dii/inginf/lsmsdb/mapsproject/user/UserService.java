package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

import java.time.LocalDate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    /**
     * return the User associated to the given credentials if password hashes match, return Null otherwise
     * @param username A string containing the given username from the user
     * @param password a string containing the given password from the user
     * //@exception Any exception
     * @return User object if credentials match or null otherwise
     */
    public static User login(String username, String password){
        UserManager um = UserManagerFactory.getUserManager();

        User u = um.getUserFromUsername(username);
        String pwdHash = u.getPassword();

        if (UserService.checkPassword(password, pwdHash)) {
            return u;
        }
        return null;
    }

    /**
     * return null if the username or email already exist, otherwise return inserted user instance
     * @param username A string containing the given username from the user
     * @param passwordHash a string containing the given hashed password
     * @param name a string containing the given name from the user
     * @param surname a string containing the given surname from the user
     * @param email a string containing the given email from the user
     * @param birthDate a date containing the given birthDate from the user
     * //@exception Any exception
     * @return user object or null if already exists
     */
    public static User register(String username, String passwordHash, String name, String surname, String email, Date birthDate) {

        final User.Role role = User.Role.USER;
        final Image profilePic = new Image();
        UserManager um = UserManagerFactory.getUserManager();

        //try to find an existing user associated to the given username, if it exists return true
        boolean duplicateUsername = um.checkDuplicateUsername(username);
        //try to find an existing user associated to the given email, if it exists return true
        boolean duplicateEmail = um.checkDuplicateEmail(email);
        if(duplicateUsername || duplicateEmail ){
            return null;
        }

        User insertedUser = um.storeUser(username, passwordHash, name, surname, email, birthDate, role, profilePic);
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
        boolean ret = BCrypt.checkpw(password, passwordHash);
        return ret;
    }
}
