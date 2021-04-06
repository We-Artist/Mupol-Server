package com.mupol.mupolserver.advice.exception.sign;

public class UserDoesNotAgreeException extends RuntimeException{
    public UserDoesNotAgreeException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserDoesNotAgreeException(String msg) {
        super(msg);
    }

    public UserDoesNotAgreeException() {
        super();
    }
}
