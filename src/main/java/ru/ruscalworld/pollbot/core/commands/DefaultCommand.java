package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultCommand implements Command {
    private final String name;
    private final String description;
    private boolean global = true;

    protected DefaultCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData(this.getName(), this.getDescription());
    }

    @Override
    public void onPreRegister(CommandData data) {}

    @Override
    public void handleError(Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("Exception while handling command", throwable);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
