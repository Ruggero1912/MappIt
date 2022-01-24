package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UserServiceTest {

    private static final String DEFAULT_PASSWORD = "defaultPassword";
    private static final String DEFAULT_USERNAME = "61c3154f1dcfc81aae55085d";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String EMPTY_PASSWORD = "";
    private static final String EMPTY_USERNAME = "";
    private static final String TOO_SHORT_PASSWORD = "abc";

    /*@DisplayName("Test UserService.login")
    @Test
    void GIVEN_login_WHEN_empty_or_invalid_username_or_password_are_passed_THEN_return_null(){
        Assertions.assertAll(
                () -> assertNull(UserService.login(EMPTY_USERNAME,DEFAULT_PASSWORD)),
                () -> assertNull(UserService.login(DEFAULT_USERNAME,EMPTY_PASSWORD)),
                () -> assertNull(UserService.login(DEFAULT_USERNAME, WRONG_PASSWORD))
                );
    }

    @DisplayName("Test UserService.register with null RegistrationUser")
    @Test
    void GIVEN_register_WHEN_null_RegistrationUser_is_passed_THEN_return_null(){
        RegistrationUser regUsr = mock(RegistrationUser.class);
        // regUsr is a mock object with all fields set to null, so the register method must return null as well
        try {
            assertNull(UserService.register(regUsr));
        } catch(DatabaseErrorException e){
            e.printStackTrace();
        } catch (DatabaseConstraintViolation databaseConstraintViolation) {
            databaseConstraintViolation.printStackTrace();
        }
    }

    @DisplayName("Test UserService.checkPassword with empty or wrong password")
    @Test
    void GIVEN_checkPassword_WHEN_null_or_wrong_password_is_passed_THEN_return_false(){
        String hashedPsw=BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt());

        Assertions.assertAll(
                () -> assertFalse(UserService.checkPassword(WRONG_PASSWORD, hashedPsw)),
                () -> assertFalse(UserService.checkPassword(EMPTY_PASSWORD, hashedPsw))
        );
    }

    @DisplayName("Test UserService.delete with null userToDelete")
    @Test
    void GIVEN_delete_WHEN_null_user_is_passed_THEN_return_false(){
        User userToDelete = mock(User.class);
        try {
            assertFalse(UserService.delete(userToDelete));
        } catch (DatabaseErrorException e) {
            e.printStackTrace();
        }
    }

    @DisplayName("Test UserService.delete with existing userToDelete")
    @Test
    void GIVEN_delete_WHEN_extisting_user_is_passed_THEN_return_true(){
        User userToDelete = UserService.getUserFromId("");
        try {
            Assertions.assertTrue(UserService.delete(userToDelete));
        } catch (DatabaseErrorException e) {
            System.out.println(e.getMessage());
        }
    }

    @DisplayName("Test UserService.updatePassword with empty userID or empty newPassword")
    @Test
    void GIVEN_updatePassword_WHEN_empty_id_or_empty_newPassword_or_tooShortPassword_is_passed_THEN_return_false(){
        Assertions.assertAll(
                () -> assertFalse(UserService.updatePassword(EMPTY_USERNAME, DEFAULT_PASSWORD)),
                () -> assertFalse(UserService.updatePassword(EMPTY_PASSWORD, DEFAULT_USERNAME)),
                () -> assertFalse(UserService.updatePassword(EMPTY_USERNAME, EMPTY_PASSWORD)),
                () -> assertFalse(UserService.updatePassword(DEFAULT_USERNAME, TOO_SHORT_PASSWORD))
        );
    }*/
}
