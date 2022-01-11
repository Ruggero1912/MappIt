package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostManagerMongoDB implements PostManager {

    private static final Logger LOGGER = Logger.getLogger(PostManagerMongoDB.class.getName());

    private MongoCollection postCollection;

    private static final String YT_NEW_POST = "youtube";
    private static final String FLICK_NEW_POST = "flickr";

    public PostManagerMongoDB(){
        this.postCollection = MongoConnection.getCollection(MongoConnection.Collections.POSTS.toString());
    }

    @Override
    public Post storePost(Post newPost) {
        Document postDoc = newPost.createDocument();

        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId("_id").toString();
            postDoc.append("_id", id);
            return new Post(postDoc);
        } catch(MongoException me){
            return null;
        }
    }

    @Override
    public boolean deletePost(Post postToDelete) {
        if(postToDelete==null)
            return false;

        ObjectId objId;
        String postId = postToDelete.getId();

        try{
            objId = new ObjectId(postId);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        Bson idFilter = Filters.eq(Post.KEY_ID, objId);
        DeleteResult ret = postCollection.deleteOne(idFilter);
        return ret.wasAcknowledged();
    }


    @Override
    public List<Post> retrieveAllPostsFromUsername(String username) {
        //...
        return null;
    }
}
