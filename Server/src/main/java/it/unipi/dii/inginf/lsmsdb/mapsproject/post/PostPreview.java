package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.neo4j.driver.Value;

public class PostPreview {

    private String authorUsername;
    private String title;
    private String description;
    private String thumbnail;

    public PostPreview(String author, String title, String description, String thumbnail){
        this.authorUsername = author;
        this.title = title;
        this.thumbnail = thumbnail;
        this.description = description;
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
