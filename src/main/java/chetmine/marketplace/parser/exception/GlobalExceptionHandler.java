package chetmine.marketplace.parser.exception;

import chetmine.marketplace.parser.service.TooManyResendCodeRequestsException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleMessagingException(EntityNotFoundException e) {
        return ErrorResponse.create(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ErrorResponse handleMessagingException(AuthException e) {
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RiskTokenException.class)
    public ErrorResponse handleMessagingException(RiskTokenException e) {
        return ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TooManyResendCodeRequestsException.class)
    public ErrorResponse handleMessagingException(TooManyResendCodeRequestsException e) {
        return ErrorResponse.create(e, HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(MessagingException.class)
    public ErrorResponse handleMessagingException(MessagingException e) {
        return ErrorResponse.create(e, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email.");
    }

    @ExceptionHandler(EntityExistsException.class)
    public ErrorResponse handleEntityAlreadyExist(EntityExistsException e) {
        return ErrorResponse.create(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception exception) {
        return ErrorResponse.create(exception, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}
