package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.PlaceService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostSubmission;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private static final Logger LOGGER = Logger.getLogger( PostController.class.getName() );


    @ApiOperation(value = "Get information of a specific post", notes = "This method retrieve information of a specific post, given its _id")
    @GetMapping(value = "/post/{id}", produces = "application/json")
    public ResponseEntity<?> getPostById(@PathVariable(value = "id") String postId) {
        ResponseEntity<?> result;
        try{
            Post post = PostService.getPostFromId(postId);
            result = ResponseEntity.ok(post);
            if(post==null) {
                LOGGER.log(Level.WARNING, "Error: could not find post (id=" + postId + ")");
                result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find post\"}");
            }
        }catch (Exception e){
            LOGGER.log(Level.WARNING, "Error: could not parse post, an exception has occurred: " + e);
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not parse post, and exception has occurred\"}");
        }
        return result;
    }

    @ApiOperation(value = "returns the list of posts of the specified user or for the current if no userId is specified")
    @GetMapping(value = "/post/list", produces = "application/json")
    public ResponseEntity<?> allPostsPreviewFromUser(@RequestParam( defaultValue = "current") String userId) {
        ResponseEntity<?> result;
        User u;

        if(userId.equals("current")){
            //retrieve the current user
            UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            u = userSpring.getApplicationUser();
        }else{
            u = UserService.getUserFromId(userId);
        }
        try{
            List<PostPreview> postPreviews = UserService.retrieveAllPostPreviewsFromUser(u);
            if(postPreviews==null)
                LOGGER.log(Level.WARNING, String.format("Empty posts list for the user %s", u.getId()));
            result = ResponseEntity.status(HttpStatus.OK).body(postPreviews);
        }catch (NullPointerException e){
            LOGGER.log(Level.WARNING, "Error: the given ID does not correspond to a userId");
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"the given ID does not correspond to a userId\"}");
        }catch (Exception e){
            LOGGER.log(Level.WARNING, "Error: could not parse post list, an exception has occurred: " + e);
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not parse post list\" " +
                                                                        "\"Reason\" : \""+e.getMessage()+"\"}");
        }

        return result;
    }

    @ApiOperation(value = "store a new post in the databases")
    @RequestMapping(path = "/post", method = POST)  //, consumes = {MediaType.MULTIPART_MIXED_VALUE}
    public ResponseEntity<?> newPost(@RequestBody() PostSubmission newPost //,
                                     //@RequestParam(name = "thumbnail", required = false) MultipartFile thumbnail,
                                     //@RequestParam(name = "pics", required = false) MultipartFile[] pics
    ) {

        Post insertedPost = null;
        MultipartFile[] pics = {};
        MultipartFile thumbnail = null;
        //retrieve the current logged-in user for storing in the doc also username and user _id
        UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userSpring.getApplicationUser();

        Place placeOfThePost = PlaceService.getPlaceFromId(newPost.getPlaceId());
        if(placeOfThePost == null){
            LOGGER.info("The user specified placeId " + newPost.getPlaceId() + " was not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: The specified place was not found (id=" + newPost.getPlaceId() + ")");
        }

        //List<MultipartFile> pics = null;
        LOGGER.info("Received store request for a new post");
        try{
            insertedPost = PostService.createNewPost(newPost, currentUser, placeOfThePost, thumbnail, Arrays.asList(pics));
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "{Error : Unable to store new post}");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Something went wrong in inserting new post:" + e.getMessage());
        }

        return ResponseEntity.ok(insertedPost);
    }
    @DeleteMapping(value={"/post/{id}"})
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") String id){
        ResponseEntity<?> result;
        try{
            Post postToDelete = PostService.getPostFromId(id);
            if(postToDelete != null) {
                PostService.deletePost(postToDelete);
                result = ResponseEntity.ok("Post successfully deleted (id="+id+ ")");
                LOGGER.log(Level.INFO, "Post successfully deleted: (id="+id+ ")");
            } else {
                result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not find Post (id=" + id + ")");
            }
        }
        catch (Exception e){
            result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not delete Post (id=" + id + ")");
            LOGGER.log(Level.WARNING, "Error: could not delete Post, an exception has occurred: " + e);
        }
        return result;
    }
}
