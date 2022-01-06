package it.unipi.dii.inginf.lsmsdb.mapsproject.exceptions;

public class DatabaseUnavailableException extends Exception{
    public DatabaseUnavailableException(String errorMessage) {
        super(errorMessage);
    }
}
