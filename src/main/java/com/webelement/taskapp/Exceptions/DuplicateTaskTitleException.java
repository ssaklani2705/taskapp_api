package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class DuplicateTaskTitleException extends RuntimeException {
    public DuplicateTaskTitleException(String message) {
        super(message);
    }
}