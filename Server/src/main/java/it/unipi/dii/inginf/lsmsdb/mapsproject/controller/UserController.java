package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")

public class UserController {

	List<User> users = new ArrayList<User>();
	{
		users.add(new User(1, "Marco","Bianchi","User1", "pw1", "user1@test.com", User.Role.USER));
		users.add(new User(2, "Luca","Rossi","User2", "pw2", "user2@test.com", User.Role.ADMIN));
		users.add(new User(3, "Mario","Verdi","User3", "pw3", "user3@test.com", User.Role.USER));
		users.add(new User(4, "Gigi","Blu","User4", "pw4", "user4@test.com", User.Role.ADMIN));
	}

	@RequestMapping(value = "/user/all", method = RequestMethod.GET, produces = "application/json")
	//@ApiOperation(value = "Get information of every users", notes = "This method retrieve information about all the users")
	public List<User> getUsers() {
		return users;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
	public User getUserById(@PathVariable(value = "id") int employeeId) {
		return users.stream().filter(x -> x.getId()==(employeeId)).collect(Collectors.toList()).get(0);
	}

	@RequestMapping(value = "/user/role/{role}", method = RequestMethod.GET, produces = "application/json")
	public List<User> getUserByRole(@PathVariable(value = "role") String role) {
		return users.stream().filter(x -> x.getRole().toString().equalsIgnoreCase(role))
				.collect(Collectors.toList());
	}

	@RequestMapping(value={"/user/{id}"}, method = RequestMethod.DELETE, produces = "application/json")
	public List<User> removeUser(@PathVariable(value = "id") int userId)
	{
		try{
			users.remove(users.stream().filter(x -> x.getId()==(userId)).collect(Collectors.toList()).get(0));
			System.out.println("Employee successfully deleted!");
		}catch (Exception e) {
			System.out.println("Error: could not delete Employee (id=" + userId + ")");
		}
		return users;
	}

}
