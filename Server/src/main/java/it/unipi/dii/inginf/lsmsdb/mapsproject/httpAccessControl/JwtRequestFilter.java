package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;


import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.unipi.dii.inginf.lsmsdb.mapsproject.controller.JwtAuthenticationController;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.unipi.dii.inginf.lsmsdb.mapsproject.service.JwtUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger( JwtRequestFilter.class.getName() );

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String userID = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                userID = jwtTokenUtil.getIdFromToken(jwtToken);
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        } else {
            LOGGER.warning("JWT Token has not been initialized yet or does not begin with Bearer String");
        }

        // if the context is empty we have to generate the session context again
        if ((userID != null && username != null)) { //&& SecurityContextHolder.getContext().getAuthentication() == null

            // if token is valid configure Spring Security to manually set authentication
            if (jwtTokenUtil.validateToken(jwtToken)) {

                LOGGER.info("[+]: restoring session for the user " + username + " | the token is valid");
                //NOTE: generate the session starting from the given token so that the server has access to the currentUser instance
                //User currentUser = UserService.getUserFromId(userID);

                //assert currentUser.getId() == userID;

                /* NOTE: generate the session starting from the given token so that the server has access to the currentUser instance
                 * UsernamePasswordAuthenticationToken is a class that implements Authentication and that lets you
                 * store the principal object (in this case, the current logged in User instance) and the credentials used to login
                 * NOTE: it is unuseful to store the password in the authentication token, so we use null as second parameter
                 */
                //UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(currentUser, null);
                //the call to setAuthentication stores in the session the authentication information for the current user
                //SecurityContextHolder.getContext().setAuthentication(upat);
                //TODO: instead of an User object, pass as first parameter a class that implements UserDetails and that also has the user informations
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                //UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(currentUser, "STATIC");
                SecurityContextHolder.getContext().setAuthentication(auth);

                /*
                TODO: what does it do?
                upat.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                */
            }else{
                LOGGER.warning("[x]: the given token is not valid!");
                SecurityContextHolder.clearContext();
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "[x]: Invalid Token");
                return;
            }
        }
        chain.doFilter(request, response);
    }

}