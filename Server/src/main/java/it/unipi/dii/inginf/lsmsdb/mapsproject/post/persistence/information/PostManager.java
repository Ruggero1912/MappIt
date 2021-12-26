package it.unipi.dii.inginf.lsmsdb.mapsproject.post.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.post.FlickrPost;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.YtPost;

import java.util.List;


public interface PostManager {


    /**
     * return the newly created Yt Post or Null if something goes wrong
     * @param newPost the YouTube Post object containing all the info
     */
    YtPost storeYtPost(YtPost newPost);


    /**
     * return the newly created Flickr Post or Null if something goes wrong
     * @param newPost the Flickr Post object containing all the info
     */
    FlickrPost storeFlickrPost(FlickrPost newPost);


    /**
     * return all the posts associated to the given username if exists, else return Null
     * @param username A string containing the given username from the user
     * @return List of posts or null if not found
     */
    List<Post> retrieveAllPostsFromUsername(String username);
}
