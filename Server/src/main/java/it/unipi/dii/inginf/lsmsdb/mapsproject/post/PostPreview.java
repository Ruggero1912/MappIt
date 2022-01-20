package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFile;
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
     * @param authorId corresponds to the id value gathered from the User node in Neo4j
     * @param authorUsername corresponds to the username value gathered from the User node in Neo4j
     */
    public PostPreview(Value valueFromPostNode, Value authorId, Value authorUsername) {
        this._id =  authorId.asString();
        this.authorUsername = authorUsername.asString();
        this.title = valueFromPostNode.get(Post.NEO_KEY_TITLE).asString();
        this.description = valueFromPostNode.get(Post.NEO_KEY_DESC).asString();
        this.thumbnail = valueFromPostNode.get(Post.NEO_KEY_THUMBNAIL).asString();
    }

    /**
     * Parses a PostPreview object from a (MongoDB) Document
     * @param doc the Document that MUST includes the PostPreview keys
     */
    public PostPreview(Document doc){
        try {
            this._id = doc.get(Post.KEY_ID).toString();
        }catch(Exception e){
            this._id = null;
        }
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

        if(this._id != null){
            postPreviewDoc.append(Post.KEY_ID, this._id);
        }

        return postPreviewDoc;
    }

    public String getId(){
        return _id;
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

    public void setThumbnail(String t) {
        this.thumbnail = t;
    }
}
