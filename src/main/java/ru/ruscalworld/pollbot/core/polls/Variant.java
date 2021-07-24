package ru.ruscalworld.pollbot.core.polls;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
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
    @Property(column = "title")
    private String title;
    @Property(column = "description")
    @Nullable private String description;

    private @NotNull List<Vote> votes = new ArrayList<>();
    private boolean votesFetched;

    public Variant(Poll poll, String name, String sign, String title) {
        this.poll = poll;
        this.name = name;
        this.sign = sign;
        this.title = title;
        this.pollId = poll.getId();
    }

    public Variant() {

    }

    public static @Nullable Variant get(long id) {
        try {
            Storage storage = PollBot.getInstance().getStorage();
            Variant variant = storage.retrieve(Variant.class, id);
            if (variant == null) return null;

            Poll poll = Poll.get(variant.getPollId());
            variant.setPoll(poll);
            return variant;
        } catch (Exception exception) {
            return null;
        }
    }

    public static @Nullable Variant get(long id, Poll poll) {
        try {
            Storage storage = PollBot.getInstance().getStorage();
            Variant variant = storage.retrieve(Variant.class, id);
            if (variant == null) return null;

            variant.setPoll(poll);
            return variant;
        } catch (Exception exception) {
            return null;
        }
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

    public static Variant create(Poll poll, String name, String sign, @Nullable String description, String title) throws Exception {
        List<String> emojis = EmojiParser.extractEmojis(sign);
        if (emojis.size() == 0) throw new InteractionException("Sign must be an emoji. Custom emojis are not supported.");
        try {
            Variant.getByName(name, poll);
            throw new InteractionException("Variant with this name already exists in selected poll");
        } catch (NotFoundException ignored) { }
        try {
            Variant.getBySign(sign, poll);
            throw new InteractionException("Variant with this sign already exists in selected poll");
        } catch (NotFoundException ignored) { }

        Storage storage = PollBot.getInstance().getStorage();
        Variant variant = new Variant(poll, name, emojis.get(0), title);
        variant.setDescription(description);
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

        Vote.getByVariant(this).forEach(vote -> {
            User user = PollBot.getInstance().getJDA().getUserById(vote.getMemberId());
            vote.setMember(Objects.requireNonNull(user));
            vote.setVariant(this);
            votes.add(vote);
        });

        this.votes = votes;
    }

    public @Nullable Vote vote(User user, GuildSettings settings) throws Exception {
        return Vote.create(this, user, settings);
    }

    public MessageReaction getReaction(@Nullable Message message) {
        if (message == null) return null;
        if (!Poll.isPoll(message)) return null;
        for (MessageReaction reaction : message.getReactions())
            if (reaction.getReactionEmote().getEmoji().equals(this.getSign())) return reaction;
        return null;
    }

    public Component makeButton() {
        return Button.secondary("vote " + this.getId(), Emoji.fromUnicode(this.getSign()));
    }

    public @Nullable String getDescription() {
        return description;
    }

    public Poll getPoll() {
        if (this.poll == null) try {
            this.poll = Poll.get(this.pollId);
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

    public @NotNull List<Vote> getVotes(User user) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        return storage.findAll(Vote.class, Condition.and(
                Comparison.equal("member_id", user.getId()),
                Comparison.equal("variant_id", this.getId())
        ));
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }
}
