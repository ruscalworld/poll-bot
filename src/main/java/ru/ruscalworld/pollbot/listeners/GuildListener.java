package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.ruscalworld.pollbot.PollBot;

public class GuildListener extends ListenerAdapter {
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        PollBot.getInstance().updateGuildCommands(event.getGuild()).queue();
    }
}
