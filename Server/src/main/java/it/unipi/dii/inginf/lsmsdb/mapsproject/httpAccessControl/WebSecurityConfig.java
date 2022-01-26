package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;


    @Autowired
    private UserDetailsService jwtUserDetailsService;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
        //DONE: set a proper UserDetailsService class in order to make it work the UsernamePasswordAuthenticationToken.getUserByUsername method
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF
        httpSecurity.csrf().disable()
                // don't authenticate this particular request
                .authorizeRequests().antMatchers("/api/user/login", "/api/user/register", "/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll().
                // all other requests need to be authenticated
                        anyRequest().authenticated(). //comment this line if you want to disable authentication mechanism
                // we use stateless session, it won't be used to store user's state
                        and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}