package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;


import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);

            // if token is valid configure Spring Security to manually set authentication
            if (jwtTokenUtil.validateToken(jwtToken)) {

                /* NOTE: generate the session starting from the given token so that the server has access to the currentUser instance
                 * UsernamePasswordAuthenticationToken is a class that implements Authentication and that lets you
                 * store the principal object (in this case, the current logged in UserSpring instance which implements UserDetails
                 * and that also has user's info) and the credentials used to login.
                 * NOTE: it is unuseful to store the password in the authentication token, so we use "" as second parameter
                 */

                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(jwtToken));
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                //the call to setAuthentication stores in the session the authentication information for the current user
                SecurityContextHolder.getContext().setAuthentication(auth);

            }else{
                LOGGER.warning("[x]: the given token is not valid!");
                SecurityContextHolder.clearContext();
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "[x]: Invalid Token");
                return;
            }
        } else {
            LOGGER.info("JWT Token has not been initialized yet or does not begin with Bearer String");
        }

        chain.doFilter(request, response);
    }

}