package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

public interface UserSocialManager {

    /**
     * return the stored User, else return null
     * @param newUser object with information required for registration process
     * @return stored User, else return null
     */
    User storeUser(User newUser);
}
