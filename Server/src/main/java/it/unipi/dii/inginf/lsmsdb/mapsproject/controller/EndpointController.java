package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.GeoLocation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class EndpointController {

	List<User> users = new ArrayList<User>();
	{
		users.add(new User(1, "Marco","Bianchi","User1", "pw1", "user1@test.com", "USER"));
		users.add(new User(2, "Luca","Rossi","User2", "pw2", "user2@test.com", "ADMIN"));
		users.add(new User(3, "Mario","Verdi","User3", "pw3", "user3@test.com", "USER"));
		users.add(new User(4, "Gigi","Blu","User4", "pw4", "user4@test.com", "ADMIN"));
	}

	List<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
	{
		geoLocations.add(new GeoLocation(1, 134.2, -122.399, "via ambrosiana 23, livorno LI 57100"));
		geoLocations.add(new GeoLocation(2, 34.7, 230.22, "via ernesto rossi 12, San Baronto PI 51035"));
	}


	@RequestMapping(value = "/getUsers", method = RequestMethod.GET, produces = "application/json")
	public List<User> getUsers() {
		return users;
	}

	@RequestMapping(value = "/getUser/{id}", method = RequestMethod.GET, produces = "application/json")
	public User getUserById(@PathVariable(value = "id") int employeeId) {
		return users.stream().filter(x -> x.getId()==(employeeId)).collect(Collectors.toList()).get(0);
	}

	@RequestMapping(value = "/getUser/role/{role}", method = RequestMethod.GET, produces = "application/json")
	public List<User> getUserByRole(@PathVariable(value = "role") String role) {
		return users.stream().filter(x -> x.getRole().equalsIgnoreCase(role))
				.collect(Collectors.toList());
	}

	@RequestMapping(value={"/delete/{id}"}, method = RequestMethod.DELETE, produces = "application/json")
	public List<User> removeEmployee(@PathVariable(value = "id") int employeeId)
	{
		try{
			users.remove(users.stream().filter(x -> x.getId()==(employeeId)).collect(Collectors.toList()).get(0));
			System.out.println("Employee successfully deleted!");
		}catch (Exception e) {
			System.out.println("Error: could not delete Employee (id=" + employeeId + ")");
		}
		return users;
	}

	@RequestMapping(value = "/getLocations", method = RequestMethod.GET, produces = "application/json")
	public List<GeoLocation> getLocations() {
		return geoLocations;
	}

}
