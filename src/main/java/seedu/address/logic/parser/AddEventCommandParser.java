package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CAPACITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses event details for the add-event command. */
public class AddEventCommandParser implements Parser<AddEventCommand> {
    @Override
    public AddEventCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithSeparatedPrefixes(
                args, PREFIX_CAPACITY, PREFIX_DESCRIPTION);
        if (arguments.getPreamble().isBlank()
                || arguments.getValue(PREFIX_CAPACITY).isEmpty()
                || arguments.getValue(PREFIX_DESCRIPTION).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
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
            throw new ParseException(AddEventCommand.MESSAGE_INVALID_CAPACITY);
        }
        return new AddEventCommand(arguments.getPreamble(), capacity,
                arguments.getValue(PREFIX_DESCRIPTION).get());
    }
}
