package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;


import it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl.UserSpring;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    /** this method loads a UserSpring object which implements UserDetails
     * and which has a field of type User which is the User object of our application
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = UserService.getUserFromUsername(username);
        return parseUserSpringFromUser(user);
    }

    /** Converts user to UserSpring, which implements UserDetails
     * @param user the User obj of our app
     * @return UserSpring converted obj
     */
    private UserSpring parseUserSpringFromUser(User user){
        return new UserSpring(user);
    }

}