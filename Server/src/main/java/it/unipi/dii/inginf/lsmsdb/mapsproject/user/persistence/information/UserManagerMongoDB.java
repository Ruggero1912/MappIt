package it.unipi.dii.inginf.lsmsdb.mapsproject.user.persistence.information;

import it.unipi.dii.inginf.lsmsdb.mapsproject.user.User;


public class UserManagerMongoDB implements UserManager{

    private static final UserManagerMongoDB obj = new UserManagerMongoDB(); //TODO

    private static final String USERCOLLECTIONKEY = "user";


    public UserManagerMongoDB(){

    }


    @Override
    public User login(String username, String password){

        return null;
    }
}
