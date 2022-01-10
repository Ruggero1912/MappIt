package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerNeo4j;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceSocialManagerFactory {

    private static final Logger LOGGER = Logger.getLogger( PlaceSocialManagerFactory.class.getName() );

    private PlaceSocialManagerFactory(){

    }

    public static PlaceSocialManager getPlaceManager(){
        PlaceSocialManager p;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.graphDBkey);
        switch (dbKind){
            case PropertyPicker.graphDbNeoKey:
                p = new PlaceSocialManagerNeo4j();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.graphDBkey});
                throw new IllegalArgumentException();
        }
        return p;
    }
}
