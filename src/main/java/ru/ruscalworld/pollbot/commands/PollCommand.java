package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import ru.ruscalworld.pollbot.core.DefaultCommand;

public class PollCommand extends DefaultCommand {
    public PollCommand() {
        super("poll", "Main command");
    }

    @Override
    public void onExecute(SlashCommandEvent event) {

    }

    @Override
    public void onRegister(CommandData data) {
        data.addSubcommands(
                new SubcommandData("create", "Creates a poll")
                        .addOption(OptionType.STRING, "name", "Name of the poll"),
                new SubcommandData("describe", "Changes description of the poll")
                        .addOption(OptionType.STRING, "description", "New description of the poll"),
                new SubcommandData("anonymous", "Makes poll anonymous or not")
                        .addOption(OptionType.BOOLEAN, "value", "Should your poll be anonymous?")
        );
    }
}
