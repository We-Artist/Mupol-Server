package com.mupol.mupolserver.advice;


import com.mupol.mupolserver.advice.exception.*;
import com.mupol.mupolserver.advice.exception.sign.InvalidJwtException;
import com.mupol.mupolserver.advice.exception.sign.InvalidSnsTokenException;
import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.domain.response.CommonResult;
import com.mupol.mupolserver.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {

    private final ResponseService responseService;

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return responseService.getFailResult(getMessage("unKnown.msg"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult illegalArgumentException(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return responseService.getFailResult(e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult ioException(HttpServletRequest request, IOException e) {
        e.printStackTrace();
        return responseService.getFailResult(getMessage("io.msg"));
    }

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected CommonResult userNotFoundException(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.getFailResult(getMessage("userNotFound.msg"));
    }

    @ExceptionHandler(InvalidSnsTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected CommonResult invalidSnsTokenException(HttpServletRequest request, InvalidSnsTokenException e) {
        return responseService.getFailResult(getMessage("invalidSnsToken.msg"));
    }

    @ExceptionHandler(SnsNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult snsNotSupportedException(HttpServletRequest request, SnsNotSupportedException e) {
        return responseService.getFailResult(getMessage("snsNotSupported.msg"));
    }

    @ExceptionHandler(CUserIdDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userIdDuplicatedException(HttpServletRequest request, CUserIdDuplicatedException e) {
        return responseService.getFailResult(getMessage("userIdDuplicated.msg"));
    }

    @ExceptionHandler(UserDoesNotAgreeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userDoesNotAgreeException(HttpServletRequest request, UserDoesNotAgreeException e) {
        return responseService.getFailResult(getMessage("userDoesNotAgree.msg"));
    }

    @ExceptionHandler(InvalidJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected CommonResult invalidJwtException(HttpServletRequest request, InvalidJwtException e) {
        return responseService.getFailResult(getMessage("invalidJwt.msg"));
    }

    @ExceptionHandler(InstrumentNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult instrumentNotExistException(HttpServletRequest request, InstrumentNotExistException e) {
        return responseService.getFailResult(getMessage("InstrumentNotExist.msg"));
    }

    @ExceptionHandler(ImageUploadFailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult imageUploadFailException(HttpServletRequest request, ImageUploadFailException e) {
        return responseService.getFailResult(getMessage("ImageUploadFail.msg"));
    }

    private String getMessage(String code) {
        return getMessage(code, null);
    }

    private String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
