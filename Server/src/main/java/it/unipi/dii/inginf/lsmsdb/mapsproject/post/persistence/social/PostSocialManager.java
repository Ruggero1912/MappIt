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
}
