package it.unipi.dii.inginf.lsmsdb.mapsproject.httpAccessControl;

public class UserCredentialChecker {

    public static boolean checkCredential(String uname, String psw){
        //... call MongoDB Driver method to check if "uname" & "psw" are correct
        return (uname.equals("username") && psw.equals("password"));
    }
}
