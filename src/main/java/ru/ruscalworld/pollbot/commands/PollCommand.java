package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.commands.Response;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.pollbot.core.polls.Poll;
import ru.ruscalworld.pollbot.core.commands.DefaultCommand;
import ru.ruscalworld.pollbot.core.sessions.Session;
import ru.ruscalworld.pollbot.core.sessions.SessionManager;
import ru.ruscalworld.pollbot.exceptions.NotFoundException;
import ru.ruscalworld.pollbot.util.Ensure;

public class PollCommand extends DefaultCommand {
    public PollCommand() {
        super("poll", "Main command");
    }

    @Override
    public Response onExecute(SlashCommandEvent event, GuildSettings settings) throws Exception {
        if (event.getSubcommandName() == null) return null;
        assert event.getGuild() != null && event.getMember() != null;

        SessionManager sessionManager = PollBot.getInstance().getSessionManager();
        Session session = sessionManager.getMemberSession(event.getMember());

        switch (event.getSubcommandName()) {
            case "create":
                OptionMapping nameOption = event.getOption("name");
                assert nameOption != null;
                Poll poll = Poll.create(nameOption.getAsString(), event.getMember());

                event.deferReply().queue();
                poll.preview(event.getHook(), settings);
                session.setSelectedPoll(poll);

                break;
            case "limit":
                OptionMapping valueOption = event.getOption("value");
                assert valueOption != null;

                poll = Ensure.ifPollIsSelected(settings, session);
                Ensure.ifPollIsEditable(settings, poll);
                if (valueOption.getAsLong() < 1) throw new InteractionException(settings, "responses.poll.per-user-limit.min", 1);

                poll.setVotesPerUser(((int) valueOption.getAsLong()));
                poll.save();
                poll.updateLatestMessage(settings);

                return Response.translation(settings, "responses.poll.per-user-limit.success", poll.getVotesPerUser());
            case "title":
                OptionMapping titleOption = event.getOption("title");
                assert titleOption != null;

                poll = Ensure.ifPollIsSelected(settings, session);
                Ensure.ifPollIsEditable(settings, poll);

                poll.setTitle(titleOption.getAsString());
                poll.save();
                poll.updateLatestMessage(settings);

                return Response.translation(settings, "responses.poll.title.success");
            case "describe":
                break;
            case "select":
                nameOption = event.getOption("name");
                assert nameOption != null && event.getGuild() != null;

                poll = Poll.getByName(nameOption.getAsString(), event.getGuild());
                if (poll == null) throw new NotFoundException(settings, "responses.poll.generic.unknown");
                if (!poll.getOwnerId().equals(event.getMember().getId()))
                    throw new InteractionException(settings, "responses.poll.generic.not-owner");

                session.setSelectedPoll(poll);
                return Response.translation(settings, "responses.poll.select.success");
            case "preview":
                event.deferReply().queue();
                poll = Ensure.ifPollIsSelected(settings, session);
                poll.preview(event.getHook(), settings);
                break;
            case "publish":
                poll = Ensure.ifPollIsSelected(settings, session);
                poll.publish(event.getTextChannel(), settings);
                return Response.translation(settings, "responses.poll.publish.success", event.getTextChannel().getAsMention());
        }

        return null;
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData().addSubcommands(
                new SubcommandData("create", "Creates a poll")
                        .addOption(OptionType.STRING, "name", "Name of the poll", true),
                new SubcommandData("describe", "Changes description of the poll")
                        .addOption(OptionType.STRING, "description", "New description of the poll", true),
                new SubcommandData("title", "Changes title of the poll")
                        .addOption(OptionType.STRING, "title", "New title of the poll", true),
                new SubcommandData("limit", "Sets limit for amount of votes per user")
                        .addOption(OptionType.INTEGER, "value", "Maximum votes per user", true),
                new SubcommandData("select", "Selects a previously created poll to make you able to edit it")
                        .addOption(OptionType.STRING, "name", "Name of the poll", true),
                new SubcommandData("preview", "Sends a message for selected poll, so you can see how your poll will appear"),
                new SubcommandData("publish", "Publishes selected poll")
        );
    }
}
