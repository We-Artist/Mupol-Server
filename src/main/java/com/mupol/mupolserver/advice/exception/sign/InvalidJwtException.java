package com.mupol.mupolserver.advice.exception.sign;

public class InvalidJwtException extends RuntimeException{
    public InvalidJwtException(String msg, Throwable t) {
        super(msg, t);
    }

    public InvalidJwtException(String msg) {
        super(msg);
    }

    public InvalidJwtException() {
        super();
    }
}
