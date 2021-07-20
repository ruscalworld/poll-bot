package ru.ruscalworld.pollbot.core.polls;

import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;
import ru.ruscalworld.pollbot.core.CommandException;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Model(table = "votes")
public class Vote extends DefaultModel {
    @NotNull
    private Poll poll;
    @Property(column = "variant_id")
    private long variantId;
    @NotNull private Variant variant;
    @Property(column = "member_id")
    private String memberId;
    @NotNull private User member;
    @Property(column = "reaction_id")
    private String reactionId;
    @Nullable
    private MessageReaction reaction;
    @Property(column = "created_at")
    private final Timestamp createdAt;

    public Vote(Variant variant, @NotNull User member, @Nullable MessageReaction reaction, Timestamp createdAt) {
        this.poll = variant.getPoll();
        this.variant = variant;
        this.member = member;
        this.memberId = member.getId();
        this.reaction = reaction;
        this.createdAt = createdAt;
    }

    public static Vote create(Variant variant, User user) throws Exception {
        Poll poll = variant.getPoll();

        if (poll.getEndsAt() != null && poll.getEndsAt().before(new Timestamp(System.currentTimeMillis())))
            throw new CommandException("This poll has ended, so you can't take part in it");

        List<Vote> votes = poll.getVotes(user);

        if (votes.size() > 0 && !poll.isRevoteAllowed()) {
            List<String> variants = new ArrayList<>();
            votes.forEach(vote -> variants.add(vote.getVariant().getDescription()));
            throw new CommandException("Вы уже проголосовали за " + String.join(", ", variants) +
                    " и не можете изменить свой выбор из-за настроек голосования");
        }

        if (!poll.isMultipleChoiceAllowed()) for (Vote vote : votes) vote.delete();

        Storage storage = PollBot.getInstance().getStorage();
        Vote vote = new Vote(variant, user, null, null);
        storage.save(vote);

        return vote;
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

    public @NotNull Variant getVariant() {
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