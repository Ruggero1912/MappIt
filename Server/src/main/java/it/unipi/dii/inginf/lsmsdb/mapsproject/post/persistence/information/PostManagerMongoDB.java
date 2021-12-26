package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.FlickrPost;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.YtPost;
import org.bson.Document;

import java.util.List;

public class PostManagerMongoDB implements PostManager {

    private MongoCollection postCollection;

    private static final String YT_NEW_POST = "youtube";
    private static final String FLICK_NEW_POST = "flickr";

    public PostManagerMongoDB(){
        this.postCollection = MongoConnection.getCollection(MongoConnection.Collections.POSTS.toString());
    }

    @Override
    public YtPost storeYtPost(YtPost newPost) {
        Document postDoc = newPost.createDocument();

        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId("_id").toString();
            postDoc.append("_id", id);
            return new YtPost(postDoc);
        } catch(MongoException me){
            return null;
        }
    }

    @Override
    public FlickrPost storeFlickrPost(FlickrPost newPost) {
        Document postDoc = newPost.createDocument();

        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId("_id").toString();
            postDoc.append("_id", id);
            return new FlickrPost(postDoc);
        } catch(MongoException me){
            return null;
        }
    }

    @Override
    public List<Post> retrieveAllPostsFromUsername(String username) {
        //...
        return null;
    }
}
