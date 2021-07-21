package ru.ruscalworld.pollbot.exceptions;

public class NotFoundException extends InteractionException {
    public NotFoundException(String message) {
        super(message);
    }
}
