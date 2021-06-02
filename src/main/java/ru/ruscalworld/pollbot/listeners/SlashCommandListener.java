package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.Command;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String name = event.getName();
        Command command = PollBot.getInstance().getCommands().get(name);
        if (command == null) return;

        command.onExecute(event);
    }
}
