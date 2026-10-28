package seedu.address.ui.dialogs;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Arrays;
import java.util.List;

import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;

/** Validates complete field values before dialogs turn them into CLI arguments. */
final class DialogInputValidator {
    static final String MESSAGE_ADDRESS_OPTIONS =
            "Address must not contain command prefixes (n/, p/, e/, a/, t/) preceded by a space.";

    private DialogInputValidator() {}

    /** Validates person fields independently so an embedded option cannot change another field. */
    static void validatePerson(String name, String phone, String email, String address, String tags)
            throws ParseException {
        ParserUtil.parseName(name);
        ParserUtil.parsePhone(phone);
        ParserUtil.parseEmail(email);
        String validatedAddress = ParserUtil.parseAddress(address).value;
        // Addresses allow arbitrary text, including prefixes that the CLI would interpret as extra arguments.
        String preamble = ArgumentTokenizer.tokenize(validatedAddress,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG).getPreamble();
        if (!preamble.equals(validatedAddress)) {
            throw new ParseException(MESSAGE_ADDRESS_OPTIONS);
        }
        ParserUtil.parseTags(splitValues(tags));
    }

    /** Validates each comma-separated role and tag before any CLI prefixes are added. */
    static void validateParticipation(String roles, String tags) throws ParseException {
        ParserUtil.parseParticipationRoles(splitValues(roles));
        ParserUtil.parseTags(splitValues(tags));
    }

    private static List<String> splitValues(String values) {
        return Arrays.stream(values.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList();
    }
}
