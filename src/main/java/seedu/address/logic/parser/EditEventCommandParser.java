package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CAPACITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;

import seedu.address.logic.commands.EditEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/** Parses event details for the edit-event command. */
public class EditEventCommandParser implements Parser<EditEventCommand> {
    @Override
    public EditEventCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithSeparatedPrefixes(
                args, PREFIX_CAPACITY, PREFIX_DESCRIPTION);
        if (arguments.getPreamble().isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditEventCommand.MESSAGE_USAGE));
        }
        String[] preamble = arguments.getPreamble().split("\\s+", 2);
        EventId eventId = ParserUtil.parseEventId(preamble[0]);
        String name = preamble.length == 2 ? preamble[1].strip() : null;
        if (name != null && name.codePoints().allMatch(codePoint ->
                Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint))) {
            name = null;
        }
        if (name == null && arguments.getValue(PREFIX_CAPACITY).isEmpty()
                && arguments.getValue(PREFIX_DESCRIPTION).isEmpty()) {
            throw new ParseException(EditEventCommand.MESSAGE_NO_CHANGES);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CAPACITY, PREFIX_DESCRIPTION);
        Integer capacity = null;
        if (arguments.getValue(PREFIX_CAPACITY).isPresent()) {
            String capacityText = arguments.getValue(PREFIX_CAPACITY).get();
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
        }
        return new EditEventCommand(eventId, name, capacity,
                arguments.getValue(PREFIX_DESCRIPTION).orElse(null));
    }
}
