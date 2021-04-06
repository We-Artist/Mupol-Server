package com.mupol.mupolserver.advice.exception;

public class ImageUploadFailException extends RuntimeException {
    public ImageUploadFailException(String msg, Throwable t) {
        super(msg, t);
    }

    public ImageUploadFailException(String msg) {
        super(msg);
    }

    public ImageUploadFailException() {
        super();
    }
}
