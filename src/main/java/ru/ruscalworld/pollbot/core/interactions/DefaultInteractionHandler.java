package ru.ruscalworld.pollbot.core.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultInteractionHandler implements InteractionHandler {
    private final String name;

    public DefaultInteractionHandler(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onError(Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("Exception while handling interaction \"{}\"", this.getName(), throwable);
    }
}
