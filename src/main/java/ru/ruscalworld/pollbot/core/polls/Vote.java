package ru.ruscalworld.pollbot.core.polls;

import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.settings.GuildSettings;
import ru.ruscalworld.pollbot.exceptions.InteractionException;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;
import ru.ruscalworld.storagelib.builder.expressions.Comparison;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Model(table = "votes")
public class Vote extends DefaultModel {
    private Poll poll;
    @Property(column = "variant_id")
    private long variantId;
    private Variant variant;
    @Property(column = "member_id")
    private String memberId;
    private User member;
    @Property(column = "reaction_id")
    private String reactionId;
    @Nullable
    private MessageReaction reaction;
    @Property(column = "created_at")
    private Timestamp createdAt;

    public Vote(Variant variant, @NotNull User member, @Nullable MessageReaction reaction, Timestamp createdAt) {
        this.poll = variant.getPoll();
        this.variant = variant;
        this.variantId = variant.getId();
        this.member = member;
        this.memberId = member.getId();
        this.reaction = reaction;
        this.createdAt = createdAt;
    }

    public Vote() {

    }

    public static Vote create(Variant variant, User user, GuildSettings settings) throws Exception {
        Poll poll = variant.getPoll();

        if (poll.getEndsAt() != null && poll.getEndsAt().before(new Timestamp(System.currentTimeMillis())))
            throw new InteractionException(settings.translate("responses.vote.create.ended", "<t:" + (poll.getEndsAt().getTime() / 1000) + ":R>"));

        List<Vote> votes = poll.getVotes(user);

        if (votes.size() >= poll.getVotesPerUser() && !poll.isRevoteAllowed()) {
            List<String> variants = new ArrayList<>();
            votes.forEach(vote -> variants.add(vote.getVariant().getTitle()));
            throw new InteractionException(settings.translate("responses.vote.create.no-revoting", String.join(", ", variants)));
        }

        if (votes.size() >= poll.getVotesPerUser())
            throw new InteractionException(settings.translate(
                    "responses.vote.create.vote-limit.max", poll.getVotesPerUser(),
                    settings.translate(poll.getVotesPerUser(), "words.variant")
            ));

        Storage storage = PollBot.getInstance().getStorage();
        Vote vote = new Vote(variant, user, null, null);
        storage.save(vote);

        return vote;
    }

    public static List<Vote> getByVariant(Variant variant) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        return storage.findAll(Vote.class, Comparison.equal("variant_id", variant.getId()));
    }

    public void delete() throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        storage.delete(this);

        MessageReaction reaction = this.getReaction();
        if (reaction == null) return;
        reaction.removeReaction(this.getMember()).queue();
    }

    public @NotNull Poll getPoll() {
        return poll;
    }

    public Variant getVariant() {
        if (this.variant == null) try {
            this.variant = Variant.get(this.variantId);
        } catch (Exception ignored) { }
        return variant;
    }

    public @NotNull User getMember() {
        return member;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public @Nullable MessageReaction getReaction() {
        return reaction;
    }

    public void setVariant(@NotNull Variant variant) {
        this.poll = variant.getPoll();
        this.variant = variant;
    }

    public void setMember(@NotNull User member) {
        this.member = member;
    }

    public void setReaction(@Nullable MessageReaction reaction) {
        this.reaction = reaction;
    }

    public long getVariantId() {
        return variantId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

    public String getReactionId() {
        return reactionId;
    }

    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }
}