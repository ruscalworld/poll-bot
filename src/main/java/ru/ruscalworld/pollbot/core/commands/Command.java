package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public interface Command {
    CommandData getCommandData();
    void onPreRegister(CommandData data);
    void onExecute(SlashCommandEvent event, GuildSettings settings) throws Exception;
    void onError(Throwable throwable);
    boolean isGlobal();
}
