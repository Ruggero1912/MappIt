package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManager;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information.PostManagerFactory;
import java.util.List;
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
     * return the newly created Yt Post or Null if something goes wrong
     * @param newPost the YouTube Post object containing all the info
     * @return YtPost object if the insert is successful or null otherwise
     */
    public static YtPost createNewYtPost(YtPost newPost) {
        PostManager pm = PostManagerFactory.getPostManager();
        YtPost insertedPost = pm.storeYtPost(newPost);
        return insertedPost;
    }

    /**
     * return the newly created Flickr Post or Null if something goes wrong
     * @param newPost the Flickr Post object containing all the info
     * @return FlickrPost object if the insert is successful or null otherwise
     */
    public static FlickrPost createNewFlickrPost(FlickrPost newPost) {
        PostManager pm = PostManagerFactory.getPostManager();
        FlickrPost insertedPost = pm.storeFlickrPost(newPost);
        return insertedPost;
    }
}
