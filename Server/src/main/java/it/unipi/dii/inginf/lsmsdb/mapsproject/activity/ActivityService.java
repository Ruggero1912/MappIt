package it.unipi.dii.inginf.lsmsdb.mapsproject.activity;

import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information.ActivityManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information.ActivityManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information.ActivityManagerMongoDB;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.social.PlaceSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.social.PlaceSocialManagerFactory;

import java.util.Arrays;
import java.util.List;

public class ActivityService {

    public static List<String> allActivitiesNames = ActivityService.getActivitiesNames();

    public static List<String> getActivitiesNames(){
        ActivityManager am = ActivityManagerFactory.getActivityManager();
        return am.retrieveActivitiesName();
    }

    public static boolean checkIfActivityExists(String activityName){
        return allActivitiesNames.contains(activityName);
    }
}
