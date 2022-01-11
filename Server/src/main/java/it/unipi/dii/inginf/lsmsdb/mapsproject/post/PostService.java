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
     * return all the posts of an user in the db, given its username
     * @return Posts List or null if there are no posts
     */
    public static List<Post> retrieveAllPostsFromUsername(String username){
        PostManager pm = PostManagerFactory.getPostManager();
        return pm.retrieveAllPostsFromUsername(username);
    }

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



}
