package ru.ruscalworld.pollbot.core;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Model(table = "variants")
public class Variant extends DefaultModel {
    @Property(column = "poll_id")
    private long pollId;
    private Poll poll;
    @Property(column = "name")
    private final String name;
    @Property(column = "sign")
    private final String sign;
    @Property(column = "description")
    private final String description;

    private final @NotNull List<Vote> votes = new ArrayList<>();
    private boolean votesFetched;

    public Variant(Poll poll, String name, String sign, String description) {
        this.poll = poll;
        this.name = name;
        this.sign = sign;
        this.description = description;
    }

    public static @Nullable Variant get(long id, Poll poll) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        Variant variant = storage.retrieve(Variant.class, id);
        if (variant == null) return null;

        variant.setPoll(poll);
        return variant;
    }

    public static @Nullable Variant get(String name, Poll poll) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Variant> variants = storage.findAll(Variant.class, "name", name);
        for (Variant variant : variants) {
            variant.setPoll(poll);
            if (variant.getName().equals(name)) return variant;
        }

        return null;
    }

    public static Variant create(Poll poll, String name, String sign, String description) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        Variant variant = new Variant(poll, name, sign, description);
        storage.save(variant);
        return variant;
    }

    public String getName() {
        return name;
    }

    public String getSign() {
        return sign;
    }

    public void fetchVotes() throws Exception {
        List<Vote> votes = this.getVotes();
        votes.clear();

        Storage storage = PollBot.getInstance().getStorage();
        List<Vote> storedVotes = storage.findAll(Vote.class, "variant_id", this.getId());

        storedVotes.forEach(vote -> {
            User user = PollBot.getInstance().getJDA().getUserById(vote.getMemberId());
            vote.setMember(Objects.requireNonNull(user));
            vote.setVariant(this);
            votes.add(vote);
        });
    }

    public @Nullable Vote vote(User user) throws Exception {
        return Vote.create(this, user);
    }

    public MessageReaction getReaction(@Nullable Message message) {
        if (message == null) return null;
        if (!Poll.isPoll(message)) return null;
        for (MessageReaction reaction : message.getReactions())
            if (reaction.getReactionEmote().getEmoji().equals(this.getSign())) return reaction;
        return null;
    }

    public String getDescription() {
        return description;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public @NotNull List<Vote> getVotes() throws Exception {
        if (!this.isVotesFetched()) {
            this.fetchVotes();
            this.setVotesFetched(true);
        }

        return votes;
    }

    public boolean isVotesFetched() {
        return votesFetched;
    }

    public void setVotesFetched(boolean votesFetched) {
        this.votesFetched = votesFetched;
    }

    public long getPollId() {
        return pollId;
    }

    public void setPollId(long pollId) {
        this.pollId = pollId;
    }
}
