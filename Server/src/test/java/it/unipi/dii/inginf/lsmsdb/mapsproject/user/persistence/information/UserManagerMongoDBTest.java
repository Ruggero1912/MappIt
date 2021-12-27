package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserManagerMongoDBTest {
        UserManagerMongoDB user;

        @BeforeEach
        void setUp(){
            user = new UserManagerMongoDB();
        }

        @DisplayName("Test UserManagerMongoDB.getUserFromUsername")
        @Test
        void GIVEN_empty_username_THEN_return_NULL_user_object(){
            assertNull(user.getUserFromUsername(""), "Empty username case is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.checkDuplicateUsername")
        @Test
        void GIVEN_empty_username_THEN_return_FALSE(){
            assertFalse(user.checkDuplicateUsername(""), "Empty username case is not well handled");
        }
}
