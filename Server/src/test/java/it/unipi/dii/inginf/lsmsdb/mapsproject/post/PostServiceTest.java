package it.unipi.dii.inginf.lsmsdb.mapsproject.post;

import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseConstraintViolation;
import it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions.DatabaseErrorException;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.RegistrationUser;
import it.unipi.dii.inginf.lsmsdb.mapsproject.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.text.Document;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class PostServiceTest {

    @DisplayName("Test PostService.createNewPost standard behaviour")
    @Test
    void GIVEN_create_new_post_WHEN_standard_parameters_are_passed_THEN_return_created_post(){

        Post newPost = new Post( "", "prova", "61dd6acd40871512a8c33b55", "61d33ff1a1dc9d89ac02f817", "postProva", new Date(), "", "", new ArrayList<String>(), "", "", "", new ArrayList<String>());

        try {
            assertNull(PostService.createNewPost(newPost));
        } catch(DatabaseErrorException e){
            e.printStackTrace();
        }
    }
}
