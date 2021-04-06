package com.mupol.mupolserver.advice.exception;

public class SnsNotSupportedException extends RuntimeException{
    public SnsNotSupportedException(String msg, Throwable t) {
        super(msg, t);
    }

    public SnsNotSupportedException(String msg) {
        super(msg);
    }

    public SnsNotSupportedException() {
        super();
    }
}
