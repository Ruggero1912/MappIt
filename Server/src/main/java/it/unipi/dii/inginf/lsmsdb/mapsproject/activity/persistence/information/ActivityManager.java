package it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information;

import java.util.List;

public interface ActivityManager {


    /**
     * return all the Activities in database
     * @return Activity List or null if there are no activities
     */
    List<String> retrieveActivitiesName();
}
