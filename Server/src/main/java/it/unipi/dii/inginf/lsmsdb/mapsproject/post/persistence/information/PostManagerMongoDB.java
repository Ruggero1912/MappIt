package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostSubmission;
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
    public Post storePost(Post newPost){
        if(newPost == null)
            return null;
        Post res = storeInPostCollection(newPost);
        if(res != null)
            storeEmbeddedPosts(res);
        return res;
    }

    private Post storeInPostCollection(Post newPost) {
        Document postDoc = newPost.createDocument();
        LOGGER.info("Going to store the following post document: " + newPost.toString());
        try{
            postCollection.insertOne(postDoc);
            String id = postDoc.getObjectId(Post.KEY_ID).toString();
            postDoc.append(Post.KEY_ID, id);
            return new Post(postDoc);
        } catch(MongoException me){
            return null;
        }
    }

    private PostPreview storeEmbeddedPosts(Post newPost) {

        ObjectId placeObjId;
        ObjectId authorObjId;
        try{
            placeObjId = new ObjectId(newPost.getPlaceId());
            authorObjId = new ObjectId(newPost.getAuthorId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        PostPreview newPostPreview = new PostPreview(newPost);
        Document postPreviewDoc = newPostPreview.createDocument();

        try{
            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);
            Bson userUpdate = Updates.push(User.KEY_PUBLISHED_POSTS, postPreviewDoc);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateOne(userFilter, userUpdate);

            Bson placeFilter = Filters.eq(Place.KEY_ID, placeObjId);
            Bson placeUpdate = Updates.push(Place.KEY_POSTS_ARRAY, postPreviewDoc);
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateOne(placeFilter, placeUpdate);

            return newPostPreview;
        } catch(MongoException me){
            LOGGER.severe("Mongo store embedded documents error");
            me.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deletePost(Post postToDelete){
        if(postToDelete == null)
            return  false;
        boolean deletedEmbeddedDocs = false;
        boolean deletedFromPostCollection = deletePostInPostCollection(postToDelete);
        if(deletedFromPostCollection)
            deletedEmbeddedDocs = deleteEmbeddedPosts(postToDelete);
        return deletedEmbeddedDocs && deletedFromPostCollection;
    }

    private boolean deletePostInPostCollection(Post postToDelete){
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

    public boolean deleteEmbeddedPosts(Post postToDelete) {
        ObjectId postObjId;
        ObjectId placeObjId;
        ObjectId authorObjId;
        try{
            postObjId = new ObjectId(postToDelete.getId());
            placeObjId = new ObjectId(postToDelete.getPlaceId());
            authorObjId = new ObjectId(postToDelete.getAuthorId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        PostPreview postPreviewToDelete = new PostPreview(postToDelete);
        Document postPreviewToDeleteDoc = postPreviewToDelete.createDocument();

        try{
            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);
            Bson userUpdate = Updates.pull(User.KEY_PUBLISHED_POSTS, postPreviewToDeleteDoc);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateOne(userFilter, userUpdate);

            Bson placeFilter = Filters.eq(Place.KEY_ID, placeObjId);
            Bson placeUpdate = Updates.pull(Place.KEY_POSTS_ARRAY, postPreviewToDeleteDoc);
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateOne(placeFilter, placeUpdate);
            return true;
        } catch(MongoException me){
            return false;
        }
    }

    @Override
    public boolean deletePostsOfGivenUser(User user) {
        if (user == null)
            return false;
        boolean deletedFromPostCollection = false;
        boolean deletedEmbeddedDocs = deleteEmbeddedPostsOfGivenUser(user);
        if(deletedEmbeddedDocs)
            deletedFromPostCollection = deletePostsOfGivenUserInPostCollection(user);
        return (deletedEmbeddedDocs && deletedFromPostCollection);
    }
    private boolean deletePostsOfGivenUserInPostCollection(User user) {
        if(user==null)
            return false;

        ObjectId objId;

        try{
            objId = new ObjectId(user.getId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }

        Bson idFilter = Filters.eq(Post.KEY_AUTHOR_ID, objId);
        DeleteResult ret = postCollection.deleteMany(idFilter);
        return ret.wasAcknowledged();
    }

    private boolean deleteEmbeddedPostsOfGivenUser(User user) {
        ObjectId authorObjId;
        try{
            authorObjId = new ObjectId(user.getId());
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }
        try{
            Bson userFilter = Filters.eq(User.KEY_ID, authorObjId);

            //Bson userUpdate = Updates.pull(User.KEY_PUBLISHED_POSTS, new Document(Post.KEY_AUTHOR_ID, authorObjId));
            // equivalent to dropping the attribute...
            Bson userUpdate = Updates.unset(User.KEY_PUBLISHED_POSTS);
            MongoCollection userCollection = MongoConnection.getCollection(MongoConnection.Collections.USERS.toString());
            userCollection.updateMany(userFilter, userUpdate);

            //TODO: not so optimized query:
            //Bson placeFilter = Filters.eq(Place.KEY_POSTS_ARRAY + "." +);
            Bson placeUpdate = Updates.pull(Place.KEY_POSTS_ARRAY, new Document(Post.KEY_AUTHOR_ID, authorObjId));
            MongoCollection placeCollection = MongoConnection.getCollection(MongoConnection.Collections.PLACES.toString());
            placeCollection.updateMany(null, placeUpdate);
            return true;
        } catch(MongoException me){
            return false;
        }
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
            cursor.close();
            return null;
        }
        else{
            Document postDoc = cursor.next();
            Post ret = new Post(postDoc);
            cursor.close();
            return ret;
        }
    }


    @Override
    public boolean updateLikesCounter(Post post, int k) {
        if(post == null || k < -1 || k > 1  || k == 0){
            return false;
        }
        String postId = post.getId();

        Bson idFilter = Filters.eq(Post.KEY_ID, new ObjectId(postId));
        UpdateResult res = postCollection.updateOne(idFilter, Updates.inc("likes", k));
        return res.wasAcknowledged();
    }
}
