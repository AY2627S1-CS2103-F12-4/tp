package seedu.address.ui.dialogs;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/** Executes a command assembled by a dialog. */
@FunctionalInterface
public interface CommandExecutor {
    /** Executes the supplied command text. */
    void execute(String command) throws CommandException, ParseException;
}
