package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import java.util.Date;
import java.util.Objects;

import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserCredentialChecker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.JwtTokenUtil;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.JwtRequest;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.JwtResponse;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping(value = "/api/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        User u;
        //check validity of user credential and eventually create new user
        if(UserCredentialChecker.checkCredential(username, password)){
             u = new User(username, password);
        }
        else
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: wrong credentials!");

        final String token = jwtTokenUtil.generateToken(u);
        final Date expires = jwtTokenUtil.getExpirationDateFromToken(token);

        return ResponseEntity.ok(new JwtResponse(token, expires, u.getId()));
    }
}
