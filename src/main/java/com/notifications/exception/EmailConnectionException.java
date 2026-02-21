package com.notifications.exception;

public class EmailConnectionException extends RuntimeException {
    public EmailConnectionException(String message) { super(message); }
    public EmailConnectionException(String message, Throwable cause) { super(message, cause); }
}
