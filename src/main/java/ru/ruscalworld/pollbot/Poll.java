package ru.ruscalworld.pollbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.util.ProgressBar;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Model(table = "polls")
public class Poll extends DefaultModel {
    @Property(column = "name")
    private final String name;
    @Property(column = "title")
    @Nullable private String title;
    @Property(column = "description")
    @Nullable private String description;
    @Property(column = "ends_at")
    @Nullable private Timestamp endsAt;
    @Property(column = "message_id")
    private String messageId;
    @Property(column = "channel_id")
    private String channelId;
    @Nullable private Message message;
    @Property(column = "allow_revote")
    private boolean allowRevote;
    @Property(column = "allow_multiple_choice")
    private boolean allowMultipleChoice;
    @Property(column = "is_anonymous")
    private boolean anonymous;

    private final @NotNull List<Variant> variants = new ArrayList<>();

    public Poll(String name) {
        this.name = name;
    }

    public static boolean isPoll(Message message) {
        if (message.getReactions().size() == 0) return false;
        if (message.getEmbeds().size() != 1) return false;
        MessageEmbed embed = message.getEmbeds().get(0);
        if (embed.getTitle() == null) return false;
        if (embed.getColor() == null) return false;
        return embed.getFields().size() != 0;
    }

    public static Poll getFromMessage(Message message) throws Exception {
        if (!isPoll(message)) return null;
        Storage storage = PollBot.getInstance().getStorage();
        Poll poll = storage.find(Poll.class, "message_id", message.getId());
        poll.setMessage(message);
        poll.fetchVariants();
        return poll;
    }

    public static Poll create(String name) throws Exception {
        Poll poll = new Poll(name);
        poll.save();
        return poll;
    }

    public void publish(TextChannel channel) throws Exception {
        EmbedBuilder builder = this.getEmbed();
        Message message = channel.sendMessage(builder.build()).complete();
        for (Variant variant : this.getVariants()) message.addReaction(variant.getSign()).complete();

        this.setMessage(message);
        this.setMessageId(message.getId());
        this.setChannelId(channel.getId());
        this.save();
    }

    public void rerender() throws Exception {
        EmbedBuilder builder = this.getEmbed();
        builder.setFooter(this.getEmbedFooter());
        if (this.getEndsAt() != null) builder.setTimestamp(this.getEndsAt().toInstant());
        if (this.getMessage() != null) this.getMessage().editMessage(builder.build()).queue();
    }

    private String getEmbedFooter() {
        int memberCount = this.getMemberCount();
        return memberCount + "participants" +
                (this.isAnonymous() ? " • " + "Anonymous poll" : "") +
                (this.isMultipleChoiceAllowed() ? " • " + "Multiple choice allowed" : "") +
                (!this.isRevoteAllowed() ? " • " + "Revoting is disabled" : "");
    }

    public EmbedBuilder getEmbed() throws Exception {
        int totalVotes = this.getTotalVotes();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getTitle());
        builder.setDescription(this.getDescription());

        for (Variant variant : this.getVariants()) {
            float percentage = totalVotes > 0 ? (float) Math.round((float) variant.getVotes().size() / (float) totalVotes * 1000) / 10 : 0;
            String name = variant.getSign() + " • " + variant.getName();
            String value = ProgressBar.makeDefault(Math.round(percentage), 15) + " • " +
                    variant.getVotes() + " (" + percentage + "%)";
            builder.addField(name, value, false);
            builder.setFooter(this.getEmbedFooter());
        }

        return builder;
    }

    public int getTotalVotes() throws Exception {
        int total = 0;
        for (Variant variant : this.getVariants()) total += variant.getVotes().size();
        return total;
    }

    public int getMemberCount() {
        if (this.getMessage() == null) return 0;
        List<String> members = new ArrayList<>();
        int total = 0;

        for (Variant variant : this.getVariants()) {
            List<User> currentMembers = variant.getReaction(this.getMessage()).retrieveUsers().complete();
            for (User currentMember : currentMembers) {
                if (members.contains(currentMember.getId())) continue;
                if (currentMember.isBot()) continue;
                members.add(currentMember.getId());
                total++;
            }
        }

        return total;
    }

    public @NotNull List<Variant> getVariants() {
        return variants;
    }

    public void fetchVariants() throws Exception {
        List<Variant> variants = this.getVariants();
        variants.clear();
        Storage storage = PollBot.getInstance().getStorage();

        List<Variant> storedVariants = storage.findAll(Variant.class, "poll_id", this.getId());
        storedVariants.forEach(variant -> {
            variant.setPoll(this);
            variants.add(variant);
        });
    }

    public void save() throws Exception {
        Message message = this.getMessage();
        if (message != null) {
            this.setMessageId(message.getId());
            this.setChannelId(message.getChannel().getId());
        }

        Storage storage = PollBot.getInstance().getStorage();
        storage.save(this);
    }

    public List<Vote> getVotes(User user) throws Exception {
        Storage storage = PollBot.getInstance().getStorage();
        List<Vote> votes = storage.findAll(Vote.class, "member_id", user.getId());

        for (Vote vote : votes) {
            Variant variant = this.getVariant(vote.getVariantId());
            if (variant == null) continue;
            vote.setVariant(variant);
            vote.setMember(user);
        }

        return votes;
    }

    public @Nullable Variant getVariant(long id) throws Exception {
        return Variant.get(id, this);
    }

    public @Nullable Variant getVariant(String name) throws Exception {
        return Variant.get(name, this);
    }

    public boolean isMultipleChoiceAllowed() {
        return this.allowMultipleChoice;
    }

    public void setMultipleChoiceAllowed(boolean multipleChoice) {
        this.allowMultipleChoice = multipleChoice;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable Timestamp getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(@Nullable Timestamp endsAt) {
        this.endsAt = endsAt;
    }

    public @Nullable Message getMessage() {
        return message;
    }

    public void setMessage(@Nullable Message message) {
        this.message = message;
    }

    public boolean isRevoteAllowed() {
        return allowRevote;
    }

    public void setAllowRevote(boolean allowRevote) {
        this.allowRevote = allowRevote;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public @NotNull String getTitle() {
        return this.title != null ? this.title : this.name;
    }

    public String getName() {
        return name;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Model(table = "variants")
    public static class Variant extends DefaultModel {
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
            if (!isPoll(message)) return null;
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

    @Model(table = "votes")
    public static class Vote extends DefaultModel {
        @NotNull private Poll poll;
        @Property(column = "variant_id")
        private long variantId;
        @NotNull private Variant variant;
        @Property(column = "member_id")
        private String memberId;
        @NotNull private User member;
        @Property(column = "reaction_id")
        private String reactionId;
        @Nullable private MessageReaction reaction;
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
                throw new Error("This poll has ended, so you can't take part in it");

            List<Vote> votes = poll.getVotes(user);

            if (votes.size() > 0 && !poll.isRevoteAllowed()) {
                List<String> variants = new ArrayList<>();
                votes.forEach(vote -> variants.add(vote.getVariant().getDescription()));
                throw new Error("Вы уже проголосовали за " + String.join(", ", variants) +
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

    public static class Error extends Exception {
        public Error(String message) {
            super(message);
        }
    }
}