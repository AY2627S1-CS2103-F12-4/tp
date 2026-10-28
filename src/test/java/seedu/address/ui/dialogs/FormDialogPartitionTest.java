package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.UiButtonTestSupport.assertError;
import static seedu.address.testutil.UiButtonTestSupport.click;
import static seedu.address.testutil.UiButtonTestSupport.fill;
import static seedu.address.testutil.UiButtonTestSupport.interact;
import static seedu.address.testutil.UiButtonTestSupport.isShowing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.UiButtonTestSupport.Fixture;
import seedu.address.testutil.UiButtonTestSupport.Snapshot;

/** EP/BVA tests exercise real form submissions, command parsing, model changes and persistence. */
public class FormDialogPartitionTest {
    @TempDir
    public Path temporaryFolder;

    @TestFactory
    public Stream<DynamicTest> requiredFields_oneInvalidInputAtATime() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Form form : Form.values()) {
            for (int index = 0; index < form.requiredLabels.size(); index++) {
                for (String blank : List.of("", " \t ")) {
                    int field = index;
                    tests.add(test(form + " required " + form.requiredLabels.get(field) + " " + printable(blank),
                            fixture -> reject(form, fixture, replace(form.values, field, blank),
                                    form.requiredLabels.get(field) + " is required.")));
                }
            }
        }
        return tests.stream();
    }

    @TestFactory
    public Stream<DynamicTest> invalidValues_equivalencePartitionsAndBoundaries() {
        List<InvalidInput> inputs = new ArrayList<>();
        for (Form form : List.of(Form.ADD_PERSON, Form.EDIT_PERSON)) {
            inputs.add(new InvalidInput(form, 0, "Alice!", Name.MESSAGE_CONSTRAINTS));
            for (String phone : List.of("12", "12a", "+123")) {
                inputs.add(new InvalidInput(form, 1, phone, Phone.MESSAGE_CONSTRAINTS));
            }
            for (String email : List.of("missing-at.example.com", "a@b.c", "a@@example.com")) {
                inputs.add(new InvalidInput(form, 2, email, Email.MESSAGE_CONSTRAINTS));
            }
            inputs.add(new InvalidInput(form, 4, "not-a-tag", Tag.MESSAGE_CONSTRAINTS));
        }
        for (Form form : List.of(Form.ADD_EVENT, Form.EDIT_EVENT)) {
            for (String capacity : List.of("-1", "0", "1.5", "abc", "2147483648")) {
                inputs.add(new InvalidInput(form, 1, capacity, AddEventCommand.MESSAGE_INVALID_CAPACITY));
            }
        }
        for (String id : List.of("p1", "E1", "P-1", "P1 P2", "P2 /tag VIP")) {
            inputs.add(new InvalidInput(Form.ADD_PARTICIPANT, 0, id, PersonId.MESSAGE_CONSTRAINTS));
        }
        inputs.add(new InvalidInput(Form.ADD_PARTICIPANT, 0, "P999",
                String.format(AddParticipantCommand.MESSAGE_PERSON_NOT_FOUND, "P999")));
        for (Form form : List.of(Form.ADD_PARTICIPANT, Form.EDIT_PARTICIPANT)) {
            int roleField = form == Form.ADD_PARTICIPANT ? 1 : 0;
            inputs.add(new InvalidInput(form, roleField, "R".repeat(51), ParticipationRole.MESSAGE_CONSTRAINTS));
            inputs.add(new InvalidInput(form, roleField, "Host!", ParticipationRole.MESSAGE_CONSTRAINTS));
            inputs.add(new InvalidInput(form, roleField + 1, "not-a-tag", Tag.MESSAGE_CONSTRAINTS));
        }
        return inputs.stream().map(input -> test(input.form + " field " + input.field + " " + printable(input.value),
                fixture -> reject(input.form, fixture, replace(input.form.values, input.field, input.value),
                        input.message)));
    }

    @TestFactory
    public Stream<DynamicTest> validForms_eachFormHasAPositiveCase() {
        return Arrays.stream(Form.values()).map(form -> test(form + " valid submission", fixture -> {
            submit(form, fixture, form.values);
            switch (form) {
                case ADD_PERSON:
                    assertEquals("Charlie Tan",
                            fixture.model.getAddressBook().getPersonList().getLast().getName().fullName);
                    assertEquals(3, fixture.model.getAddressBook().getPersonList().size());
                    break;
                case EDIT_PERSON:
                    assertEquals("Bob Updated",
                            fixture.model.getAddressBook().getPersonList().getLast().getName().fullName);
                    assertEquals(fixture.bob.getId(), fixture.model.getAddressBook().getPersonList().getLast().getId());
                    break;
                case ADD_EVENT:
                    assertEquals("New event", fixture.model.getEvents().getLast().getName());
                    assertEquals(3, fixture.model.getEvents().size());
                    break;
                case EDIT_EVENT:
                    assertEquals("Main edited", fixture.model.getEvents().getFirst().getName());
                    assertEquals(fixture.main.getId(), fixture.model.getEvents().getFirst().getId());
                    break;
                case ADD_PARTICIPANT:
                    assertEquals(2, fixture.model.getParticipationsForEvent(fixture.main).size());
                    assertFalse(fixture.model.getParticipationsForEvent(fixture.main).stream()
                        .filter(participation -> participation.getPerson().getId().equals(fixture.bob.getId()))
                        .findFirst().orElseThrow().isPresent());
                    break;
                case EDIT_PARTICIPANT:
                    assertTrue(fixture.model.getParticipationsForEvent(fixture.main).iterator().next().isPresent());
                    assertEquals(Set.of(new ParticipationRole("Speaker")),
                        fixture.model.getParticipationsForEvent(fixture.main).iterator().next().getRoles());
                    break;
                default:
                    throw new AssertionError("Untested form " + form);
            }
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> sameNames_preserveDistinctPersonIdentities() {
        return Stream.of(Form.ADD_PERSON, Form.EDIT_PERSON).map(form -> test(form + " same name", fixture -> {
            var registrations = fixture.model.getParticipations().getParticipations();
            submit(form, fixture, replace(form.values, 0, fixture.alice.getName().fullName));
            var actual = fixture.model.getAddressBook().getPersonList().getLast();
            assertEquals(fixture.alice.getName(), actual.getName());
            assertNotEquals(fixture.alice.getId(), actual.getId());
            assertEquals(fixture.alice, fixture.model.getAddressBook().getPersonList().getFirst());
            assertEquals(form == Form.ADD_PERSON ? 3 : 2, fixture.model.getAddressBook().getPersonList().size());
            assertEquals(registrations, fixture.model.getParticipations().getParticipations());
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> validBoundaries_eachValueAppearsInAPositiveCase() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Form form : List.of(Form.ADD_PERSON, Form.EDIT_PERSON)) {
            for (String name : List.of("C", "CD")) {
                tests.add(test(form + " name length " + name.length(), fixture -> {
                    submit(form, fixture, replace(form.values, 0, " " + name + " "));
                    assertEquals(name, fixture.model.getAddressBook().getPersonList().getLast().getName().fullName);
                    fixture.assertPersisted();
                }));
            }
            for (String phone : List.of("123", "1234")) {
                tests.add(test(form + " phone " + phone, fixture -> {
                    submit(form, fixture, replace(form.values, 1, " " + phone + " "));
                    assertEquals(phone, fixture.model.getAddressBook().getPersonList().getLast().getPhone().value);
                    fixture.assertPersisted();
                }));
            }
        }
        for (Form form : List.of(Form.ADD_EVENT, Form.EDIT_EVENT)) {
            for (int capacity : List.of(1, 2, Integer.MAX_VALUE)) {
                tests.add(test(form + " capacity " + capacity, fixture -> {
                    submit(form, fixture, replace(form.values, 1, " " + capacity + " "));
                    int index = form == Form.ADD_EVENT ? 2 : 0;
                    assertEquals(capacity, fixture.model.getEvents().get(index).getCapacity());
                    fixture.assertPersisted();
                }));
            }
        }
        for (Form form : List.of(Form.ADD_PARTICIPANT, Form.EDIT_PARTICIPANT)) {
            for (int length : List.of(1, 49, 50)) {
                tests.add(test(form + " role length " + length, fixture -> {
                    String role = "R".repeat(length);
                    int index = form == Form.ADD_PARTICIPANT ? 1 : 0;
                    submit(form, fixture, replace(form.values, index, role));
                    PersonId person = form == Form.ADD_PARTICIPANT ? fixture.bob.getId() : fixture.alice.getId();
                    var actual = fixture.model.getParticipationsForEvent(fixture.main).stream()
                            .filter(participation -> participation.getPerson().getId().equals(person))
                            .findFirst().orElseThrow();
                    assertEquals(Set.of(new ParticipationRole(role)), actual.getRoles());
                    fixture.assertPersisted();
                }));
            }
        }
        return tests.stream();
    }

    @TestFactory
    public Stream<DynamicTest> optionalRolesAndTags_allFourPresencePairs() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Form form : List.of(Form.ADD_PARTICIPANT, Form.EDIT_PARTICIPANT)) {
            for (boolean hasRole : List.of(false, true)) {
                for (boolean hasTag : List.of(false, true)) {
                    tests.add(test(form + " role=" + hasRole + " tag=" + hasTag, fixture -> {
                        List<String> values = new ArrayList<>(form.values);
                        int offset = form == Form.ADD_PARTICIPANT ? 1 : 0;
                        values.set(offset, hasRole ? " Speaker, Speaker, Host, " : " ");
                        values.set(offset + 1, hasTag ? " VIP, VIP, Guest, " : " ");
                        var unrelated = fixture.model.getParticipationsForEvent(fixture.other);
                        submit(form, fixture, values);
                        PersonId person = form == Form.ADD_PARTICIPANT ? fixture.bob.getId() : fixture.alice.getId();
                        var actual = fixture.model.getParticipationsForEvent(fixture.main).stream()
                                .filter(participation -> participation.getPerson().getId().equals(person))
                                .findFirst().orElseThrow();
                        assertEquals(hasRole ? Set.of(new ParticipationRole("Speaker"), new ParticipationRole("Host"))
                                : Set.of(), actual.getRoles());
                        assertEquals(hasTag ? Set.of(new Tag("VIP"), new Tag("Guest")) : Set.of(), actual.getTags());
                        assertEquals(form == Form.EDIT_PARTICIPANT, actual.isPresent());
                        assertEquals(unrelated, fixture.model.getParticipationsForEvent(fixture.other));
                        fixture.assertPersisted();
                    }));
                }
            }
        }
        return tests.stream();
    }

    @TestFactory
    public Stream<DynamicTest> optionalDescriptionsAndPersonTags_blankValuesHaveExplicitMeaning() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Form form : List.of(Form.ADD_EVENT, Form.EDIT_EVENT)) {
            for (String description : List.of("", " \t ")) {
                tests.add(test(form + " description " + printable(description), fixture -> {
                    submit(form, fixture, replace(form.values, 2, description));
                    int index = form == Form.ADD_EVENT ? 2 : 0;
                    assertEquals("", fixture.model.getEvents().get(index).getDescription());
                    fixture.assertPersisted();
                }));
            }
        }
        for (Form form : List.of(Form.ADD_PERSON, Form.EDIT_PERSON)) {
            for (String tags : List.of("", ", ,")) {
                tests.add(test(form + " tags " + printable(tags), fixture -> {
                    submit(form, fixture, replace(form.values, 4, tags));
                    assertTrue(fixture.model.getAddressBook().getPersonList().getLast().getTags().isEmpty());
                    fixture.assertPersisted();
                }));
            }
        }
        return tests.stream();
    }

    @TestFactory
    public Stream<DynamicTest> failedExecution_preservesInputsAndAllowsOneSuccessfulRetry() {
        return Arrays.stream(Form.values()).flatMap(form -> Stream.of(false, true).map(parseFailure ->
                test(form + " retry after " + (parseFailure ? "parse failure" : "command failure"), fixture -> {
                    Snapshot before = fixture.snapshot();
                    AtomicInteger attempts = new AtomicInteger();
                    AtomicReference<String> firstCommand = new AtomicReference<>();
                    CommandExecutor executor = command -> {
                        if (attempts.incrementAndGet() == 1) {
                            firstCommand.set(command);
                            if (parseFailure) {
                                throw new ParseException("Simulated parse failure");
                            }
                            throw new CommandException("Simulated command failure");
                        }
                        assertEquals(firstCommand.get(), command);
                        fixture.logic.execute(command);
                    };
                    interact(() -> form.open(fixture, executor), pane -> {
                        fill(pane, form.values);
                        click(pane, ButtonData.OK_DONE);
                        assertError(pane, parseFailure ? "Simulated parse failure" : "Simulated command failure");
                        assertEquals(form.values, seedu.address.testutil.UiButtonTestSupport.fields(pane).stream()
                                .map(javafx.scene.control.TextField::getText).toList());
                        fixture.assertUnchanged(before);
                        click(pane, ButtonData.OK_DONE);
                        assertFalse(isShowing(pane));
                    });
                    assertEquals(2, attempts.get());
                    assertNotEquals(before, fixture.snapshot());
                    fixture.assertPersisted();
                })));
    }

    @TestFactory
    public Stream<DynamicTest> cancelAndClose_eachFormLeavesMemoryAndFilesUnchanged() {
        return Arrays.stream(Form.values()).flatMap(form -> Stream.of(false, true).map(closeWindow ->
                test(form + (closeWindow ? " close window" : " cancel"), fixture -> {
                    Snapshot before = fixture.snapshot();
                    interact(() -> form.open(fixture), pane -> {
                        fill(pane, form.values);
                        if (closeWindow) {
                            Stage dialogStage = (Stage) pane.getScene().getWindow();
                            dialogStage.close();
                        } else {
                            click(pane, ButtonData.CANCEL_CLOSE);
                        }
                    });
                    fixture.assertUnchanged(before);
                })));
    }

    @TestFactory
    public Stream<DynamicTest> fieldOptions_cannotBecomeAnotherField() {
        return Stream.of(
                new InvalidInput(Form.ADD_PARTICIPANT, 1, "Host /role Speaker", ParticipationRole.MESSAGE_CONSTRAINTS),
                new InvalidInput(Form.ADD_PARTICIPANT, 2, "VIP /role Host", Tag.MESSAGE_CONSTRAINTS),
                new InvalidInput(Form.EDIT_PARTICIPANT, 0, "Host /tag VIP", ParticipationRole.MESSAGE_CONSTRAINTS),
                new InvalidInput(Form.EDIT_PARTICIPANT, 1, "VIP /role Host", Tag.MESSAGE_CONSTRAINTS),
                new InvalidInput(Form.ADD_PERSON, 4, "VIP t/Guest", Tag.MESSAGE_CONSTRAINTS),
                new InvalidInput(Form.EDIT_PERSON, 4, "VIP t/Guest", Tag.MESSAGE_CONSTRAINTS))
                .map(input -> test(input.form + " options in field " + input.field,
                        fixture -> reject(input.form, fixture, replace(input.form.values, input.field, input.value),
                                input.message)));
    }

    @Test
    public void addPerson_addressWithOption_preservesLiteralValueOrRejectsWithoutChanges() throws Exception {
        runOnFxThread(() -> {
            try (Fixture fixture = fixture()) {
                Snapshot before = fixture.snapshot();
                String address = "3 Main Street t/VIP";
                interact(() -> Form.ADD_PERSON.open(fixture), pane -> {
                    fill(pane, replace(Form.ADD_PERSON.values, 3, address));
                    click(pane, ButtonData.OK_DONE);
                    if (isShowing(pane)) {
                        assertError(pane, null);
                        fixture.assertUnchanged(before);
                    } else {
                        var actual = fixture.model.getAddressBook().getPersonList().getLast();
                        assertEquals(address, actual.getAddress().value);
                        assertEquals(Set.of(new Tag("friend")), actual.getTags());
                        fixture.assertPersisted();
                    }
                });
            }
        });
    }

    private DynamicTest test(String name, Scenario scenario) {
        return DynamicTest.dynamicTest(name, () -> runOnFxThread(() -> {
            try (Fixture fixture = fixture()) {
                scenario.run(fixture);
            }
        }));
    }

    private Fixture fixture() throws Exception {
        return new Fixture(Files.createTempDirectory(temporaryFolder, "form-"));
    }

    private static void reject(Form form, Fixture fixture, List<String> values, String message) throws Exception {
        Snapshot before = fixture.snapshot();
        interact(() -> form.open(fixture), pane -> {
            fill(pane, values);
            click(pane, ButtonData.OK_DONE);
            assertAll(() -> assertError(pane, message), () -> fixture.assertUnchanged(before));
        });
        fixture.assertUnchanged(before);
    }

    private static void submit(Form form, Fixture fixture, List<String> values) throws Exception {
        interact(() -> form.open(fixture), pane -> {
            fill(pane, values);
            click(pane, ButtonData.OK_DONE);
            assertFalse(isShowing(pane), "Valid form should close after submission");
        });
    }

    private static List<String> replace(List<String> values, int field, String replacement) {
        List<String> result = new ArrayList<>(values);
        result.set(field, replacement);
        return result;
    }

    private static String printable(String value) {
        return value.isBlank() ? (value.isEmpty() ? "empty" : "whitespace") : value;
    }

    private record InvalidInput(Form form, int field, String value, String message) {}

    @FunctionalInterface
    private interface Scenario {
        void run(Fixture fixture) throws Exception;
    }

    private enum Form {
        ADD_PERSON(List.of("Charlie Tan", "93456789", "charlie@example.com", "3 Main Street", "friend"),
                List.of("Name", "Phone", "Email", "Address")),
        EDIT_PERSON(List.of("Bob Updated", "92345678", "updated@example.com", "2 New Street", "colleague"),
                List.of("Name", "Phone", "Email", "Address")),
        ADD_EVENT(List.of("New event", "1", "New description"), List.of("Name", "Capacity")),
        EDIT_EVENT(List.of("Main edited", "2", "Changed description"), List.of("Name", "Capacity")),
        ADD_PARTICIPANT(List.of("P2", "Speaker", "Guest"), List.of("Person ID")),
        EDIT_PARTICIPANT(List.of("Speaker", "Guest"), List.of());

        private final List<String> values;
        private final List<String> requiredLabels;

        Form(List<String> values, List<String> requiredLabels) {
            this.values = values;
            this.requiredLabels = requiredLabels;
        }

        void open(Fixture fixture) {
            open(fixture, command -> fixture.logic.execute(command));
        }

        void open(Fixture fixture, CommandExecutor executor) {
            switch (this) {
                case ADD_PERSON:
                    new AddPersonDialog(fixture.owner).showAndWait(executor);
                    break;
                case EDIT_PERSON:
                    new EditPersonDialog(fixture.owner, fixture.bob, 2).showAndWait(executor);
                    break;
                case ADD_EVENT:
                    new AddEventDialog(fixture.owner).showAndWait(executor);
                    break;
                case EDIT_EVENT:
                    new EditEventDialog(fixture.owner, fixture.main).showAndWait(executor);
                    break;
                case ADD_PARTICIPANT:
                    new AddParticipantDialog(fixture.owner, fixture.main.getId(), fixture.main.getName())
                            .showAndWait(executor);
                    break;
                case EDIT_PARTICIPANT:
                    new EditParticipantDialog(fixture.owner,
                        fixture.model.getParticipationsForEvent(fixture.main).iterator().next()).showAndWait(executor);
                    break;
                default:
                    throw new AssertionError("Untested form " + this);
            }
        }
    }
}
