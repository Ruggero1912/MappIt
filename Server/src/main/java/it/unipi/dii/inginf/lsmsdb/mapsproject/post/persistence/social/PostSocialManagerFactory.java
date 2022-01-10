package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerNeo4j;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PostSocialManagerFactory {

    private static final Logger LOGGER = Logger.getLogger( PostSocialManagerFactory.class.getName() );

    public static PostSocialManager getPostManager(){
        PostSocialManager p;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.graphDBkey);
        switch (dbKind){
            case PropertyPicker.graphDbNeoKey:
                p = new PostSocialManagerNeo4j();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.graphDBkey});
                throw new IllegalArgumentException();
        }
        return p;
    }
}
