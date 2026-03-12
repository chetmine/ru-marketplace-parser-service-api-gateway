package chetmine.marketplace.parser.model;

public class RequestLimitExceededException extends RuntimeException {
    public RequestLimitExceededException(String message) {
        super(message);
    }
}
