package ru.ruscalworld.pollbot.exceptions;

public class NotFoundException extends CommandException {
    public NotFoundException(String message) {
        super(message);
    }
}
