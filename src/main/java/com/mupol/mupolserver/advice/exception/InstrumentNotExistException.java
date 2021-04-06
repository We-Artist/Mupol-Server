package com.mupol.mupolserver.advice.exception;

public class InstrumentNotExistException extends RuntimeException{
    public InstrumentNotExistException(String msg, Throwable t) {
        super(msg, t);
    }

    public InstrumentNotExistException(String msg) {
        super(msg);
    }

    public InstrumentNotExistException() {
        super();
    }
}
