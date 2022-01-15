package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserSpring implements UserDetails {

    private User applicationUser;

    public UserSpring(User user){
        this.applicationUser = user;
    }

    /**
     * use this method to access to the User instance of the currently logged-in user
     * @return the User instance of the logged user
     */
    public User getApplicationUser(){ return this.applicationUser; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

        // add user's authorities
        for (String userRole : this.applicationUser.getUserRole()) {
            setAuths.add(new SimpleGrantedAuthority(userRole));
        }

        List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

        return Result;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.applicationUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
