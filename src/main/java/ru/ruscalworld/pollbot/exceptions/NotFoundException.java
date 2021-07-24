package ru.ruscalworld.pollbot.exceptions;

import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public class NotFoundException extends InteractionException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(GuildSettings settings, String key, Object... args) {
        super(settings, key, args);
    }
}
