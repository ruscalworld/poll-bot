package ru.ruscalworld.pollbot.core;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
    CommandData getCommandData();
    void onRegister(CommandData data);
    void onExecute(SlashCommandEvent event);
    boolean isGlobal();
}
