package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FlickrPost extends Post{

    private List<String> pics;

    public FlickrPost(String id, String author, String authorID, String title, Date d, String des, List<String> imgs, List<String> tags) {
        super(id, author, authorID, title, d, des, tags);
        if(imgs!=null)
            this.pics = imgs;
        else
            this.pics = new ArrayList<String>();
    }

    public FlickrPost(Document doc){
        super(doc);
        this.pics = Arrays.asList(doc.get("pics").toString()); //to check
    }

    public Document createDocument(){
        Document postDoc = new Document("title", this.title)
                .append("author", this.author) //need to change the Post collection key names
                .append("authorID", this.authorID) //need to change the Post collection key names
                .append("desc", this.description)
                .append("tags", this.tags)
                .append("activity", this.activity.toString())
                .append("pics", this.pics)
                .append("postDate", this.postDate)
                .append("date", this.date);

        return postDoc;
    }

    public List<String> getImages() {
        return pics;
    }

    public void setImages(List<String> imgs) {
        this.pics = imgs;
    }

    public void addImages(String imgLink) {
        this.pics.add(imgLink);
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
