package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ParticipantUiTest {

    private static final Event EVENT =
            new Event(new EventId("E1"), "Wedding", 30, "Reception");

    private static final Person PERSON =
            new PersonBuilder()
                    .withId("P001")
                    .withName("John Doe")
                    .build();

    private static final Participation PARTICIPATION =
            new Participation(EVENT, PERSON);

    @Test
    public void panel_archiveStatus_showsSeparateLabelAndClearsItWhenContextChanges() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(new EventId("E1"), "Training <archived>", 10, "");
            ParticipantListPanel panel = new ParticipantListPanel(FXCollections.observableArrayList());
            Label status = (Label) panel.getRoot().lookup("#archivedStatus");
            assertFalse(status.isVisible());
            panel.showParticipantsForEvent(event.withArchived(true), Set.of());
            assertEquals("Training <archived> (E1)", labelText(panel, "eventHeading"));
            assertEquals("Archived", status.getText());
            assertTrue(status.isVisible());
            assertTrue(status.isManaged());
            panel.showParticipantsForEvent(event, Set.of());
            assertFalse(status.isVisible());
            assertFalse(status.isManaged());
            panel.setSearchContext(event.withArchived(true), "Guest");
            assertTrue(status.isVisible());
            panel.setSearchContext("Guest", true);
            assertFalse(status.isVisible());
            assertFalse(status.isManaged());
            panel.setSearchContext(event.withArchived(true), "Guest");
            panel.clearSearchContext();
            assertFalse(status.isVisible());
            assertFalse(status.isManaged());
        });
    }

    @Test
    public void card_nameAndStableId_displaysExactValues() throws Exception {
        runOnFxThread(() -> {
            ParticipantCard card = new ParticipantCard(PARTICIPATION);

            assertEquals("John Doe", labelText(card, "name"));
            assertEquals("Person ID: P001", labelText(card, "personId"));
            assertEquals("Roles: No role", labelText(card, "roles"));
        });
    }

    @Test
    public void card_multipleRoles_displaysSortedRoles() throws Exception {
        runOnFxThread(() -> {
            Participation participation = new Participation(
                    EVENT,
                    PERSON,
                    true,
                    Set.of(),
                    Set.of(
                            new ParticipationRole("Speaker"),
                            new ParticipationRole("Best Man")));

            assertEquals(
                    "Roles: Best Man, Speaker",
                    labelText(
                            new ParticipantCard(participation),
                            "roles"));
        });
    }

    @Test
    public void card_personAndEventTags_displaysDistinctStyledTags()
            throws Exception {
        runOnFxThread(() -> {
            Person taggedPerson = new PersonBuilder(PERSON)
                    .withTags("person", "shared")
                    .build();

            Participation participation = new Participation(
                    EVENT,
                    taggedPerson,
                    false,
                    Set.of(
                            new Tag("event"),
                            new Tag("shared")),
                    Set.of());

            ParticipantCard card = new ParticipantCard(participation);
            List<Label> labels = tagLabels(card);

            assertEquals(
                    List.of("person", "shared", "event"),
                    labels.stream()
                            .map(Label::getText)
                            .toList());

            assertTrue(
                    labels.get(0)
                            .getStyleClass()
                            .contains("person-tag"));

            assertTrue(
                    labels.get(1)
                            .getStyleClass()
                            .contains("person-tag"));

            assertTrue(
                    labels.get(2)
                            .getStyleClass()
                            .contains("event-tag"));
        });
    }

    @Test
    public void card_noTags_displaysEmptyTagPane() throws Exception {
        runOnFxThread(() -> {
            ParticipantCard card = new ParticipantCard(PARTICIPATION);

            assertTrue(tagLabels(card).isEmpty());
        });
    }

    @Test
    public void card_identicalNamesAndRoles_distinguishesStableIds()
            throws Exception {
        runOnFxThread(() -> {
            Person second = new PersonBuilder(PERSON)
                    .withId("P027")
                    .build();

            ParticipantCard firstCard =
                    new ParticipantCard(PARTICIPATION);

            ParticipantCard secondCard =
                    new ParticipantCard(
                            new Participation(EVENT, second));

            assertEquals(
                    labelText(firstCard, "name"),
                    labelText(secondCard, "name"));

            assertEquals(
                    "Person ID: P001",
                    labelText(firstCard, "personId"));

            assertEquals(
                    "Person ID: P027",
                    labelText(secondCard, "personId"));
        });
    }

    @Test
    public void constructors_nullArguments_rejectInput() throws Exception {
        runOnFxThread(() -> {
            assertThrows(NullPointerException.class, () ->
                    new ParticipantCard(null));

            assertThrows(NullPointerException.class, () ->
                    new ParticipantListPanel(null));
        });
    }

    @Test
    public void panel_initialState_showsSearchInstructionsAndEmptyPlaceholder()
            throws Exception {
        runOnFxThread(() -> {
            ParticipantListPanel panel =
                    new ParticipantListPanel(
                            FXCollections.observableArrayList());

            assertEquals(
                    "Participant search",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "search-participant NAME "
                            + "[/event EVENTID] [/include-archive]\n"
                            + "search-role ROLE "
                            + "[/event EVENTID] [/include-archive]",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "Select an event to view its participants.",
                    placeholderText(panel));

            assertTrue(listView(panel).getItems().isEmpty());
        });
    }

    @Test
    public void panel_searchContext_displaysEventIdAndOriginalPhrase()
            throws Exception {
        runOnFxThread(() -> {
            ParticipantListPanel panel =
                    new ParticipantListPanel(
                            FXCollections.observableArrayList());

            panel.setSearchContext(EVENT, "jOhN");

            assertEquals(
                    "Wedding (E1)",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "Matching \"jOhN\"",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "No matching participants.",
                    placeholderText(panel));

            assertThrows(NullPointerException.class, () ->
                    panel.setSearchContext(null, "John"));

            assertThrows(NullPointerException.class, () ->
                    panel.setSearchContext(EVENT, null));
        });
    }

    @Test
    public void panel_showParticipantsForEvent_sortsAndDisplaysParticipants()
            throws Exception {
        runOnFxThread(() -> {
            Person laterPerson = new PersonBuilder(PERSON)
                    .withId("P10")
                    .withName("Later Person")
                    .build();

            Person earlierPerson = new PersonBuilder(PERSON)
                    .withId("P2")
                    .withName("Earlier Person")
                    .build();

            Participation laterParticipation =
                    new Participation(EVENT, laterPerson);

            Participation earlierParticipation =
                    new Participation(EVENT, earlierPerson);

            ParticipantListPanel panel =
                    new ParticipantListPanel(
                            FXCollections.observableArrayList());

            panel.showParticipantsForEvent(
                    EVENT,
                    Set.of(laterParticipation, earlierParticipation));

            assertEquals(2, listView(panel).getItems().size());

            assertEquals(
                    earlierParticipation,
                    listView(panel).getItems().get(0));

            assertEquals(
                    laterParticipation,
                    listView(panel).getItems().get(1));

            assertEquals(
                    "Wedding (E1)",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "Participants",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "No participants found for this event.",
                    placeholderText(panel));
        });
    }

    @Test
    public void panel_participantButtons_runConfiguredActions()
            throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Participation> edited =
                    new AtomicReference<>();

            AtomicReference<Participation> deleted =
                    new AtomicReference<>();

            ParticipantListPanel panel = new ParticipantListPanel(
                    FXCollections.observableArrayList(),
                    edited::set,
                    deleted::set);

            panel.showParticipantsForEvent(
                    EVENT,
                    Set.of(PARTICIPATION));

            ListView<Participation> list = listView(panel);
            ListCell<Participation> cell =
                    list.getCellFactory().call(list);

            cell.updateListView(list);
            cell.updateIndex(0);

            Button editButton =
                    (Button) cell.getGraphic()
                            .lookup("#editButton");

            Button deleteButton =
                    (Button) cell.getGraphic()
                            .lookup("#deleteButton");

            editButton.fire();
            deleteButton.fire();

            assertSame(PARTICIPATION, edited.get());
            assertSame(PARTICIPATION, deleted.get());
        });
    }

    @Test
    public void panel_clearSearchContext_clearsParticipantsAndRestoresPrompt()
            throws Exception {
        runOnFxThread(() -> {
            ParticipantListPanel panel =
                    new ParticipantListPanel(
                            FXCollections.observableArrayList());

            panel.showParticipantsForEvent(
                    EVENT,
                    Set.of(PARTICIPATION));

            assertEquals(1, listView(panel).getItems().size());

            panel.clearSearchContext();

            assertTrue(listView(panel).getItems().isEmpty());

            assertEquals(
                    "Participants",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "Select an event to view its participants.",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "Select an event to view its participants.",
                    placeholderText(panel));
        });
    }

    @Test
    public void panel_searchAfterEventSelection_restoresSearchResults()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Participation> searchResults =
                    FXCollections.observableArrayList(PARTICIPATION);

            Person otherPerson = new PersonBuilder(PERSON)
                    .withId("P099")
                    .withName("Other Person")
                    .build();

            Participation otherParticipation =
                    new Participation(EVENT, otherPerson);

            ParticipantListPanel panel =
                    new ParticipantListPanel(searchResults);

            panel.showParticipantsForEvent(
                    EVENT,
                    Set.of(otherParticipation));

            assertEquals(
                    otherParticipation,
                    listView(panel).getItems().getFirst());

            panel.setSearchContext(EVENT, "John");

            assertSame(
                    searchResults,
                    listView(panel).getItems());

            assertEquals(
                    PARTICIPATION,
                    listView(panel).getItems().getFirst());
        });
    }

    @Test
    public void panel_globalSearchContext_displaysScopeAndPhrase()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Participation> results =
                    FXCollections.observableArrayList(PARTICIPATION);

            ParticipantListPanel panel =
                    new ParticipantListPanel(results);

            panel.showParticipantsForEvent(EVENT, Set.of());
            panel.setSearchContext("jOhN");

            assertSame(results, listView(panel).getItems());

            assertEquals(
                    PARTICIPATION,
                    listView(panel).getItems().getFirst());

            assertEquals(
                    "All nonarchived events",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "Matching \"jOhN\"",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "No matching participants.",
                    placeholderText(panel));

            assertThrows(NullPointerException.class, () ->
                    panel.setSearchContext((String) null));
        });
    }

    @Test
    public void panel_archiveSearchContext_updatesScopeAndPreservesResultsBinding()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Participation> results =
                    FXCollections.observableArrayList();

            ParticipantListPanel panel =
                    new ParticipantListPanel(results);

            panel.setSearchContext("jOhN", true);

            assertEquals(
                    "All events",
                    labelText(panel, "eventHeading"));

            assertEquals(
                    "Matching \"jOhN\"",
                    labelText(panel, "searchHeading"));

            assertEquals(
                    "No matching participants.",
                    placeholderText(panel));

            assertSame(results, listView(panel).getItems());

            results.add(PARTICIPATION);

            assertEquals(
                    PARTICIPATION,
                    listView(panel).getItems().getFirst());

            panel.setSearchContext("jOhN", false);

            assertEquals(
                    "All nonarchived events",
                    labelText(panel, "eventHeading"));

            panel.setSearchContext("jOhN", true);
            panel.setSearchContext("jOhN");

            assertEquals(
                    "All nonarchived events",
                    labelText(panel, "eventHeading"));

            assertSame(results, listView(panel).getItems());

            assertThrows(NullPointerException.class, () ->
                    panel.setSearchContext(null, true));
        });
    }

    @Test
    public void panel_observableResults_addReplaceAndClearUpdateList()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Participation> results =
                    FXCollections.observableArrayList();

            ParticipantListPanel panel =
                    new ParticipantListPanel(results);

            results.add(PARTICIPATION);

            assertEquals(
                    PARTICIPATION,
                    listView(panel).getItems().getFirst());

            Participation changed = PARTICIPATION.withRoles(
                    Set.of(new ParticipationRole("Guest")));

            results.set(0, changed);

            assertEquals(
                    changed,
                    listView(panel).getItems().getFirst());

            results.clear();

            assertTrue(listView(panel).getItems().isEmpty());
        });
    }

    @Test
    public void panel_recycledCell_clearsOldGraphicForEmptyAndNullItems()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Participation> results =
                    FXCollections.observableArrayList(PARTICIPATION);

            ParticipantListPanel panel =
                    new ParticipantListPanel(results);

            ListView<Participation> list = listView(panel);
            ListCell<Participation> cell =
                    list.getCellFactory().call(list);

            cell.updateListView(list);
            cell.updateIndex(0);

            assertNotNull(cell.getGraphic());
            assertNull(cell.getText());

            cell.updateIndex(1);
            assertNull(cell.getGraphic());

            results.set(0, null);
            cell.updateIndex(0);

            assertNull(cell.getGraphic());
        });
    }

    private String labelText(UiPart<?> part, String id) {
        Label label =
                (Label) ((javafx.scene.Node) part.getRoot())
                        .lookup("#" + id);

        return label.getText();
    }

    private String placeholderText(ParticipantListPanel panel) {
        Label placeholder =
                (Label) listView(panel).getPlaceholder();

        return placeholder.getText();
    }

    private List<Label> tagLabels(ParticipantCard card) {
        FlowPane tags = (FlowPane) card.getRoot()
                .lookup("#tags");

        return tags.getChildren().stream()
                .map(node -> (Label) node)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private ListView<Participation> listView(
            ParticipantListPanel panel) {
        return (ListView<Participation>) panel.getRoot()
                .lookup("#participantListView");
    }
}
