package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.core.polls.Poll;
import ru.ruscalworld.pollbot.core.commands.DefaultCommand;
import ru.ruscalworld.pollbot.core.sessions.Session;
import ru.ruscalworld.pollbot.core.sessions.SessionManager;
import ru.ruscalworld.pollbot.exceptions.NotFoundException;

public class PollCommand extends DefaultCommand {
    public PollCommand() {
        super("poll", "Main command");
    }

    @Override
    public void onExecute(SlashCommandEvent event) throws Exception {
        if (event.getMember() == null) return;
        if (event.getSubcommandName() == null) return;

        SessionManager sessionManager = PollBot.getInstance().getSessionManager();
        Session session = sessionManager.getMemberSession(event.getMember());

        switch (event.getSubcommandName()) {
            case "create":
                OptionMapping nameOption = event.getOption("name");
                assert nameOption != null;
                Poll poll = Poll.create(nameOption.getAsString(), event.getMember());

                poll.preview(event.getHook());
                session.setSelectedPoll(poll);
                break;
            case "limit":
                OptionMapping valueOption = event.getOption("value");
                assert valueOption != null;
                poll = ensurePollIsSelected(session);
                ensurePollIsEditable(poll);
                if (valueOption.getAsLong() < 1) throw new InteractionException("Minimum value for limit is 1");

                poll.setVotesPerUser(((int) valueOption.getAsLong()));
                poll.save();
                poll.updateLatestMessage();
                event.getHook().sendMessage("Amount of maximum votes per user has been changed to " + poll.getVotesPerUser()).queue();
            case "anonymous":
            case "describe":
                break;
            case "select":
                nameOption = event.getOption("name");
                assert nameOption != null;
                if (event.getGuild() == null) return;

                poll = Poll.getByName(nameOption.getAsString(), event.getGuild());
                if (poll == null) throw new NotFoundException("Poll with this name does not exist");
                if (!poll.getOwnerId().equals(event.getMember().getId()))
                    throw new InteractionException("This poll was created by another member, and you can not edit it");

                session.setSelectedPoll(poll);
                event.getHook().sendMessage("You have successfully selected this poll").queue();
                break;
            case "preview":
                poll = ensurePollIsSelected(session);
                poll.preview(event.getHook());
                break;
            case "publish":
                poll = ensurePollIsSelected(session);
                poll.publish(event.getTextChannel());
                event.getHook().sendMessage("Your poll has been published to " + event.getTextChannel().getAsMention()).queue();
                break;
        }
    }

    public static Poll ensurePollIsSelected(Session session) throws InteractionException {
        if (session.getSelectedPoll() == null) throw new InteractionException("Please select a poll using /poll select");
        return session.getSelectedPoll();
    }

    public static void ensurePollIsEditable(Poll poll) throws InteractionException {
        if (poll.isPublished()) throw new InteractionException("This poll is published and cannot be edited");
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData().addSubcommands(
                new SubcommandData("create", "Creates a poll")
                        .addOption(OptionType.STRING, "name", "Name of the poll", true),
                new SubcommandData("describe", "Changes description of the poll")
                        .addOption(OptionType.STRING, "description", "New description of the poll", true),
                new SubcommandData("anonymous", "Makes poll anonymous or not")
                        .addOption(OptionType.BOOLEAN, "value", "Should your poll be anonymous?", true),
                new SubcommandData("limit", "Sets limit for amount of votes per user")
                        .addOption(OptionType.INTEGER, "value", "Maximum votes per user", true),
                new SubcommandData("select", "Selects a previously created poll to make you able to edit it")
                        .addOption(OptionType.STRING, "name", "Name of the poll", true),
                new SubcommandData("preview", "Sends a message for selected poll, so you can see how your poll will appear"),
                new SubcommandData("publish", "Publishes selected poll")
        );
    }
}
