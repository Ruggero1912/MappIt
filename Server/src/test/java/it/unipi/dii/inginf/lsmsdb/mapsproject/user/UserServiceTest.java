package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @DisplayName("Test UserService.login")
    @Test
    void GIVEN_login_WHEN_empty_or_invalid_username_or_password_are_passed_THEN_return_null(){
        Assertions.assertAll(
                () -> assertNull(UserService.login("","defaultPassword")),
                () -> assertNull(UserService.login("61c3154f1dcfc81aae55085d","")),
                () -> assertNull(UserService.login("61c3154f1dcfc81aae55085d", "wrongPassword"))
                );
    }

    @DisplayName("Test UserService.register with null RegistrationUser")
    @Test
    void GIVEN_register_WHEN_null_RegistrationUser_is_passed_THEN_return_null(){
        RegistrationUser regUsr = mock(RegistrationUser.class);
        // regUsr is a mock object with all fields set to null, so the register method must return null as well
        assertNull(UserService.register(regUsr));
    }

}
