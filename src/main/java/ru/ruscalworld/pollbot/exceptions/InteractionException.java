package ru.ruscalworld.pollbot.exceptions;

import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public class InteractionException extends Exception {
    public InteractionException(String message) {
        super(message);
    }

    public InteractionException(GuildSettings settings, String key, Object... args) {
        super(settings.translate(key, args));
    }
}
