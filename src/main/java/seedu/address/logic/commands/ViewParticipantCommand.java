
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;

/**
 * Displays a participant's details for a specified event.
 */
public class ViewParticipantCommand extends Command {

    public static final String COMMAND_WORD = "view-participant";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views a participant's details in an event.\n"
            + "Parameters: EVENT_ID PERSON_ID\n"
            + "Example: " + COMMAND_WORD + " E1 P2";

    public static final String MESSAGE_EVENT_NOT_FOUND =
            "Event %s does not exist.";

    public static final String MESSAGE_PARTICIPANT_NOT_FOUND =
            "Person %2$s is not a participant of event %1$s.";

    private final EventId eventId;
    private final PersonId personId;

    /**
     * Creates a ViewParticipantCommand to display the details of a
     * person participating in the specified event.
     *
     * @param eventId ID of the event
     * @param personId ID of the person
     */
    public ViewParticipantCommand(EventId eventId, PersonId personId) {
        requireAllNonNull(eventId, personId);
        this.eventId = eventId;
        this.personId = personId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Event event = model.getEvents().stream()
                .filter(e -> e.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_EVENT_NOT_FOUND, eventId)));

        Participation participation;

        try {
            participation = model.getParticipationsForEvent(event).stream()
                    .filter(p -> p.getPerson().getId().equals(personId))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(
                            String.format(MESSAGE_PARTICIPANT_NOT_FOUND,
                                    eventId, personId)));
        } catch (EventNotFoundException e) {
            throw new CommandException(
                    String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        }

        Person person = participation.getPerson();

        String roles = participation.getRoles().isEmpty()
                ? "No role"
                : participation.getRoles().stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        String details = "Participant Details\n"
                + "Event: " + event.getName() + "\n"
                + "Person ID: " + person.getId() + "\n"
                + "Name: " + person.getName() + "\n"
                + "Phone: " + person.getPhone() + "\n"
                + "Email: " + person.getEmail() + "\n"
                + "Address: " + person.getAddress() + "\n"
                + "Roles: " + roles + "\n"
                + "Present: " + (participation.isPresent() ? "Yes" : "No");

        return new CommandResult(details);
    }
}
