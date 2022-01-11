package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
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

    public PostManagerMongoDB(){
        this.postCollection = MongoConnection.getCollection(MongoConnection.Collections.POSTS.toString());
    }

    @Override
    public Post storePost(Post newPost) {
        Document postDoc = newPost.createDocument();

        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId(Post.KEY_AUTHOR_ID).toString();
            postDoc.append(Post.KEY_AUTHOR_ID, id);
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

    @Override
    public Post getPostFromId(String id) {
        if(id.equals(""))
            return null;

        ObjectId objId;

        try{
            objId = new ObjectId(id);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        Bson idFilter = Filters.eq(Post.KEY_ID, objId);
        MongoCursor<Document> cursor = postCollection.find(idFilter).cursor();
        if(!cursor.hasNext()){
            return null;
        }
        else{
            Document postDoc = cursor.next();
            Post ret = new Post(postDoc);
            return ret;
        }
    }
}
