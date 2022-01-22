package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")

public class UserController {

	private static final Logger LOGGER = Logger.getLogger( UserController.class.getName() );

	/**
	 * return all the Users in the database
	 * //@ApiOperation(value = "Get information of every users",
	 * notes = "This method retrieve information about all the users")
	 */
	@GetMapping(value = "/user/all", produces = "application/json")
	public List<User> getUsers() {
		// here we should check if the current User can access to this information
		// we need to access to the object of the current user
		return UserService.getAllUsers();
	}

	/**
	 * //@ApiOperation(value = "Get information of the current user",
	 * notes = "This method retrieve information of the current user")
	 */
	@GetMapping(value = "/user", produces = "application/json")
	public ResponseEntity<?> getCurrentUserInfo() {
		ResponseEntity<?> result;
		UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		//we return the information about the current logged in user
		User currentUser = userSpring.getApplicationUser();
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "Current logged in user not found");
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find current logged in user\"}");
		}

		LOGGER.log(Level.INFO, "answering to the request... Response body: " + currentUser.toString());
		result = ResponseEntity.status(HttpStatus.OK).body(currentUser);

		return result;
	}

	/**
	 * //@ApiOperation(value = "Get information of a specific user",
	 * notes = "This method retrieve information of a specific user, given its _id")
	 */
	@GetMapping(value = "/user/{id}", produces = "application/json")
	public ResponseEntity<?> getUserById(@PathVariable(value = "id") String userId) {
		ResponseEntity<?> result;
		try{
			User user = UserService.getUserFromId(userId);
			result = ResponseEntity.ok(user);
			if(user==null) {
				LOGGER.log(Level.WARNING, "Error: could not find user (id=" + userId + ")");
				result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not find user\"}");
			}
		}catch (Exception e){
			LOGGER.log(Level.WARNING, "Error: could not parse user, an exception has occurred: " + e);
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Could not parse user, and exception has occurred\"}");
		}
		return result;
	}

	/**
	 * lets an admin delete a user, given its _id
	 * //@ApiOperation(value = "Delete users having _id=id",
	 * notes = "This method deletes a specific user")
	 */
	//TODO: only an admin can delete a user, we need to check role level
	@DeleteMapping(value={"/user/{id}"})
	public ResponseEntity<?> deleteUser(@PathVariable(value = "id") String id){
		ResponseEntity<?> result;
		try{
			User userToDelete = UserService.getUserFromId(id);
			if(userToDelete != null) {
				UserService.delete(userToDelete);
				result = ResponseEntity.ok("User successfully deleted (id="+id+ ")");
				LOGGER.log(Level.INFO, "User successfully deleted: (id="+id+ ")");
			} else {
				result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not find the user with id='" + id + "'");
			}
		}
		catch (Exception e){
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not delete user (id=" + id + ")");
			LOGGER.log(Level.WARNING, "Error: could not delete User, an exception has occurred: " + e);
		}

		return result;
	}

	@PutMapping(value = "/user/password")
	public ResponseEntity<?> changePassword(@RequestParam(name="newPassword") String newPassword) {

		if(newPassword.equals("") || newPassword.length() < 4){ //password constrains need to be established
			LOGGER.log(Level.INFO, "{\"Error\" : \"Password must be at least 4 characters long\"}");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Password must be at least 4 characters long\"}");
		}

		UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User currentUser = userSpring.getApplicationUser();
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "Could not find current logged in user");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not find current logged in user");
		}

		ResponseEntity<?> result;
		if(UserService.updatePassword(currentUser.getId(), newPassword)) {
			result = ResponseEntity.ok("Password successfully updated");
		}
		else{
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not update password");
		}
		return result;
	}

	/**
	 * //@ApiOperation(value = "Make a user follow another user",
	 * notes = "This method makes and user follow another user")
	 */
	@PostMapping(value = "/user/follow/{id}", produces = "application/json")
	public ResponseEntity<?> followUser(@PathVariable (value = "id") String userToFollowId, @RequestBody(required = false) LocalDateTime localDateTime) {
		ResponseEntity<?> result;



		// the time of the visit will be considered now if the localDateTime parameter is null
		if(localDateTime == null){
			localDateTime = LocalDateTime.now();
		}

		//we retrieve user objects of current logged-in user and user to follow
		UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User currentUser = userSpring.getApplicationUser();
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "Could not find current logged in user");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not find current logged in user");
		}

		User userToFollow = UserService.getUserFromId(userToFollowId);
		if(userToFollow == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + userToFollowId);
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"could not find user with id="+userToFollowId+"\"}");
		}

		boolean followerAdded = UserService.followUser( currentUser, userToFollow, localDateTime);

		if (followerAdded){
			LOGGER.log(Level.INFO, "Success: follower added successfully");
			result = ResponseEntity.ok("{\"Success\" : \"follower successfully added\"}");
		} else {
			LOGGER.log(Level.INFO, "Error: could not add the follower");
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"could not add the follower\"}");
		}

		return result;
	}

	/**
	 * //@ApiOperation(value = "Make a user unfollow another user",
	 * notes = "This method makes and user unfollow another user")
	 */
	@DeleteMapping(value = "/user/follow/{id}", produces = "application/json")
	public ResponseEntity<?> unfollowUser(@PathVariable (value = "id") String userToUnfollowId) {
		ResponseEntity<?> result;

		//we retrieve the id of the current logged-in user
		UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User currentUser = userSpring.getApplicationUser();
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "Could not find current logged in user");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not find current logged in user");
		}

		User userToUnfollow = UserService.getUserFromId(userToUnfollowId);
		if(userToUnfollow == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + userToUnfollowId);
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"could not find user with id="+userToUnfollowId+"\"}");
		}

		boolean followerRemoved = UserService.unfollowUser( currentUser, userToUnfollow);

		if (followerRemoved){
			LOGGER.log(Level.INFO, "Success: follower removed successfully");
			result = ResponseEntity.ok("Follower successfully removed!");
		} else {
			LOGGER.log(Level.INFO, "Error: could not remove the follower");
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not remove the follower");
		}

		return result;
	}

	// suggested followers for the current user
	@ApiOperation(value = "returns a list of suggested followers for the current user")
	@GetMapping(value = "/user/follow/suggestions", produces = "application/json")
	public ResponseEntity<?> suggestedFollowers() {
		ResponseEntity<?> result;
		try {
			UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User currentUser = userSpring.getApplicationUser();
			List<User> suggestedFollowers = UserService.getSuggestedFollowers(currentUser);

			if(suggestedFollowers.size()==0 || suggestedFollowers==null)
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"No suggestion about new users to follow\"}");
			else
				result = ResponseEntity.status(HttpStatus.OK).body(suggestedFollowers);
		}catch (Exception e) {
			result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting suggested followers\"}");
		}

		return result;
	}


	// suggested followers for the current user
	@ApiOperation(value = "returns a list of suggested posts for the current user")
	@GetMapping(value = "/user/post/suggestions", produces = "application/json")
	public ResponseEntity<?> suggestedPosts(@RequestParam(required = false) Integer howMany) {
		ResponseEntity<?> result;
		try {
			UserSpring userSpring = (UserSpring) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User currentUser = userSpring.getApplicationUser();
			List<PostPreview> suggestedPosts = UserService.getSuggestedPosts(currentUser, howMany);

			if(suggestedPosts.size()==0 || suggestedPosts==null)
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"No suggestion about new posts to check out\"}");
			else
				result = ResponseEntity.status(HttpStatus.OK).body(suggestedPosts);
		}catch (Exception e) {
			result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting suggested posts\"}");
		}

		return result;
	}


	// most active users in therms of posts written, given an activity
	@ApiOperation(value = "returns an aggregated result containing list of users by activity and their # of posts")
	@GetMapping(value = "/user/most-active", produces = "application/json")
	public ResponseEntity<?> mostActiveUsers(@RequestParam( defaultValue = "any", name = "activity") String activityFilter, @RequestParam(defaultValue = "3", name = "limit") int maxQuantity) {

		//TODO: admin role check

		ResponseEntity<?> result;

		try {
			List<Document> aggregatedValues = UserService.mostActiveUsersByActivity(activityFilter, maxQuantity);
			result = ResponseEntity.status(HttpStatus.OK).body(aggregatedValues);
		}catch (Exception e) {
			e.printStackTrace();
			result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"Error\":\"something went wrong in getting the aggregated value about most active users\"}");
		}

		return result;
	}
}
