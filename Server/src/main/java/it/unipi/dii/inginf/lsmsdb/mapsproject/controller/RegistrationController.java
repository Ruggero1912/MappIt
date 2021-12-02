package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class RegistrationController {

    @PostMapping(value = "/api/register")
    public ResponseEntity<?> registerNewUser(@RequestBody User newUser) {

        String username = newUser.getUsername();
        String passwordHash = newUser.getPassword();
        String name = newUser.getName();
        String surname = newUser.getSurname();
        String email = newUser.getEmail();
        LocalDate birthDate = newUser.getBirthDate();

        // checks on username and password duplicates are done inside UserService.register()
        User insertedUser = UserService.register(username, passwordHash, name, surname, email, birthDate);

        //TODO: decide if combining some controllers
        //TODO: update the controller properly

        if(insertedUser != null) {
            return ResponseEntity.ok(insertedUser);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Username or Email already taken");
        }
    }
}