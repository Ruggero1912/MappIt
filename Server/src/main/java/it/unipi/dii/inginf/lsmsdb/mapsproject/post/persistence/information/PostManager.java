package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

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
}
