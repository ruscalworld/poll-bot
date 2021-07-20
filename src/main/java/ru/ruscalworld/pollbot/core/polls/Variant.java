package ru.ruscalworld.pollbot.core.polls;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.exceptions.CommandException;
import ru.ruscalworld.pollbot.exceptions.NotFoundException;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;
import ru.ruscalworld.storagelib.builder.expressions.Comparison;
import ru.ruscalworld.storagelib.builder.expressions.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Model(table = "variants")
public class Variant extends DefaultModel {
    @Property(column = "poll_id")
    private long pollId;
    private Poll poll;
    @Property(column = "name")
    private String name;
    @Property(column = "sign")
    private String sign;
    @Property(column = "description")
    private String description;

    private @NotNull List<Vote> votes = new ArrayList<>();
    private boolean votesFetched;

    public Variant(Poll poll, String name, String sign, String description) {
        this.poll = poll;
        this.name = name;
        this.sign = sign;
        this.pollId = poll.getId();
        this.description = description;
    }

    public Variant() {

    }

    public static @Nullable Variant get(long id, Poll poll) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        Variant variant = storage.retrieve(Variant.class, id);
        if (variant == null) return null;

        variant.setPoll(poll);
        return variant;
    }

    public static @NotNull Variant getByName(String name, Poll poll) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Variant> variants = storage.findAll(Variant.class, Condition.and(
                Comparison.equal("name", name),
                Comparison.equal("poll_id", poll.getId())
        ));

        if (variants.size() == 0) throw new NotFoundException("Variant with this name does not exist");
        return variants.get(0);
    }

    public static @NotNull Variant getBySign(String sign, Poll poll) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Variant> variants = storage.findAll(Variant.class, Condition.and(
                Comparison.equal("sign", sign),
                Comparison.equal("poll_id", poll.getId())
        ));

        if (variants.size() == 0) throw new NotFoundException("Variant with this sign does not exist");
        return variants.get(0);
    }

    public static Variant create(Poll poll, String name, String sign, String description) throws Exception {
        try {
            Variant.getByName(name, poll);
            throw new CommandException("Variant with this name already exists in selected poll");
        } catch (NotFoundException ignored) { }
        try {
            Variant.getBySign(sign, poll);
            throw new CommandException("Variant with this sign already exists in selected poll");
        } catch (NotFoundException ignored) { }
        if (sign.contains(">")) throw new CommandException("Sorry, but custom emojis are not supported yet");

        Storage storage = PollBot.getInstance().getStorage();
        Variant variant = new Variant(poll, name, sign, description);
        storage.save(variant);
        variant.getPoll().getVariants().add(variant);
        return variant;
    }

    public void delete() throws Exception {
        this.getPoll().getVariants().remove(this);
        Storage storage = PollBot.getInstance().getStorage();
        storage.deleteAll(Vote.class, Comparison.equal("variant_id", this.getId()));
        storage.delete(this);
    }

    public String getName() {
        return name;
    }

    public String getSign() {
        return sign;
    }

    public void fetchVotes() throws Exception {
        List<Vote> votes = new ArrayList<>();

        Storage storage = PollBot.getInstance().getStorage();
        List<Vote> storedVotes = storage.findAll(Vote.class, Comparison.equal("variant_id", this.getId()));

        storedVotes.forEach(vote -> {
            User user = PollBot.getInstance().getJDA().getUserById(vote.getMemberId());
            vote.setMember(Objects.requireNonNull(user));
            vote.setVariant(this);
            votes.add(vote);
        });

        this.votes = votes;
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
        if (this.poll == null) try {
            return Poll.get(this.pollId);
        } catch (Exception ignored) { }
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
