package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class ClientValidationException extends RuntimeException {

    public ClientValidationException(String message) {
        super(message);
    }
}

