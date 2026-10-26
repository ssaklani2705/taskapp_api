package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class DuplicateTitleException extends RuntimeException {
    public DuplicateTitleException(String message) {
        super(message);
    }
}