package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserManagerMongoDBTest {
        UserManagerMongoDB user;

        @BeforeEach
        void init(){
            user = new UserManagerMongoDB();
        }

        @DisplayName("Test UserManagerMongoDB.getUserFromUsername")
        @Test
        void GIVEN_get_user_from_username_WHEN_empty_username_is_passed_THEN_return_null(){
            assertNull(user.getUserFromUsername(""), "Empty username case is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.checkDuplicateUsername with empy username")
        @Test
        void GIVEN_check_duplicate_username_WHEN_empty_username_is_passed_THEN_return_false(){
            assertFalse(user.checkDuplicateUsername(""), "Empty username case in checkDuplicateUsername() is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.checkDuplicateUsername with duplicate username")
        @Test
        void GIVEN_check_duplicate_username_WHEN_duplicate_username_is_passed_THEN_return_true(){
            //we use a Username already taken
            String takenUsername = "gianfranco";
            RegistrationUser regUsr = mock(RegistrationUser.class);
            when(regUsr.getUsername()).thenReturn(takenUsername);
            assertTrue(user.checkDuplicateUsername(regUsr.getUsername()));
        }

        @DisplayName("Test UserManagerMongoDB.checkDuplicateEmail")
        @Test
        void GIVEN_check_duplicate_email_WHEN_empty_email_is_passed_THEN_return_false(){
            assertFalse(user.checkDuplicateEmail(""), "Empty username case in checkDuplicateEmail() is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.checkDuplicateEmail with duplicate email")
        @Test
        void GIVEN_check_duplicate_email_WHEN_duplicate_email_is_passed_THEN_return_true(){
            //we use an Email already taken
            String takenEmail = "gioacchinoantonini@example.com";
            RegistrationUser regUsr = mock(RegistrationUser.class);
            when(regUsr.getEmail()).thenReturn(takenEmail);
            assertTrue(user.checkDuplicateEmail(regUsr.getEmail()));
        }

        @DisplayName("Test UserManagerMongoDB.storeUser")
        @Test
        void GIVEN_store_user_WHEN_empty_registration_user_is_passed_THEN_throws_exception(){
            RegistrationUser regUsr = mock(RegistrationUser.class);
            assertNull(user.storeUser(regUsr));
        }

        @DisplayName("Test UserManagerMongoDB.getUserFromId")
        @Test
        void GIVEN_get_user_from_id_WHEN_empty_id_is_passed_THEN_return_null(){
            assertNull(user.getUserFromId(""), "Empty id case in getUserFromId() is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.deleteUserFromId")
        @Test
        void GIVEN_delete_user_from_id_WHEN_empty_id_is_passed_THEN_return_false(){
            assertFalse(user.deleteUserFromId(""), "Empty id case in deleteUserFromId() is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.changePassword with empty password")
        @Test
        void GIVEN_change_password_WHEN_empty_password_is_passed_THEN_return_false(){
            // We use a default user_id to test
            assertFalse(user.changePassword("61c3154f1dcfc81aae55085d", ""), "Empty password case in changePassword() is not well handled");
        }

        @DisplayName("Test UserManagerMongoDB.changePassword with empty id")
        @Test
        void GIVEN_change_password_WHEN_empty_id_is_passed_THEN_return_false(){
            assertFalse(user.changePassword("", "defaultPassword"), "Empty id case in changePassword() is not well handled");
        }
}
