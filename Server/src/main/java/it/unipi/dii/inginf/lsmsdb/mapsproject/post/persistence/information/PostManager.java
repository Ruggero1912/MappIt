package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.bson.Document;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;


public interface PostManager {


    /**
     * return the newly created Post or Null if something goes wrong
     * @param newPost the Post object containing all the info
     */
    Post storePost(Post newPost);

    /**
     * return true if the deletion was successful or false if something goes wrong
     * @param postToDelete the Post to be deleted
     */
    boolean deletePost(Post postToDelete);

    /**
     * return true if the deletion was successful or false if something goes wrong
     * @param user author of the posts to delete
     */
    boolean deletePostsOfGivenUser(User user);

    /**
     * return all the posts associated to the given username if exists, else return Null
     * @param username A string containing the given username from the user
     * @return List of posts or null if not found
     */
    List<Post> retrieveAllPostsFromUsername(String username);

    /**
     * return null if it does not exist a post with that id, otherwise returns the associated post
     * @param id the id of the requested post
     * @return associated Post object or null if not found
     */
    Post getPostFromId(String id);

    /**
     * return true if likes counter was correctly increased, else return false
     * @param post we want to increase the likes number
     * @param k value of the increment or decrement
     * @return true if likes counter was correctly increased, false otherwise
     */
    boolean updateLikesCounter(Post post, int k);

    /**
     * returns a list of the most popular posts ordered by popularity in therms of likes received, filtering by period and activity
     * @param fromDate the starting date of the period
     * @param toDate the ending date of the period
     * @param activityName can be "any" or the name of an activity that should be in the activity field of the returned Post
     * @param maxQuantity
     * @return a List of Posts ordered by popularity
     */
    List<Post> getPopularPosts(LocalDate fromDate, LocalDate toDate, String activityName, int maxQuantity);

    /**
     * returns a list of aggregated values in the following shape: year-activityName-#ofPosts
     * @param maxQuantity the quantity of the aggregated values to be returned
     * @return a List of Document containing the aggregated values in json format
     */
    List<Document> getPostsPerYearAndActivity(int maxQuantity);
}
