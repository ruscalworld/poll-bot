package ru.ruscalworld.pollbot.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import ru.ruscalworld.pollbot.core.interactions.DefaultInteractionHandler;
import ru.ruscalworld.pollbot.core.polls.Variant;
import ru.ruscalworld.pollbot.core.polls.Vote;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;

public class VoteInteraction extends DefaultInteractionHandler {
    public VoteInteraction() {
        super("vote");
    }

    @Override
    public void onButtonClick(ButtonClickEvent event, String[] args) throws Exception {
        if (event.getGuild() == null) return;
        GuildSettings settings = GuildSettings.getByGuild(event.getGuild());

        long id = Long.parseLong(args[0]);
        Variant variant = Variant.get(id);
        if (variant == null) return;

        Vote vote = variant.vote(event.getUser(), settings);
        if (vote == null) return;

        event.getHook().sendMessage(settings.translate("responses.vote.create.success", vote.getVariant().getTitle())).queue();
        vote.getPoll().updateLatestMessage(settings);
    }
}
