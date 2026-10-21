package seedu.address.logic;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;

/**
 * API of the Logic component.
 */
public interface Logic {

    /**
     * Executes the command and returns the result.
     *
     * @param commandText The command entered by the user.
     * @return the result of the command execution
     * @throws CommandException if an error occurs during command execution
     * @throws ParseException if an error occurs during parsing
     */
    CommandResult execute(String commandText)
            throws CommandException, ParseException;

    /**
     * Returns an unmodifiable view of the filtered list of persons.
     */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns a live, unmodifiable view of the filtered events.
     */
    ObservableList<Event> getFilteredEventList();

    /**
     * Returns a live, unmodifiable list of the current participant search
     * results.
     */
    ObservableList<Participation> getParticipantSearchResults();

    /**
     * Returns an immutable snapshot of matching registrations for an event's
     * stable ID.
     */
    List<Participation> getParticipantSearchResultsForEvent(Event event);

    /**
     * Returns the scoped search event, or empty during a global search or when
     * no search is active.
     */
    Optional<Event> getParticipantSearchEvent();

    /**
     * Returns the normalized search phrase, or empty when no search is active.
     */
    Optional<String> getParticipantSearchKeyword();

    /**
     * Returns the currently selected event, or empty when no event is selected.
     */
    Optional<Event> getSelectedEvent();

    /**
     * Returns all participants registered for the given event.
     */
    Set<Participation> getParticipationsForEvent(Event event);

    /**
     * Returns the user preferences' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user preferences' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
