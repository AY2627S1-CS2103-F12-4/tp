package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.exceptions.ParticipationNotFoundException;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Edits a participant's roles and tags for an event.
 */
public class EditParticipantCommand extends Command {

    public static final String COMMAND_WORD = "edit-participant";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits a participant's roles and tags for an event.\n"
            + "Parameters: EVENT_ID PERSON_ID [/role ROLE]... [/tag TAG]...\n"
            + "Example: " + COMMAND_WORD + " E1 P1 /role Speaker /tag VIP";

    public static final String MESSAGE_SUCCESS = "Updated person %2$s for event %1$s.";
    public static final String MESSAGE_NO_CHANGES = "At least one role or tag field must be provided.";
    public static final String MESSAGE_PARTICIPANT_NOT_FOUND =
            "Person %2$s is not a participant of event %1$s.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %1$s does not exist.";
    public static final String MESSAGE_PERSON_NOT_FOUND = "Person %1$s does not exist.";

    private final EventId eventId;
    private final PersonId personId;
    private final Set<ParticipationRole> roles;
    private final Set<Tag> tags;

    /**
     * Creates a command to edit roles and tags for {@code personId} at {@code eventId}.
     */
    public EditParticipantCommand(EventId eventId, PersonId personId, Set<ParticipationRole> roles, Set<Tag> tags) {
        requireAllNonNull(eventId, personId);
        this.eventId = eventId;
        if (roles == null && tags == null) {
            throw new IllegalArgumentException(MESSAGE_NO_CHANGES);
        }
        this.personId = personId;
        this.roles = roles == null ? null : Set.copyOf(roles);
        this.tags = tags == null ? null : Set.copyOf(tags);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        try {
            model.editParticipation(eventId, personId, roles, tags);
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        } catch (PersonNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_PERSON_NOT_FOUND, personId), e);
        } catch (ParticipationNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_PARTICIPANT_NOT_FOUND, eventId, personId), e);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, eventId, personId));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EditParticipantCommand otherCommand)) {
            return false;
        }
        return eventId.equals(otherCommand.eventId) && personId.equals(otherCommand.personId)
                && Objects.equals(roles, otherCommand.roles)
                && Objects.equals(tags, otherCommand.tags);
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
