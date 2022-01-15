package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        if(userId == "current"){
            //retrieve the current user
            UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            u = userSpring.getApplicationUser();
        }else{
            u = UserService.getUserFromId(userId);
        }
        try{
            List<PostPreview> postPreviews = UserService.retrieveAllPostPreviewsFromUser(u);
            if(postPreviews==null)
                LOGGER.log(Level.WARNING, "Empty list");
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
    @PostMapping(value = "/post")
    public ResponseEntity<?> newPost(@RequestBody Post newPost) {

        //TODO: handle the files upload and specify as author the current user
        Post insertedPost;
        try{
            insertedPost = PostService.createNewPost(newPost);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "{Error : Unable to store new post}");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Something went wrong in inserting new post:" + e.getMessage());
        }

        return ResponseEntity.ok(insertedPost);
    }
}
