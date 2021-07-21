package ru.ruscalworld.pollbot.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import ru.ruscalworld.pollbot.core.interactions.DefaultInteractionHandler;
import ru.ruscalworld.pollbot.core.polls.Variant;
import ru.ruscalworld.pollbot.core.polls.Vote;

public class VoteInteraction extends DefaultInteractionHandler {
    public VoteInteraction() {
        super("vote");
    }

    @Override
    public void onButtonClick(ButtonClickEvent event, String[] args) throws Exception {
        long id = Long.parseLong(args[0]);
        Variant variant = Variant.get(id);
        if (variant == null) return;
        Vote vote = variant.vote(event.getUser());
        if (vote == null) return;
        event.getHook().sendMessage("You have voted for " + vote.getVariant().getTitle()).queue();
        vote.getPoll().updateLatestMessage();
    }
}
