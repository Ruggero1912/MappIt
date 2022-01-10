package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;

public interface PostSocialManager {

    /**
     * return the newly created Post or Null if something goes wrong
     * @param newPost the Post object containing all the info
     */
    Post storePost(Post newPost);
}
