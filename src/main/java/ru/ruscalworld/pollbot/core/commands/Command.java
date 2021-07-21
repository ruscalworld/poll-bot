package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
    CommandData getCommandData();
    void onPreRegister(CommandData data);
    void onExecute(SlashCommandEvent event) throws Exception;
    void onError(Throwable throwable);
    boolean isGlobal();
}
