package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final Logger LOGGER = Logger.getLogger( JwtAuthenticationEntryPoint.class.getName() );
    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    /**
     *this method is called by Spring when an AuthenticationException is raised from a request
     * (it happens inside a Controller or inside a Filter that is executed during the request processing)
     *
     * it prevents the normal behaviour that would return a html document containing the error (this would be unuseful for a RESTful backend)
     */
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        LOGGER.log(Level.INFO, "sending 401 unauthorized. authException: " + authException.toString());

        //authException.printStackTrace();

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}