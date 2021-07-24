package ru.ruscalworld.pollbot.core.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public interface Command {
    CommandData getCommandData();
    @Nullable Response onExecute(SlashCommandEvent event, GuildSettings settings) throws Exception;
    void onPreRegister(CommandData data);
    void onError(Throwable throwable);
    boolean isGlobal();
}
