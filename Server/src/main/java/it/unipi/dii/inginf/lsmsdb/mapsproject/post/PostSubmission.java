package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public class PostSubmission {


    private String title;
    private String placeId;
    private String authorId;
    private String authorUsername;
    private String description;
    private Date experienceDate;
    private String activity;
    private String YTvideoId;
    private List<String> tags;
    private MultipartFile thumbnail;
    private List<MultipartFile> pics;

    public Document createDocument(){

        Document postDoc = new Document(Post.KEY_AUTHOR_USERNAME, authorUsername)
                .append(Post.KEY_AUTHOR_ID, authorId)
                .append(Post.KEY_PLACE_ID, placeId)
                .append(Post.KEY_TITLE,title)
                .append(Post.KEY_DESCRIPTION, description)
                .append(Post.KEY_DATE, experienceDate)
                .append(Post.KEY_ACTIVITY, activity)
                .append(Post.KEY_TAGS, tags);

        return postDoc;
    }

    public String getTitle(){ return title; }

    public String getDescription(){ return description; }

    public Date getExperienceDate(){ return experienceDate; }

    public String getActivity(){ return activity; }

    public List<String> getTags(){ return tags; }

    public String getPlaceId(){ return placeId; }

    public String getYTvideoId(){ return YTvideoId; }

    public void setAuthorId(String id){ this.authorId = id; }

    public void setAuthorUsername(String uname){ this.authorUsername = uname; }
}
