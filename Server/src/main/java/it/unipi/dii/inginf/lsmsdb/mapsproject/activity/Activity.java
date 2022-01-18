package it.unipi.dii.inginf.lsmsdb.mapsproject.activity;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.bson.Document;

import java.util.List;

public class Activity {

    public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.activityCollection, "id");
    public static final String KEY_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.activityCollection, "name");
    public static final String KEY_CATEGORY = PropertyPicker.getCollectionPropertyKey(PropertyPicker.activityCollection, "category");
    public static final String KEY_TAGS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.activityCollection, "tags");

    private String _id;
    private String name;
    private String category;
    private List<String> tags;

    public Activity (String id, String n, String cat, List<String> t) {
        this._id = id;
        this.name = n;
        this.category = cat;
        this.tags = t;
    }

    public Activity (Document doc) {
        this._id = doc.get(KEY_ID).toString();
        this.name = doc.get(KEY_NAME).toString();
        this.category = doc.get(KEY_CATEGORY).toString();
        this.tags = (List<String>) doc.get(KEY_TAGS, List.class);
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }
}
