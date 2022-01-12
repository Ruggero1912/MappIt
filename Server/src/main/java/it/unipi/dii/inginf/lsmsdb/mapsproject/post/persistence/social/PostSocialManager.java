package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;

public interface PostSocialManager {

    /**
     * return true if the insert of the newly created Post was successful or false if something goes wrong
     * @param newPost the Post object containing all the info
     */
    boolean storePost(Post newPost);

    /**
     * deletes all the posts of the specified user
     * @param u user to delete
     * @return true if the deletion of all the posts is successful, else false
     */
    boolean deleteAllPostsOfGivenUser(User u);

    /**
     * Adds like from a user u to a post p
     * @param u user that likes the post
     * @param p post that received the appreciation
     * @return true if the like was delivered successfully, else false
     */
    boolean likePost(User u, Post p);

    /**
     * Remove like from a user u to a post p
     * @param u user that unlikes the post
     * @param p post for which we remove the appreciation
     * @return true if the like was deleted successfully, else false
     */
    boolean unlikePost(User u, Post p);
}
