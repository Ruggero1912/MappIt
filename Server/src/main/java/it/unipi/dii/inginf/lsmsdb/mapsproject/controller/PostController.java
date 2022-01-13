package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.Post;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Post getPostById(@PathVariable(value = "id") String postId) {
        return PostService.getPostFromId(postId);
    }

    @ApiOperation(value = "returns the list of posts of the specified user or for the current if no userId is specified")
    @GetMapping(value = "/posts", produces = "application/json")
    public ResponseEntity<?> allPostsFromUser(@RequestParam( defaultValue = "current") String userId) {
        User u;
        if(userId == "current"){
            //should retrieve the current user
            u = new User(); // TODO: call the method that returns the instance of the currently logged in user
        }else{
            u = UserService.getUserFromId(userId);
        }
        List<Post> posts = UserService.retrieveAllPostsFromUser(u);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    @ApiOperation(value = "store a new post in the databases")
    @PostMapping(value = "/api/post")
    public ResponseEntity<?> newPost(@RequestBody Post newPost) {

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
