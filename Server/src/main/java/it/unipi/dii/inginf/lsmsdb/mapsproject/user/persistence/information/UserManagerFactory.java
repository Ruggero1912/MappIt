package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class UserManagerFactory {

    private static final Logger LOGGER = Logger.getLogger( UserManagerFactory.class.getName() );

    private UserManagerFactory(){

    }

    public static UserManager getUserManager(){
        UserManager u;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.documentDBkey);
        switch (dbKind){
            case PropertyPicker.documentDbMongoKey:
                u = new UserManagerMongoDB();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.documentDBkey});
                throw new IllegalArgumentException();
        }
        return u;
    }
}
