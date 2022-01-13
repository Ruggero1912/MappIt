package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.persistence.information.PlaceManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social.PostSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social.PostSocialManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social.PostSocialManagerNeo4j;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social.UserSocialManagerFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostService {

    private static final Logger LOGGER = Logger.getLogger(PostService.class.getName());

    /**
     * return null if it does not exist a post with that id, otherwise returns the associated post
     * @param id the id of the requested post
     * @return associated Post object or null if not found
     */
    public static Post getPostFromId(String id){
        PostManager um = PostManagerFactory.getPostManager();
        return um.getPostFromId(id);
    }

    /**
     * return the newly created Yt Post or Null if something goes wrong
     * @param newPost the YouTube Post object containing all the info
     * @return YtPost object if the insert is successful or null otherwise
     */
    public static Post createNewPost(Post newPost) throws DatabaseErrorException {
        if(newPost == null){
            return null;
        }

        PostManager pm = PostManagerFactory.getPostManager();
        Post addedNewPostMongo = pm.storePost(newPost);
        if(addedNewPostMongo == null){
            LOGGER.log(Level.SEVERE, "Error during insert: Neo4j new favourite place failed!");
            throw new DatabaseErrorException("MongoDB new post insert failed");
        }

        PostSocialManager psm = PostSocialManagerFactory.getPostManager();
        boolean insertedNewPostNeo;
        try{
            insertedNewPostNeo = psm.storePost(newPost);
            if(!insertedNewPostNeo){
                //we have to delete the newly inserted post from MongoDB
                LOGGER.log(Level.SEVERE, "Error during new post insert: Neo4j failed!");
                pm.deletePost(newPost);
                return null;
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during insert: Neo4j new post insert failed!");
            return null;
        }

        return newPost;
    }

    /**
     * return True if posts are successfully deleted, False otherwise
     * @param user is the author of the posts to delete
     * @return True if posts are successfully deleted, else False
     */
    public static boolean deletePostsOfGivenUser(User user){
        PostSocialManager um = PostSocialManagerFactory.getPostManager();
        return um.deleteAllPostsOfGivenUser(user);
    }

    /**
     * Adds like from a user u to a post p
     * @param user user that likes the post
     * @param post post that received the appreciation
     * @return true if the like was delivered successfully, else false
     */
    //TODO: add endpoint method
    public static boolean likePost(User user, Post post){
        if(user == null || post == null){
            return false;
        }

        PostManager pm = PostManagerFactory.getPostManager();
        boolean updatedLikesCounterInMongo = pm.updateLikesCounter(post, 1);
        if(!updatedLikesCounterInMongo){
            LOGGER.log(Level.SEVERE, "Error during like insert: MongoDB update failed!");
            return false;
        }

        PostSocialManager psm = PostSocialManagerFactory.getPostManager();
        boolean addedLikesRelationshipNeo = psm.likePost(user, post);
        if(!addedLikesRelationshipNeo){
            //restore the likes counter value
            LOGGER.log(Level.SEVERE, "Error during like insert: Neo4j relationship insertion failed!");
            pm.updateLikesCounter(post, -1);
            return false;
        }

        return true;
    }

    /**
     * Removes like from a user u to a post p
     * @param user user that unlikes the post
     * @param post post for which we delete the appreciation
     * @return true if the like was deleted successfully, else false
     */
    //TODO: add endpoint method
    public static boolean unlikePost(User user, Post post){
        if(user == null || post == null){
            return false;
        }

        PostManager pm = PostManagerFactory.getPostManager();
        boolean updatedLikesCounterInMongo = pm.updateLikesCounter(post, -1);
        if(!updatedLikesCounterInMongo){
            LOGGER.log(Level.SEVERE, "Error during like deletion: MongoDB update failed!");
            return false;
        }

        PostSocialManager psm = PostSocialManagerFactory.getPostManager();
        boolean removedLikesRelationshipNeo = psm.unlikePost(user, post);
        if(!removedLikesRelationshipNeo){
            //restore the likes counter value
            LOGGER.log(Level.SEVERE, "Error during unlike: Neo4j relationship deletion failed!");
            pm.updateLikesCounter(post, 1);
            return false;
        }

        return true;
    }
}
