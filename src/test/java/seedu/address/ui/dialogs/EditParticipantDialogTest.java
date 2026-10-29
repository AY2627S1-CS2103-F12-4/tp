package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class EditParticipantDialogTest {

    @Test
    public void buildCommand_rolesAndTags_formatsRepeatedCliArguments() throws ParseException {
        assertEquals("edit-participant E1 P1 /role Facilitator /role Guest Speaker /tag VIP /tag Alumni",
                EditParticipantDialog.buildCommand(
                        "E1", "P1", " Facilitator, Guest Speaker ", " VIP, Alumni "));
    }

    @Test
    public void buildCommand_blankRolesAndTags_includesEmptyPrefixes() throws ParseException {
        assertEquals("edit-participant E1 P1 /role /tag",
                EditParticipantDialog.buildCommand("E1", "P1", " ", " "));
    }

    @Test
    public void submit_existingTag_usesRawTagName() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            Participation participation = new Participation(event,
                    new PersonBuilder().withId("P001").build(), false,
                    Set.of(new Tag("VIP")), Set.of(new ParticipationRole("Speaker")));
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            EditParticipantDialog dialog = new EditParticipantDialog(owner, participation);
            AtomicReference<String> submittedCommand = new AtomicReference<>();
            Platform.runLater(this::clickEditButton);

            dialog.showAndWait(submittedCommand::set);

            assertEquals("edit-participant E1 P001 /role Speaker /tag VIP", submittedCommand.get());
        });
    }

    private void clickEditButton() {
        DialogPane dialogPane = Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .map(Scene::getRoot)
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow();
        Button editButton = (Button) dialogPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType.getText().equals("Edit"))
                .map(dialogPane::lookupButton)
                .findFirst()
                .orElseThrow();
        editButton.fire();
    }
}
