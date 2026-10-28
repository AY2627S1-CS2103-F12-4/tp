package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Updates an event identified by its ID in the model. */
public class EditEventCommand extends Command {
    public static final String COMMAND_WORD = "edit-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates an event identified by its ID.\n"
            + "Parameters: EVENT_ID [EVENT_NAME] [/cap CAPACITY] [/desc DESCRIPTION]\n"
            + "Example: edit-event E1 Orientation /cap 100 /desc Welcome session";
    public static final String MESSAGE_SUCCESS = "Event updated: %1$s (%2$s)";
    public static final String MESSAGE_INVALID_CAPACITY = "Event capacity must be a positive integer";

    public static final String MESSAGE_NOT_FOUND = "Event %s does not exist.";
    public static final String MESSAGE_NO_CHANGES = "At least one event field must be provided.";

    private static final Pattern LIKELY_PREFIX = Pattern.compile("(?<!\\S)/[A-Za-z][A-Za-z0-9-]*(?=\\s|$)");

    private final EventId eventId;
    private final String name;
    private final Integer capacity;
    private final String description;

    /** Null fields are omitted; an empty description explicitly clears the description. */
    public EditEventCommand(EventId eventId, String name, Integer capacity, String description) {
        this.eventId = requireNonNull(eventId);
        checkArgument(name != null || capacity != null || description != null, MESSAGE_NO_CHANGES);
        new Event(eventId, name == null ? "Placeholder" : name,
                capacity == null ? 1 : capacity, description == null ? "" : description);
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    /** Returns a preview when free-text fields contain standalone tokens resembling unsupported prefixes. */
    public Optional<String> getConfirmationMessage() {
        boolean suspiciousName = name != null && LIKELY_PREFIX.matcher(name).find();
        boolean suspiciousDescription = description != null && LIKELY_PREFIX.matcher(description).find();
        if (!suspiciousName && !suspiciousDescription) {
            return Optional.empty();
        }
        String preview = "The name or description contains text resembling an unsupported field prefix."
                + " Only /cap and /desc are supported.\n\nEvent ID: " + eventId;
        if (name != null) {
            preview += "\nNew name: " + name;
        }
        if (capacity != null) {
            preview += "\nNew capacity: " + capacity;
        }
        if (description != null) {
            preview += "\nNew description: " + description;
        }
        return Optional.of(preview + "\n\nContinue and save this text literally?");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event current = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND, eventId)));
        Event event = new Event(eventId, name == null ? current.getName() : name,
                capacity == null ? current.getCapacity() : capacity,
                description == null ? current.getDescription() : description, current.isArchived());
        model.updateEvent(event);
        return new CommandResult(String.format(MESSAGE_SUCCESS, event.getName(), event.getId()), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditEventCommand command
                && eventId.equals(command.eventId)
                && Objects.equals(name, command.name)
                && Objects.equals(capacity, command.capacity)
                && Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, name, capacity, description);
    }
}
