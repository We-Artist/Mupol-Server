package com.mupol.mupolserver.advice.exception;

public class CUserIdDuplicatedException extends RuntimeException{

    public CUserIdDuplicatedException(String msg, Throwable t) {
        super(msg, t);
    }

    public CUserIdDuplicatedException(String msg) {
        super(msg);
    }

    public CUserIdDuplicatedException() {
        super();
    }
}
