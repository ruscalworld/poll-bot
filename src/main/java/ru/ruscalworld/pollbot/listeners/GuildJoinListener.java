package ru.ruscalworld.pollbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.ruscalworld.pollbot.PollBot;

public class GuildJoinListener extends ListenerAdapter {
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        PollBot.getInstance().updateGuildCommands(event.getGuild()).queue();
    }
}
