package ru.ruscalworld.pollbot.core;

public class PollError extends Exception {
    public PollError(String message) {
        super(message);
    }
}
