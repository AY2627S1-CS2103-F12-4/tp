package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;

public class AddParticipantCommandParserTest {

    private final AddParticipantCommandParser parser = new AddParticipantCommandParser();

    @Test
    public void parse_validArguments_returnsAddParticipantCommand() {
        assertParseSuccess(parser, " E1   P1 /role Facilitator /role Guest Speaker /tag VIP /tag Alumni ",
                new AddParticipantCommand(new EventId("E1"), new PersonId("P1"),
                        Set.of(new ParticipationRole("Facilitator"), new ParticipationRole("Guest Speaker")),
                        Set.of(new Tag("VIP"), new Tag("Alumni"))));
    }

    @Test
    public void parse_noRolesOrTags_returnsAddParticipantCommandWithEmptySets() {
        assertParseSuccess(parser, " E1   P1 ",
                new AddParticipantCommand(new EventId("E1"), new PersonId("P1"), Set.of(), Set.of()));
    }

    @Test
    public void parse_wrongArgumentCount_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddParticipantCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "E1", expectedMessage);
        assertParseFailure(parser, "E1 P1 extra", expectedMessage);
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        assertParseFailure(parser, "1 P1", EventId.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidPersonId_throwsParseException() {
        assertParseFailure(parser, "E1 1", PersonId.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        assertParseFailure(parser, "E1 P1 /role !", ParticipationRole.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, "E1 P1 /tag invalid-tag", Tag.MESSAGE_CONSTRAINTS);
    }
}
