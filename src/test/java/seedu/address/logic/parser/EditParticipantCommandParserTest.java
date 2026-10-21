package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;

public class EditParticipantCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final EventId eventId = new EventId("E1");
    private final PersonId personId = new PersonId("P1");

    @Test
    public void parse_repeatedFieldsAndWhitespace_replacesSets() throws Exception {
        assertEquals(new EditParticipantCommand(eventId, personId,
                Set.of(new ParticipationRole("Best Man"), new ParticipationRole("Speaker")),
                Set.of(new Tag("VIP"), new Tag("Guest"))),
                parser.parseCommand("edit-participant E1\tP1 /tag VIP /role Best Man /tag Guest /role Speaker"));
    }

    @Test
    public void parse_omittedAndEmptyFields_distinguishesPreserveAndClear() throws Exception {
        assertEquals(new EditParticipantCommand(eventId, personId, Set.of(), null),
                parser.parseCommand("edit-participant E1 P1 /role"));
        assertEquals(new EditParticipantCommand(eventId, personId, null, Set.of()),
                parser.parseCommand("edit-participant E1 P1 /tag"));
        assertEquals(new EditParticipantCommand(eventId, personId, Set.of(), Set.of()),
                parser.parseCommand("edit-participant E1 P1 /role /tag"));
        assertEquals(new EditParticipantCommand(eventId, personId,
                Set.of(new ParticipationRole("Speaker")), null),
                parser.parseCommand("edit-participant E1 P1 /role Speaker /role Speaker"));
    }

    @Test
    public void parse_missingOrInvalidArguments_rejected() {
        for (String args : new String[] {"", "E1", "E1 P1", "E1 P1 extra /tag VIP",
            "e1 P1 /tag VIP", "E1 p1 /role Guest", "E1 P1 /role @", "E1 P1 /tag !",
            "E1 P1 /role /role Guest", "E1 P1 /tag /tag VIP", "E1 P1 /role " + "A".repeat(51)}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-participant " + args), args);
        }
    }
}
