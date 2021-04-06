package com.mupol.mupolserver.advice;


import com.mupol.mupolserver.advice.exception.CUserIdDuplicatedException;
import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.advice.exception.SnsNotSupportedException;
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

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {

    private final ResponseService responseService;

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("unKnown.code")), getMessage("unKnown.msg"));
    }

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected CommonResult userNotFoundException(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("userNotFound.code")), getMessage("userNotFound.msg"));
    }

    @ExceptionHandler(SnsNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult snsNotSupportedException(HttpServletRequest request, SnsNotSupportedException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("snsNotSupported.code")), getMessage("snsNotSupported.msg"));
    }

    @ExceptionHandler(CUserIdDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userIdDuplicatedException(HttpServletRequest request, CUserIdDuplicatedException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("userIdDuplicated.code")), getMessage("userIdDuplicated.msg"));
    }

    @ExceptionHandler(UserDoesNotAgreeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult userDoesNotAgreeException(HttpServletRequest request, UserDoesNotAgreeException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("userDoesNotAgree.code")), getMessage("userDoesNotAgree.msg"));
    }

    @ExceptionHandler(InstrumentNotExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected CommonResult instrumentNotExistException(HttpServletRequest request, InstrumentNotExistException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("InstrumentNotExist.code")), getMessage("InstrumentNotExist.msg"));
    }
    private String getMessage(String code) {
        return getMessage(code, null);
    }

    private String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
