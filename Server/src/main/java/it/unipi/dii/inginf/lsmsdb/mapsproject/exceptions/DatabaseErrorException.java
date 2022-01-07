package it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions;

public class DatabaseErrorException extends Exception{
    public DatabaseErrorException(String errorMessage) {
        super(errorMessage);
    }
}
