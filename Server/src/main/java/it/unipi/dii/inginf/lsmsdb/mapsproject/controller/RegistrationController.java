package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RegistrationController {

    List<User> users = new ArrayList<User>();
    {
        users.add(new User("1", "Marco","Bianchi","User1", "pw1", "user1@test.com", User.Role.USER));
        users.add(new User("2", "Luca","Rossi","User2", "pw2", "user2@test.com", User.Role.ADMIN));
        users.add(new User("3", "Mario","Verdi","User3", "pw3", "user3@test.com", User.Role.USER));
    }

    @PostMapping(value = "/api/register", produces = "application/json")
    public ResponseEntity<?> registerNewUser(@RequestBody User newUser) {

        //method for checking duplicate user
        //...
        //if username id unique then save into user mongodb collection

        String username = newUser.getUsername();
        String password = newUser.getPassword();

        if(UserService.save(newUser)) {
            users.add(newUser);
            System.out.println(users);
            return ResponseEntity.ok(newUser);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error during registration process");
        }
    }
}