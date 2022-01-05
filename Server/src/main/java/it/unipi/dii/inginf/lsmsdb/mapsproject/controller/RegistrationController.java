package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseUnavailableException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {

    @PostMapping(value = "/api/register")
    public ResponseEntity<?> registerNewUser(@RequestBody RegistrationUser newRegistrationUser) {

        // checks on username and password duplicates are done inside UserService.register()
        User insertedUser;

        try{
            insertedUser = UserService.register(newRegistrationUser);
        } catch (DatabaseUnavailableException due){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DB is unreachable!");
        }

        //TODO: decide if combining some controllers
        //TODO: update the controller properly

        if(insertedUser != null) {
            insertedUser.setPassword("");
            return ResponseEntity.ok(insertedUser);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Username or Email already taken");
        }
    }
}