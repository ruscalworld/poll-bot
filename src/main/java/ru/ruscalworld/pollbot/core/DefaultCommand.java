package ru.ruscalworld.pollbot.core;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

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
