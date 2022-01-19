package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;
import org.neo4j.driver.Value;

import java.util.Date;
import java.util.List;

public class PostPreview {

    private String _id;
    private String authorUsername;
    private String title;
    private String description;
    private String thumbnail;

    public PostPreview(String id, String author, String title, String description, String thumbnail){
        this._id = id;
        this.authorUsername = author;
        this.title = title;
        this.thumbnail = thumbnail;
        this.description = description;
    }

    public PostPreview(Post post){
        this._id = post.getId();
        this.authorUsername = post.getAuthorUsername();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.thumbnail = post.getThumbnail();
    }

    /**
     * We exploit this constructor to parse a Post Preview object from a Neo4j Node
     * @param valueFromPostNode corresponds to the value gathered from the Post Node in Neo4j
     * @param valueFromAuthorNode corresponds to the value gathered from the User Node in Neo4j
     */
    public PostPreview(Value valueFromPostNode, Value valueFromAuthorNode) {
        this.authorUsername = valueFromAuthorNode.get(User.NEO_KEY_USERNAME).asString();
        this.title = valueFromPostNode.get(Post.NEO_KEY_TITLE).asString();
        this.description = valueFromPostNode.get(Post.NEO_KEY_DESC).asString();
        this.thumbnail = valueFromPostNode.get(Post.NEO_KEY_THUMBNAIL).asString();
    }

    public PostPreview(Document doc){
        this._id = doc.get(Post.KEY_ID).toString();
        this.authorUsername = doc.get(Post.KEY_AUTHOR_USERNAME).toString();
        this.title = doc.get(Post.KEY_TITLE).toString();
        this.description = doc.get(Post.KEY_DESCRIPTION).toString();
        this.thumbnail = doc.get(Post.KEY_THUMBNAIL).toString();
    }

    public Document createDocument(){
        Document postPreviewDoc = new Document(Post.KEY_TITLE, this.title)
                .append(Post.KEY_AUTHOR_USERNAME, this.authorUsername)
                .append(Post.KEY_DESCRIPTION, this.description)
                .append(Post.KEY_THUMBNAIL, this.thumbnail);

        return postPreviewDoc;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String des) {
        this.description = des;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String t) {
        this.thumbnail = t;
    }
}
