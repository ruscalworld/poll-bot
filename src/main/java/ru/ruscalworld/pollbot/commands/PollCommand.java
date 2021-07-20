package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ru.ruscalworld.pollbot.core.Poll;
import ru.ruscalworld.pollbot.core.DefaultCommand;

public class PollCommand extends DefaultCommand {
    public PollCommand() {
        super("poll", "Main command");
    }

    @Override
    public void onExecute(SlashCommandEvent event) throws Exception {
        if (event.getSubcommandName() == null) return;
        switch (event.getSubcommandName()) {
            case "create":
                OptionMapping nameOption = event.getOption("name");
                assert nameOption != null && event.getMember() != null;
                Poll poll = Poll.create(nameOption.getAsString(), event.getMember());

                Message message = event.getHook().sendMessageEmbeds(poll.getEmbed().build()).complete();
                poll.setMessage(message);
                poll.save();
                break;
            case "anonymous":
            case "describe":
                break;
        }
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData().addSubcommands(
                new SubcommandData("create", "Creates a poll")
                        .addOption(OptionType.STRING, "name", "Name of the poll"),
                new SubcommandData("describe", "Changes description of the poll")
                        .addOption(OptionType.STRING, "description", "New description of the poll"),
                new SubcommandData("anonymous", "Makes poll anonymous or not")
                        .addOption(OptionType.BOOLEAN, "value", "Should your poll be anonymous?")
        );
    }
}
