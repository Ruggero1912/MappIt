package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information.UserManagerMongoDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class UserSocialManagerNeo4jTest {

    UserSocialManagerNeo4j user;

    @BeforeEach
    void init(){
        user = new UserSocialManagerNeo4j();
    }

    @DisplayName("Test UserSocialManager.storeUser")
    @Test
    void GIVEN_store_user_WHEN_empty_registration_user_is_passed_THEN_throws_exception(){
        User usr = mock(User.class);
        assertNull(user.storeUser(usr));
    }
}
