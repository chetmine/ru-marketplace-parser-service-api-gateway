package chetmine.marketplace.parser.service;

public class TooManyResendCodeRequestsException extends RuntimeException {
    public TooManyResendCodeRequestsException(String message) {
        super(message);
    }
}
