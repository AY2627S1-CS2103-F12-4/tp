package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PARTICIPANT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Adds a person as a participant of an event.
 */
public class AddParticipantCommand extends Command {

    public static final String COMMAND_WORD = "add-participant";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a person as a participant of an event.\n"
            + "Parameters: EVENT_ID PERSON_ID [" + PREFIX_ROLE + " ROLE]... ["
            + PREFIX_PARTICIPANT_TAG + " TAG]...\n"
            + "Example: " + COMMAND_WORD + " E1 P1 " + PREFIX_ROLE + " Facilitator "
            + PREFIX_PARTICIPANT_TAG + " VIP";

    public static final String MESSAGE_SUCCESS = "Added person %2$s as a participant of event %1$s.";
    public static final String MESSAGE_DUPLICATE_PARTICIPANT =
            "Person %2$s is already a participant of event %1$s.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %1$s does not exist.";
    public static final String MESSAGE_PERSON_NOT_FOUND = "Person %1$s does not exist.";

    private final EventId eventId;
    private final PersonId personId;
    private final Set<ParticipationRole> roles;
    private final Set<Tag> tags;

    /**
     * Creates a command to add {@code personId} to {@code eventId}.
     */
    public AddParticipantCommand(EventId eventId, PersonId personId) {
        this(eventId, personId, Set.of(), Set.of());
    }

    /**
     * Creates a command to add {@code personId} to {@code eventId} with event-specific roles and tags.
     */
    public AddParticipantCommand(EventId eventId, PersonId personId,
                                 Set<ParticipationRole> roles, Set<Tag> tags) {
        requireAllNonNull(eventId, personId, roles, tags);
        this.eventId = eventId;
        this.personId = personId;
        this.roles = Set.copyOf(roles);
        this.tags = Set.copyOf(tags);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        try {
            model.addParticipation(eventId, personId, roles, tags);
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        } catch (PersonNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_PERSON_NOT_FOUND, personId), e);
        } catch (DuplicateParticipationException e) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PARTICIPANT, eventId, personId), e);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, eventId, personId));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddParticipantCommand otherCommand)) {
            return false;
        }
        return eventId.equals(otherCommand.eventId)
                && personId.equals(otherCommand.personId)
                && roles.equals(otherCommand.roles)
                && tags.equals(otherCommand.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, personId, roles, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("eventId", eventId)
                .add("personId", personId)
                .add("roles", roles)
                .add("tags", tags)
                .toString();
    }
}
