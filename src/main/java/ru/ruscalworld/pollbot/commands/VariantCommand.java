package ru.ruscalworld.pollbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.commands.DefaultCommand;
import ru.ruscalworld.pollbot.core.polls.Poll;
import ru.ruscalworld.pollbot.core.polls.Variant;
import ru.ruscalworld.pollbot.core.sessions.Session;
import ru.ruscalworld.pollbot.core.sessions.SessionManager;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public class VariantCommand extends DefaultCommand {
    public VariantCommand() {
        super("variant", "Main command to manage poll variants");
    }

    @Override
    public void onExecute(SlashCommandEvent event) throws Exception {
        if (event.getGuild() == null) return;
        if (event.getMember() == null) return;
        if (event.getSubcommandName() == null) return;

        GuildSettings settings = GuildSettings.getByGuild(event.getGuild());
        SessionManager sessionManager = PollBot.getInstance().getSessionManager();
        Session session = sessionManager.getMemberSession(event.getMember());
        Poll poll = PollCommand.ensurePollIsSelected(session, settings);
        PollCommand.ensurePollIsEditable(poll, settings);

        OptionMapping nameOption = event.getOption("name");
        assert nameOption != null;

        switch (event.getSubcommandName()) {
            case "create":
                OptionMapping titleOption = event.getOption("title");
                OptionMapping signOption = event.getOption("sign");
                OptionMapping descriptionOption = event.getOption("description");
                assert titleOption != null && signOption != null;

                Variant.create(poll,
                        nameOption.getAsString(),
                        signOption.getAsString(),
                        descriptionOption != null ? descriptionOption.getAsString() : null,
                        titleOption.getAsString()
                );

                poll.updateLatestMessage();
                event.getHook().sendMessage(settings.translate("responses.variant.create.success")).queue();
                break;
            case "delete":
                Variant variant = Variant.getByName(nameOption.getAsString(), poll);
                variant.delete();
                poll.updateLatestMessage();
                event.getHook().sendMessage(settings.translate("responses.variant.delete.success")).queue();
                break;
        }
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData().addSubcommands(
                new SubcommandData("create", "Create a new variant for selected poll")
                        .addOption(OptionType.STRING, "name", "Short name of this variant that will not be visible", true)
                        .addOption(OptionType.STRING, "title", "Long name of this variant that will be visible to everyone", true)
                        .addOption(OptionType.STRING, "sign", "An emoji for this variant", true)
                        .addOption(OptionType.STRING, "description", "More info about this variant that will be visible to everyone"),
                new SubcommandData("delete", "Delete variant from selected poll")
                        .addOption(OptionType.STRING, "name", "Short name of variant that should be deleted", true)
        );
    }
}
