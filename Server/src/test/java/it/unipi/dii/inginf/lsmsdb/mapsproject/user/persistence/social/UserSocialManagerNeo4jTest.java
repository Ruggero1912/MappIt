package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.social;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UserSocialManagerNeo4jTest {

    UserSocialManagerNeo4j user;

    @BeforeEach
    void init(){
        user = new UserSocialManagerNeo4j();
    }

    @DisplayName("Test UserSocialManager.storeUser already inserted user is passed")
    @Test
    void GIVEN_store_user_WHEN_already_inserted_user_i_passed_THEN_throws_exception(){
        User usr = new User("61d3428101336eeafcb438e5", "", "", "StefanoStabile", "", "", User.Role.USER);
        Assertions.assertThrows( DatabaseConstraintViolation.class, () -> {
            user.storeUser(usr);
        });
    }

    @DisplayName("Test UserSocialManager.storeUser empty user is passed")
    @Test
    void GIVEN_store_user_WHEN_empty_user_is_passed_THEN_throws_exception() {
        User usr = mock(User.class);
        try {
            assertNull(user.storeUser(usr));
        } catch (DatabaseConstraintViolation e) {
            e.printStackTrace();
        }
    }
}
