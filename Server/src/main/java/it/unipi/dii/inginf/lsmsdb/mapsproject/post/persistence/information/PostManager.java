package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;

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
}
