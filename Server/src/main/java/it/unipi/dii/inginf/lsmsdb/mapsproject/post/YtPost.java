package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;

import javax.print.Doc;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class YtPost extends Post {

    private String videoId;
    private String ytThumbnail;

    public YtPost(String id, String author, String authorID, String title, Date d, String des, String v_id, String thumb, List<String> tags) {
        super(id, author, authorID, title, d, des, tags);
        this.videoId = v_id;
        this.ytThumbnail = thumb;
    }

    public YtPost(Document doc){
        super(doc);
        this.videoId = doc.get("YT_videoId").toString();
        this.ytThumbnail = doc.get("thumb").toString();
    }

    public Document createDocument(){
        Document postDoc = new Document("YT_videoId",this.videoId)
                .append("title", this.title)
                .append("author", this.author) //need to change the Post collection key names
                .append("authorID", this.authorID) //need to change the Post collection key names
                .append("desc", this.description)
                .append("thumb", this.ytThumbnail)
                .append("tags", this.tags)
                .append("activity", this.activity.toString())
                .append("postDate", this.postDate)
                .append("date", this.date);

        return postDoc;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String id) {
        this.videoId = id;
    }

    public String getThumbnail() {
        return this.ytThumbnail;
    }

    public void setThumbnail(String t) {
        this.ytThumbnail = t;
    }

    @Override
    public String toString() {

        String ret =
                "User{" +
                "id=" + _id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date.toString() + '\'' +
                ", description='" + description + '\'' +
                '}';

        return ret;
    }
}
