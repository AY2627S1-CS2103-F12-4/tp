package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean shouldShowHelp;

    /** The application should exit. */
    private final boolean shouldExit;

    /** The event list should be shown to the user. */
    private final boolean shouldShowEvents;

    /** The participant results should be shown to the user. */
    private final boolean shouldShowParticipants;

    /** The people list should be shown to the user. */
    private final boolean shouldShowPeople;

    /**
     * Constructs a result with help and exit instructions.
     */
    public CommandResult(
            String feedbackToUser,
            boolean shouldShowHelp,
            boolean shouldExit) {
        this(feedbackToUser, shouldShowHelp, shouldExit, false);
    }

    /**
     * Constructs a result with help, exit, and event-display instructions.
     */
    public CommandResult(
            String feedbackToUser,
            boolean shouldShowHelp,
            boolean shouldExit,
            boolean shouldShowEvents) {
        this(
                feedbackToUser,
                shouldShowHelp,
                shouldExit,
                shouldShowEvents,
                false);
    }

    /**
     * Constructs a result with participant-display instructions.
     */
    public CommandResult(
            String feedbackToUser,
            boolean shouldShowHelp,
            boolean shouldExit,
            boolean shouldShowEvents,
            boolean shouldShowParticipants) {
        this(
                feedbackToUser,
                shouldShowHelp,
                shouldExit,
                shouldShowEvents,
                shouldShowParticipants,
                false);
    }

    /**
     * Constructs a result with all display instructions.
     */
    public CommandResult(
            String feedbackToUser,
            boolean shouldShowHelp,
            boolean shouldExit,
            boolean shouldShowEvents,
            boolean shouldShowParticipants,
            boolean shouldShowPeople) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.shouldShowHelp = shouldShowHelp;
        this.shouldExit = shouldExit;
        this.shouldShowEvents = shouldShowEvents;
        this.shouldShowParticipants = shouldShowParticipants;
        this.shouldShowPeople = shouldShowPeople;
    }

    /**
     * Constructs a result containing only user feedback.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    /** Returns whether help information should be shown. */
    public boolean shouldShowHelp() {
        return shouldShowHelp;
    }

    /** Returns whether the application should exit. */
    public boolean shouldExit() {
        return shouldExit;
    }

    /** Returns whether the event list should be shown. */
    public boolean shouldShowEvents() {
        return shouldShowEvents;
    }

    /** Returns whether participant results should be shown. */
    public boolean shouldShowParticipants() {
        return shouldShowParticipants;
    }

    /** Returns whether the people list should be shown. */
    public boolean shouldShowPeople() {
        return shouldShowPeople;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(
                otherCommandResult.feedbackToUser)
                && shouldShowHelp == otherCommandResult.shouldShowHelp
                && shouldExit == otherCommandResult.shouldExit
                && shouldShowEvents == otherCommandResult.shouldShowEvents
                && shouldShowParticipants == otherCommandResult.shouldShowParticipants
                && shouldShowPeople == otherCommandResult.shouldShowPeople;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                feedbackToUser,
                shouldShowHelp,
                shouldExit,
                shouldShowEvents,
                shouldShowParticipants,
                shouldShowPeople);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("shouldShowHelp", shouldShowHelp)
                .add("shouldExit", shouldExit)
                .add("shouldShowEvents", shouldShowEvents)
                .add("shouldShowParticipants", shouldShowParticipants)
                .add("shouldShowPeople", shouldShowPeople)
                .toString();
    }
}
