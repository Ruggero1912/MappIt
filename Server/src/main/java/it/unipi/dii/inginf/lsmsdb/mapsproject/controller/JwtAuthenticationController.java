package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserCredentialChecker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerFactory;
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


    private static final Logger LOGGER = Logger.getLogger( JwtAuthenticationController.class.getName() );

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping(value = "/api/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        User u = UserService.login(username, password);
        if(u == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"Error\" : \"wrong credentials!\"}");

        final String token = jwtTokenUtil.generateToken(u);
        final Date expires = jwtTokenUtil.getExpirationDateFromToken(token);
        final String id = u.getId();

        LOGGER.log(Level.SEVERE, "login request accepted for the user: " + u.toString());

        return ResponseEntity.ok(new JwtResponse(token, expires, id));
    }
}
