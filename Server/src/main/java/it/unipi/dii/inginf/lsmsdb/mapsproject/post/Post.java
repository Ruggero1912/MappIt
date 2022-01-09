package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import com.google.gson.Gson;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import org.bson.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


public class Post {

    public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "id");
    public static final String KEY_AUTHOR_USERNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "authorUsername");
    public static final String KEY_AUTHOR_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "authorId");
    public static final String KEY_PLACE_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "placeId");
    public static final String KEY_TITLE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "title");
    public static final String KEY_DATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "date");
    public static final String KEY_POST_DATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "postDate");
    public static final String KEY_DESCRIPTION = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "description");
    public static final String KEY_ACTIVITY = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "activity");
    public static final String KEY_TAGS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "tags");
    public static final String KEY_THUMBNAIL = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "thumbnail");
    public static final String KEY_YT_CHANNEL_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "channelId");
    public static final String KEY_YT_VIDEO_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "videoId");
    public static final String KEY_PICS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "pics");


    private String _id;
        private String authorUsername;
        private String authorId;
        private String placeId;
        private String title;
        private Date date;
        private Date postDate;
        private String description;
        private String activity;
        private List<String> tags;
        private String ytChannelId;
        private String videoId;
        private String thumbnail;
        private List<String> pics;
        private int likeCounter;

        public Post(String id, String author, String authorID, String place_id, String t, Date d, String des, String activities, List<String> tags, String ytChId, String vidId, String thumb, List<String> pics) {
            this._id = id;
            this.authorUsername = author;
            this.authorId = authorID;
            this.placeId = place_id;
            this.title= t;
            this.date = d;
            this.postDate = new Date();
            this.description = des;
            this.activity = activities;
            this.tags = tags;
            this.ytChannelId = ytChId;
            this.videoId = vidId;
            this.thumbnail = thumb;
            this.pics = pics;
        }

    public Post(Document doc){
        this._id = doc.get(KEY_ID).toString();
        this.authorUsername = doc.get(KEY_AUTHOR_USERNAME).toString();
        this.authorId = doc.get(KEY_AUTHOR_ID).toString();
        this.placeId = doc.get(KEY_PLACE_ID).toString();
        this.title = doc.get(KEY_TITLE).toString();
        this.date = (Date) doc.get(KEY_DATE);
        this.postDate = (Date) doc.get(KEY_POST_DATE);
        this.description = doc.get(KEY_DESCRIPTION).toString();
        this.activity = doc.get(KEY_ACTIVITY).toString();
        this.tags = (List<String>) doc.get(KEY_TAGS, List.class);
        this.ytChannelId = doc.get(KEY_YT_CHANNEL_ID).toString();
        this.videoId = doc.get(KEY_YT_VIDEO_ID).toString();
        this.thumbnail = doc.get(KEY_THUMBNAIL).toString();
        this.pics = (List<String>) doc.get(KEY_PICS, List.class);
    }

    public Document createDocument(){
        Document postDoc = new Document("title", this.title)
                .append("author", this.authorUsername)
                .append("authorID", this.authorId)
                .append("desc", this.description)
                .append("tags", this.tags)
                .append("activity", this.activity)
                .append("postDate", this.postDate)
                .append("date", this.date)
                .append("thumb", this.thumbnail);

                if(this.ytChannelId != null && this.videoId != null) {
                    postDoc.append("YT_videoId", this.ytChannelId);
                    postDoc.append("YT_videoId", this.videoId);
                }
                if(this.pics != null)
                 postDoc.append("pics", this.pics);

        return postDoc;
    }

    public static Post buildPost(@NotNull Document doc){
        Gson g = new Gson();
        Post p = g.fromJson(doc.toJson(), Post.class);
        return p;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String user) {
        this.authorUsername = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date d) {
        this.date = d;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String des) {
        this.description = des;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String act) {
        this.activity = act;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void addTags(String t) {
        this.tags.add(t);
    }

    public String getYtChannelId() {
        return this.ytChannelId;
    }

    public void setYtChannelId(String id) {
        this.ytChannelId = id;
    }

    public String getVideoId() {
        return this.videoId;
    }

    public void setVideoId(String id) {
        this.videoId = id;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String t) {
        this.thumbnail = t;
    }

    public String toString() {

        String ret =
                "User{" +
                        "id=" + _id +
                        ", title='" + title + '\'' +
                        ", author='" + authorUsername + '\'' +
                        ", date='" + date.toString() + '\'' +
                        ", description='" + description + '\'' +
                        '}';

        return ret;
    }
}
