package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CAPACITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;

import seedu.address.logic.commands.EditEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses event details for the edit-event command. */
public class EditEventCommandParser implements Parser<EditEventCommand> {
    @Override
    public EditEventCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CAPACITY, PREFIX_DESCRIPTION);
        if (arguments.getPreamble().isBlank()
                || arguments.getValue(PREFIX_CAPACITY).isEmpty()
                || arguments.getValue(PREFIX_DESCRIPTION).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditEventCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CAPACITY, PREFIX_DESCRIPTION);
        String capacityText = arguments.getValue(PREFIX_CAPACITY).get();
        int capacity;
        try {
            if (!capacityText.matches("[0-9]+")) {
                throw new NumberFormatException();
            }
            capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new ParseException(EditEventCommand.MESSAGE_INVALID_CAPACITY);
        }
        return new EditEventCommand(arguments.getPreamble(), capacity,
                arguments.getValue(PREFIX_DESCRIPTION).get());
    }
}
