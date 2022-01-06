package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSocialManagerFactory {

    private static final Logger LOGGER = Logger.getLogger( UserSocialManagerFactory.class.getName() );

    private UserSocialManagerFactory(){

    }

    public static UserSocialManager getUserManager(){
        UserSocialManager u;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.graphDBkey);
        switch (dbKind){
            case PropertyPicker.graphDbNeoKey:
                u = new UserSocialManagerNeo4j();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.graphDBkey});
                throw new IllegalArgumentException();
        }
        return u;
    }
}
