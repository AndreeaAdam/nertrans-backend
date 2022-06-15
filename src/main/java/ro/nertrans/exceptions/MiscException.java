package ro.nertrans.exceptions;

public class MiscException extends RuntimeException {
    public MiscException(String errorMessage){
        super(errorMessage);
    }
}