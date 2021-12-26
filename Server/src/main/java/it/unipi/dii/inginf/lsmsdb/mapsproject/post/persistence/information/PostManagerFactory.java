package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostManagerFactory {

    private static final Logger LOGGER = Logger.getLogger(PostManagerFactory.class.getName() );

    public PostManagerFactory(){
    }

    public static PostManager getPostManager(){
        PostManager p;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.documentDBkey);
        switch (dbKind){
            case PropertyPicker.documentDbMongoKey:
                p = new PostManagerMongoDB();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.documentDBkey});
                throw new IllegalArgumentException();
        }
        return p;
    }
}
