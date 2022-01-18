package it.unipi.dii.inginf.lsmsdb.mapsproject.activity.persistence.information;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.Activity;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class ActivityManagerMongoDB implements ActivityManager{

    private MongoCollection activityCollection;

    public ActivityManagerMongoDB(){
        activityCollection = MongoConnection.getCollection(MongoConnection.Collections.ACTIVITIES.toString());
    }

    public List<String> retrieveActivitiesName(){
        List<String> activities = new ArrayList<>();
        Bson projectionFields = Projections.fields(
                Projections.include(Activity.KEY_NAME),
                Projections.excludeId());

        MongoCursor<Document> cursor = activityCollection.find().projection(projectionFields).iterator();
        try {
            while(cursor.hasNext()) {
                activities.add((String) cursor.next().get("name"));
            }
        } finally {
            cursor.close();
        }

        return activities;
    }
}