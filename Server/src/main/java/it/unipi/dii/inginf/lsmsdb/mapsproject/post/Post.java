package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFile;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.neo4j.driver.Value;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Post {

    public static final String KEY_POST_COLLECTION = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "collectionName");
    public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "id");
    public static final String KEY_AUTHOR_USERNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "authorUsername");
    public static final String KEY_AUTHOR_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "authorId");
    public static final String KEY_PLACE_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "placeId");
    public static final String KEY_PLACE_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "placeName");
    public static final String KEY_TITLE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "title");
    public static final String KEY_DATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "date");
    public static final String KEY_POST_DATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "postDate");
    public static final String KEY_DESCRIPTION = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "description");
    public static final String KEY_ACTIVITY = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "activity");
    public static final String KEY_TAGS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "tags");
    public static final String KEY_THUMBNAIL = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "thumbnail");
    public static final String KEY_YT_VIDEO_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "videoId");
    public static final String KEY_PICS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "pics");
    public static final String KEY_LIKES = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "likes");
    public static final String KEY_COUNTRY_CODE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.postCollection, "countryCode");

    public static final String NEO_POST_LABEL = PropertyPicker.getNodeLabel(PropertyPicker.postEntity);
    public static final String NEO_KEY_ID = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "id");
    public static final String NEO_KEY_TITLE = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "title");
    public static final String NEO_KEY_DESC = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "desc");
    public static final String NEO_KEY_THUMBNAIL = PropertyPicker.getNodePropertyKey(PropertyPicker.postEntity, "thumb");

    public static final String NEO_RELATION_AUTHOR = PropertyPicker.getGraphRelationKey("author");
    public static final String NEO_RELATION_LOCATION = PropertyPicker.getGraphRelationKey("location");

    private String _id;
    private String authorUsername;
    private String authorId;
    private String placeId;
    private String placeName;
    private String title;
    private Date date;
    private Date postDate;
    private String description;
    private String activity;
    private List<String> tags;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String videoId;
    private String thumbnail;
    private List<String> pics;
    private int likeCounter;
    private String countryCode;

    public Post(String id, String author, String authorID, String place_id, String t, Date d, String des, String activities, List<String> tags, String vidId, String thumb, List<String> pics) {
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
        this.videoId = vidId;
        this.thumbnail = thumb;
        this.pics = pics;
    }

    public Post(PostSubmission submittedPost, User author, Place placeOfThePost, String thumbnail, List<String> pics){
        this.authorUsername = author.getUsername();
        this.authorId = author.getId();
        this.authorUsername = author.getUsername();
        this.placeId = submittedPost.getPlaceId();
        this.placeName = placeOfThePost.getName();
        this.countryCode = placeOfThePost.getCountryCode();
        this.title= submittedPost.getTitle();
        this.date = submittedPost.getExperienceDate();
        this.postDate = new Date();
        this.description = submittedPost.getDescription();
        this.activity = submittedPost.getActivity();
        this.tags = submittedPost.getTags();
        this.videoId = submittedPost.getYTvideoId();
        this.thumbnail = thumbnail;
        this.pics = pics;
    }

    public Post(Document doc){
        this._id = doc.get(KEY_ID).toString();
        this.authorUsername = doc.get(KEY_AUTHOR_USERNAME).toString();
        this.authorId = doc.get(KEY_AUTHOR_ID).toString();
        this.placeId = doc.get(KEY_PLACE_ID).toString();
        this.placeName = doc.getString(KEY_PLACE_NAME);
        this.title = doc.get(KEY_TITLE).toString();
        this.date = (Date) doc.get(KEY_DATE);
        this.postDate = (Date) doc.get(KEY_POST_DATE);
        this.description = doc.get(KEY_DESCRIPTION).toString();
        this.activity = doc.get(KEY_ACTIVITY).toString();
        this.tags = (List<String>) doc.get(KEY_TAGS, List.class);
        this.videoId = doc.getString(KEY_YT_VIDEO_ID);
        this.thumbnail = doc.get(KEY_THUMBNAIL).toString();
        this.pics = (List<String>) doc.get(KEY_PICS, List.class);
        this.likeCounter = doc.getInteger(KEY_LIKES, 0);
        this.countryCode = doc.getString(KEY_COUNTRY_CODE);
    }

    public Document createDocument(){
        Document postDoc = new Document(KEY_TITLE, this.title)
                .append(KEY_AUTHOR_USERNAME, this.authorUsername)
                .append(KEY_DESCRIPTION, this.description)
                .append(KEY_PLACE_ID, this.placeId)
                .append(KEY_PLACE_NAME, this.placeName)
                .append(KEY_TITLE, this.title)
                .append(KEY_TAGS, this.tags)
                .append(KEY_ACTIVITY, this.activity)
                .append(KEY_POST_DATE, this.postDate)
                .append(KEY_DATE, this.date)
                .append(KEY_THUMBNAIL, this.thumbnail)
                .append(KEY_COUNTRY_CODE, this.countryCode);

        if (this.authorId != null){
            postDoc.append(KEY_AUTHOR_ID, this.authorId);
        }
        if(this.videoId != null) {
            postDoc.append(KEY_YT_VIDEO_ID, this.videoId);
        }
        if(this.pics != null)
         postDoc.append(KEY_PICS, this.pics);

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

    public String getAuthorId(){ return this.authorId; }

    public void setAuthorId(String id){ this.authorId=id; }

    public String getPlaceId(){ return this.placeId; }

    public void setPlaceId(String id){ this.placeId=id; }

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

    public String getVideoId() {
        return this.videoId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getYouTubeLink() { if(this.videoId != null) return "https://www.youtube.com/watch?v=" + this.videoId; else return null;}

    public void setVideoId(String id) {
        this.videoId = id;
    }

    /**
     * if the thumbanil is stored on our platform, converts the image id to image link (an absolute path)
     * @return the Link to the thumbnail of the given post
     */
    public String getThumbnail() {
        if(this.thumbnail == null)
            return null;
        if(ImageFile.isServerImageId(this.thumbnail))
            return ImageFile.getResourceURIFromId(this.thumbnail);
        return this.thumbnail;
    }

    /**
     * if one or more of the images are stored on our platform, converts the image id to image link (an absolute path)
     * @return a list of Links to the images of the given post
     */
    public List<String> getPics() {
        List<String> picsLinks = new ArrayList<>();
        for( String pic : this.pics){
            if(pic == null)
                return null;
            if(ImageFile.isServerImageId(pic))
                picsLinks.add( ImageFile.getResourceURIFromId(this.thumbnail) );
            picsLinks.add(pic);
        }
        return picsLinks;
    }

    public void setThumbnail(String t) {
        this.thumbnail = t;
    }

    public String getCountryCode(){
        return countryCode;
    }

    public Date getPostDate() {
        return postDate;
    }

    public int getLikeCounter() {
        return likeCounter;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String toString() {

        String ret =
                "Post{" +
                        "id=" + _id +
                        ", title='" + title + '\'' +
                        ", author='" + authorUsername + '\'' +
                        ", date='" + date.toString() + '\'' +
                        ", description='" + description + '\'' +
                        '}';

        return ret;
    }
}
