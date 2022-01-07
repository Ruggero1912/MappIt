package it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions;

public class DatabaseConstraintViolation extends Exception{
    public DatabaseConstraintViolation(String errorMessage) {super(errorMessage);}
}
