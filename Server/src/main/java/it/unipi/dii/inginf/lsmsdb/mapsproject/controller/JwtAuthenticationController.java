package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        LOGGER.log(Level.INFO, "login request accepted for the user: " + u.toString());

        /*
         * UsernamePasswordAuthenticationToken is a class that implements Authentication and that lets you
         * store the principal object (in this case, the current logged in User instance) and the credentials used to login
         * NOTE: TODO: maybe it is better to store in this object the password as already hashed for security purposes...
         *      it is unuseful to store the password in the authentication token, so we use null as second parameter
         */
        //UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(u, null);
         //the call to setAuthentication stores in the session the authentication information for the current user
        //SecurityContextHolder.getContext().setAuthentication(upat);

        UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(u, "STATIC");
        SecurityContextHolder.getContext().setAuthentication(upat);


        return ResponseEntity.ok(new JwtResponse(token, expires, id));
    }
}
