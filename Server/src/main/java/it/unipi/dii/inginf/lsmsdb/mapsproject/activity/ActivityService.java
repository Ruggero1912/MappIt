package it.unipi.dii.inginf.lsmsdb.mapsproject.activity;

import java.util.Arrays;
import java.util.List;

public class ActivityService {

    public static List<String> allActivitiesNames = ActivityService.getActivitiesNames();

    private static List<String> getActivitiesNames(){
        // TODO: load activities from file or statically
        return Arrays.asList("drone", "generic", "running", "trekking");
    }

    public static boolean checkIfActivityExists(String activityName){
        return allActivitiesNames.contains(activityName);
    }
}
