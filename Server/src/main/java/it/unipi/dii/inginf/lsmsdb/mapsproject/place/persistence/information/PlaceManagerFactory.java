package it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlaceManagerFactory {

    private static final Logger LOGGER = Logger.getLogger(PlaceManagerFactory.class.getName() );

    private PlaceManagerFactory(){
    }

    public static PlaceManager getPlaceManager(){
        PlaceManager p;
        String dbKind = PropertyPicker.getProperty(PropertyPicker.documentDBkey);
        switch (dbKind){
            case PropertyPicker.documentDbMongoKey:
                p = new PlaceManagerMongoDB();
                break;

            default:
                LOGGER.log(Level.SEVERE, "Value {0} for the key {1} not valid", new Object[]{dbKind, PropertyPicker.documentDBkey});
                throw new IllegalArgumentException();
        }
        return p;
    }
}
