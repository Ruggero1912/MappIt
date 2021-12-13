package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;

import java.util.List;
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
     * @param newRegistrationUser object with information required for registration process
     * //@exception Any exception
     * @return user object or null if already exists
     */
    public static User register(RegistrationUser newRegistrationUser) {
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

        User insertedUser = um.storeUser(newRegistrationUser);
        return insertedUser;
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
        boolean ret = BCrypt.checkpw(password, passwordHash);
        return ret;
    }

    /**
     * return true if given password hash match stored hash, else return false
     * @param userToDelete is the user to be deleted
     * @return true if deletion has been successful, false otherwise
     */
    public static boolean delete(User userToDelete){
        UserManager um = UserManagerFactory.getUserManager();
        String userId = userToDelete.getId();
        boolean ret = um.deleteUserFromId(userId);
        return ret;
    }

    /**
     * return true if given password hash match stored hash, else return false
     * @param userID is the ID of the current user
     * @param newPassword
     * @return true if password change has been successful, false otherwise
     */
    public static boolean updatePassword(String userID, String newPassword){
        UserManager um = UserManagerFactory.getUserManager();
        boolean ret = um.changePassword(userID, newPassword);
        return ret;
    }
}
