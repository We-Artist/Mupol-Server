package com.mupol.mupolserver.advice.exception.sign;

public class InvalidSnsTokenException extends RuntimeException{
    public InvalidSnsTokenException(String msg, Throwable t) {
        super(msg, t);
    }

    public InvalidSnsTokenException(String msg) {
        super(msg);
    }

    public InvalidSnsTokenException() {
        super();
    }
}
