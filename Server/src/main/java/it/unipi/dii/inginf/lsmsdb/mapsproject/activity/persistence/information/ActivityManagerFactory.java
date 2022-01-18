package it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityManagerFactory {

    private static final Logger LOGGER = Logger.getLogger( ActivityManagerFactory.class.getName() );

    private ActivityManagerFactory(){

    }

    public static ActivityManager getActivityManager(){
        ActivityManager a;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.documentDBkey);
        switch (dbKind){
            case PropertyPicker.documentDbMongoKey:
                a = new ActivityManagerMongoDB();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.documentDBkey});
                throw new IllegalArgumentException();
        }
        return a;
    }
}
