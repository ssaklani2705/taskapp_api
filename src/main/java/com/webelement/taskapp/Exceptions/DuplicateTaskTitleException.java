package com.webelement.taskapp.Exceptions;

public class DuplicateTaskTitleException extends RuntimeException {
    public DuplicateTaskTitleException(String message) {
        super(message);
    }
}