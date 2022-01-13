package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import com.google.gson.Gson;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.JwtTokenUtil;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
	public User getCurrentUserInfo(@RequestHeader("Authorization") String authToken) {
		//we return the information about the current logged in user
		String token = JwtTokenUtil.parseTokenFromAuthorizationHeader(authToken);
		String userID;
		try {
			userID = JwtTokenUtil.getIdFromToken(token);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "{Error : Unable to get JWT Token}");
			return null;
		} catch (ExpiredJwtException e) {
			LOGGER.log(Level.INFO, "{Error : JWT Token has expired}");
			return null;
		}
		User currentUser = UserService.getUserFromId(userID);
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + userID);
			return null;
		}
		//UsernamePasswordAuthenticationToken upat = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		//User currentUser = (User) upat.getPrincipal();
		LOGGER.log(Level.INFO, "answering to the request... Response body: " + currentUser.toString());
		return currentUser;
	}

	/**
	 * //@ApiOperation(value = "Get information of a specific user",
	 * notes = "This method retrieve information of a specific user, given its _id")
	 */
	@GetMapping(value = "/user/{id}", produces = "application/json")
	public User getUserById(@PathVariable(value = "id") String userId) {
		return UserService.getUserFromId(userId);
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
				result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not find employee (id=" + id + ")");
			}
		}
		catch (Exception e){
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not delete employee (id=" + id + ")");
			LOGGER.log(Level.WARNING, "Error: could not delete Employee, an exception has occurred: " + e);
		}

		return result;
	}

	@PutMapping(value = "/user/password")
	public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authToken, String newPassword) {

		if(newPassword.equals("") || newPassword.length() < 4){ //password constrains need to be established
			LOGGER.log(Level.INFO, "{\"Error\" : \"Password must be at least 4 characters long\"}");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Password must be at least 4 characters long\"}");
		}

		String token = JwtTokenUtil.parseTokenFromAuthorizationHeader(authToken);
		String userID;
		try {
			userID = JwtTokenUtil.getIdFromToken(token);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"Unable to get JWT Token\"}");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Unable to get JWT Token\"}");
		} catch (ExpiredJwtException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"JWT Token has expired\"}");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"JWT Token has expired\"}");
		}

		User currentUser = UserService.getUserFromId(userID);
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "Could not find user with ID: " + userID);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not find user with ID: " + userID);
		}

		ResponseEntity<?> result;
		if(UserService.updatePassword(userID, newPassword)) {
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
	public ResponseEntity<?> followUser(@RequestHeader("Authorization") String authToken, @PathVariable (value = "id") String userToFollowId, @RequestBody(required = false) LocalDateTime localDateTime) {
		//we retrieve the id of the current logged-in user from auth token
		String token = JwtTokenUtil.parseTokenFromAuthorizationHeader(authToken);
		String currentUserID;
		try {
			currentUserID = JwtTokenUtil.getIdFromToken(token);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"Unable to get JWT Token\"}");
			return null;
		} catch (ExpiredJwtException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"JWT Token has expired\"}");
			return null;
		}

		// the time of the visit will be considered now if the localDateTime parameter is null
		if(localDateTime == null){
			localDateTime = LocalDateTime.now();
		}

		//we retrieve user objects of current logged-in user and user to follow
		User currentUser = UserService.getUserFromId(currentUserID);
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + currentUserID);
			return null;
		}

		User userToFollow = UserService.getUserFromId(userToFollowId);
		if(userToFollow == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + userToFollowId);
			return null;
		}

		boolean followerAdded = UserService.followUser( currentUser, userToFollow, localDateTime);

		if (followerAdded){
			return ResponseEntity.ok("Follower successfully added!");
		} else {
			LOGGER.log(Level.INFO, "Error: could not add the follower");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not add the follower");
		}
	}

	/**
	 * //@ApiOperation(value = "Make a user unfollow another user",
	 * notes = "This method makes and user unfollow another user")
	 */
	@DeleteMapping(value = "/user/follow/{id}", produces = "application/json")
	public ResponseEntity<?> unfollowUser(@RequestHeader("Authorization") String authToken, @PathVariable (value = "id") String userToUnfollowId) {
		//we retrieve the id of the current logged-in user from auth token
		String token = JwtTokenUtil.parseTokenFromAuthorizationHeader(authToken);
		String currentUserID;
		try {
			currentUserID = JwtTokenUtil.getIdFromToken(token);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"Unable to get JWT Token\"}");
			return null;
		} catch (ExpiredJwtException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"JWT Token has expired\"}");
			return null;
		}

		//we retrieve user objects of current logged-in user and user to follow
		User currentUser = UserService.getUserFromId(currentUserID);
		if(currentUser == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + currentUserID);
			return null;
		}

		User userToUnfollow = UserService.getUserFromId(userToUnfollowId);
		if(userToUnfollow == null){
			LOGGER.log(Level.WARNING, "user not found for userID: " + userToUnfollowId);
			return null;
		}

		boolean followerRemoved = UserService.unfollowUser( currentUser, userToUnfollow);

		if (followerRemoved){
			return ResponseEntity.ok("Follower successfully removed!");
		} else {
			LOGGER.log(Level.INFO, "Error: could not remove the follower");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not remove the follower");
		}
	}
}
