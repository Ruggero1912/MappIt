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

	List<User> users = new ArrayList<User>();
	{
		users.add(new User("1", "Marco","Bianchi","User1", "pw1", "user1@test.com", User.Role.USER));
		users.add(new User("2", "Luca","Rossi","User2", "pw2", "user2@test.com", User.Role.ADMIN));
		users.add(new User("3", "Mario","Verdi","User3", "pw3", "user3@test.com", User.Role.USER));
		users.add(new User("4", "Gigi","Blu","User4", "pw4", "user4@test.com", User.Role.ADMIN));
	}

	@GetMapping(value = "/user/all", produces = "application/json")
	/**
	 * return all the Users in the database
	 * @param username A string containing the given username from the user
	 * //@ApiOperation(value = "Get information of every users", notes = "This method retrieve information about all the users")
	 */
	public List<User> getUsers() {
		// here we should check if the current User can access to this information
		// we need to access to the object of the current user
		return users;
	}

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

	@GetMapping(value = "/user/{id}", produces = "application/json")
	public User getUserById(@PathVariable(value = "id") String userId) {
		return users.stream().filter(x -> x.getId()==(userId)).collect(Collectors.toList()).get(0);
	}

	@GetMapping(value = "/user/role/{role}", produces = "application/json")
	public List<User> getUserByRole(@PathVariable(value = "role") String role) {
		return users.stream().filter(x -> x.getRole().toString().equalsIgnoreCase(role))
				.collect(Collectors.toList());
	}

	@DeleteMapping(value={"/user/{id}"}, produces = "application/json")
	public List<User> removeUser(@PathVariable(value = "id") String userId)
	{
		try{
			users.remove(users.stream().filter(x -> x.getId()==(userId)).collect(Collectors.toList()).get(0));
			System.out.println("User successfully deleted!");
		}catch (Exception e) {
			System.out.println("Error: could not delete Employee (id=" + userId + ")");
		}
		return users;
	}

}
