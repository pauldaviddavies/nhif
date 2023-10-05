package com.sebin.uhc.exceptions;

public class ExceptionManager extends RuntimeException {
    public ExceptionManager() {
        super("General exception");
    }

    public ExceptionManager(String message, final String status) {
        super(message + "::" + status);
    }
    public ExceptionManager(String message){
        super(message);
    }
}
