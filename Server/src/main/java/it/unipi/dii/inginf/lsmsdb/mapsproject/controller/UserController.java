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
			LOGGER.log(Level.INFO, "{\"Error\" : \"Unable to get JWT Token\"}");
			//return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"Unable to get JWT Token\"}");
			return null;
		} catch (ExpiredJwtException e) {
			LOGGER.log(Level.INFO, "{\"Error\" : \"JWT Token has expired\"}");
			//return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"JWT Token has expired\"}");
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
	@DeleteMapping(value={"/user/{id}"}, produces = "application/json")
	public ResponseEntity<?> deleteUser(@PathVariable(value = "id") String id){
		ResponseEntity<?> result;
		try{
			User userToDelete = UserService.getUserFromId(id);
			if(userToDelete != null) {
				UserService.deleteUser(userToDelete);
				result = ResponseEntity.ok("User successfully deleted (id="+id+ ")");
				LOGGER.log(Level.INFO, "User successfully deleted: " + id);
			} else {
				result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not delete employee (id=" + id + ")");
			}
		}
		catch (Exception e){
			result = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: could not delete employee (id=" + id + ")");
			LOGGER.log(Level.WARNING, "Error: could not delete Employee, an exception has occurred: " + e);
		}

		return result;
	}
}
