package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;

public interface PostSocialManager {

    /**
     * return true if the insert of the newly created Post was successful or false if something goes wrong
     * @param newPost the Post object containing all the info
     */
    boolean storePost(Post newPost);
}
