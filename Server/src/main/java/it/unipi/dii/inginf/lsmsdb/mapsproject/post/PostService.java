package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmsdb.mapsproject.activity.ActivityService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFile;
import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFileService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
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
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostService {

    private static final Logger LOGGER = Logger.getLogger(PostService.class.getName());
    public static final int DEFAULT_MAXIMUM_QUANTITY = 5;
    public static final int LIMIT_MAXIMUM_QUANTITY = 100;
    public static final String noActivityFilterKey = "any";

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
     * @param submittedPost the YouTube Post object containing all the info
     * @return YtPost object if the insert is successful or null otherwise
     */
    public static Post createNewPost(PostSubmission submittedPost, User author, Place placeOfThePost, MultipartFile thumb, List<MultipartFile> pics) throws DatabaseErrorException {
        if(submittedPost == null){
            return null;
        }

        ImageFileService imageFileService = new ImageFileService();

        String thumbId = "";
        List<String> picsLinks = new ArrayList<>();

        try {
            if (thumb != null)
                thumbId = imageFileService.uploadImage(thumb);
            if( ! pics.isEmpty()){
                for(MultipartFile pic : pics){
                    String picId = (imageFileService.uploadImage(pic));
                    picsLinks.add(picId);
                }
            }
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during file storing!");
            e.printStackTrace();
            return null;
        }

        PostManager pm = PostManagerFactory.getPostManager();

        Post newPost = new Post(submittedPost, author, placeOfThePost, thumbId, picsLinks);
        newPost = pm.storePost(newPost);

        if(newPost == null){
            LOGGER.log(Level.SEVERE, "Error: MongoDB new post insertion failed!");
            throw new DatabaseErrorException("MongoDB new post insert failed");
        }

        PostSocialManager psm = PostSocialManagerFactory.getPostManager();
        boolean insertedNewPostNeo;
        try{
            insertedNewPostNeo = psm.storePost(newPost);
            if(!insertedNewPostNeo){
                //we have to delete the newly inserted post from MongoDB
                LOGGER.log(Level.SEVERE, "Error during new post insert: Neo4j failed... Roll back completed");
                pm.deletePost(newPost);
                throw new DatabaseErrorException("Neo4J new post insertion failed");
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error during insert: Neo4j new post insert failed!");
            return null;
        }
        return newPost;
    }

    /**
     * return True if post is successfully deleted, False otherwise
     * @param post the post to be deleted
     * @return True if post is successfully deleted, else False
     */
    public static boolean deletePost(Post post){
        PostManager pm = PostManagerFactory.getPostManager();
        boolean deletedFromDocDb = pm.deletePost(post);
        if(deletedFromDocDb){
            PostSocialManager um = PostSocialManagerFactory.getPostManager();
            boolean deletedFromGraph = um.deletePost(post);
            if(deletedFromGraph)
                return true;
            else{
                //in this case we have to restore the post to mongodb
                LOGGER.warning("CANNOT delete from GraphDB the post" + post.getId() + "! Rolling back delete operation from Mongo");
                pm.storePost(post);
                return false;
            }
        }else{
            LOGGER.warning("Cannot delete the post " + post.getId() + " from documentDB! No changes.");
            return false;
        }

    }

    /**
     * return True if posts are successfully deleted, False otherwise
     * @param user is the author of the posts to delete
     * @return True if posts are successfully deleted, else False
     */
    public static boolean deletePostsOfGivenUser(User user){
        if(user == null){
            LOGGER.warning("An empty user object was given for the method 'deletePostsOfGivenUser'");
            return false;
        }
        PostManager pm = PostManagerFactory.getPostManager();
        boolean deletedPostsFromMongo = pm.deletePostsOfGivenUser(user);
        if(deletedPostsFromMongo) {
            PostSocialManager um = PostSocialManagerFactory.getPostManager();
            boolean deleteFromNeo = um.deleteAllPostsOfGivenUser(user);
            if(deleteFromNeo){
                return true;
            }else{
                LOGGER.severe("Cannot delete the posts of the user " + user.getId() + "from Neo4j!");
                return false;
            }
        }else{
            LOGGER.warning("'deletePostsOfGivenUser': cannot delete the posts of the user " + user.getId() + " from Mongo");
            return false;
        }
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


    /**
     * returns a list of the most popular posts ordered by popularity in therms of likes received, filtering by period and activity
     * @param fromDate the starting date of the period
     * @param toDate the ending date of the period
     * @param activityFilter can be "any" or the name of an activity that should be in the activity field of the returned Post
     * @param maxQuantity
     * @return a List of Posts ordered by popularity
     */
    public static List<Post> getPopularPosts(Date fromDate, Date toDate, String activityFilter, int maxQuantity){
        if(!activityFilter.equals(noActivityFilterKey)){
            if( ! ActivityService.checkIfActivityExists(activityFilter)){
                activityFilter = noActivityFilterKey;
            }
        }

        if(maxQuantity <= 0){
            maxQuantity = DEFAULT_MAXIMUM_QUANTITY;
        }else if(maxQuantity > LIMIT_MAXIMUM_QUANTITY){
            maxQuantity = LIMIT_MAXIMUM_QUANTITY;
        }
        PostManager pm = PostManagerFactory.getPostManager();
        return pm.getPopularPosts(fromDate, toDate, activityFilter, maxQuantity);
    }

    public static List<Document> getPostsPerYearAndActivity(int maxQuantity) {

        if(maxQuantity <= 0){
            maxQuantity = DEFAULT_MAXIMUM_QUANTITY;
        }else if(maxQuantity > LIMIT_MAXIMUM_QUANTITY){
            maxQuantity = LIMIT_MAXIMUM_QUANTITY;
        }

        PostManager pm = PostManagerFactory.getPostManager();
        return pm.getPostsPerYearAndActivity(maxQuantity);
    }
}
