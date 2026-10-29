package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonId;

public class AddParticipantDialogTest {

    @Test
    public void buildCommand_personId_formatsExistingCliCommand() throws ParseException {
        assertEquals("add-participant E1 P1", AddParticipantDialog.buildCommand("E1", " P1 ", "", ""));
    }

    @Test
    public void buildCommand_rolesAndTags_formatsRepeatedCliArguments() throws ParseException {
        assertEquals("add-participant E1 P1 /role Facilitator /role Guest Speaker /tag VIP /tag Alumni",
                AddParticipantDialog.buildCommand(
                        "E1", " P1 ", " Facilitator, Guest Speaker ", " VIP, Alumni "));
    }

    @Test
    public void buildCommand_invalidPersonId_rejectsEntireFieldValue() {
        for (String invalidId : List.of("", " ", "P1 /role Host", "P1 /tag VIP", "P1 P2", "p1", "E1")) {
            assertThrows(ParseException.class, PersonId.MESSAGE_CONSTRAINTS, () ->
                    AddParticipantDialog.buildCommand("E1", invalidId, "", ""));
        }
    }

    @Test
    public void submit_personIdWithRoleOption_showsErrorAndAllowsCorrection() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            AtomicReference<String> submittedCommand = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddParticipantDialog dialog = new AddParticipantDialog(owner, "E1", "Workshop");
            Platform.runLater(() -> {
                DialogPane pane = showingDialogPane();
                try {
                    GridPane form = (GridPane) pane.lookup(".dialog-form");
                    TextField personId = form.getChildren().stream().filter(TextField.class::isInstance)
                            .map(TextField.class::cast).findFirst().orElseThrow();
                    Button addButton = (Button) pane.lookupButton(pane.getButtonTypes().getFirst());
                    personId.setText("P1 /role Host");
                    addButton.fire();

                    assertNull(submittedCommand.get());
                    assertTrue(pane.getScene().getWindow().isShowing());
                    Label error = (Label) pane.lookup(".validation-error-label");
                    assertTrue(error.isVisible());
                    assertEquals(PersonId.MESSAGE_CONSTRAINTS, error.getText());

                    personId.setText(" P1 ");
                    addButton.fire();
                    assertEquals("add-participant E1 P1", submittedCommand.get());
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    pane.getScene().getWindow().hide();
                }
            });

            dialog.showAndWait(submittedCommand::set);
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    @Test
    public void form_containsPersonIdRolesAndTagsFields() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddParticipantDialog dialog = new AddParticipantDialog(owner, "E1", "Workshop");
            Platform.runLater(() -> checkForm(failure));

            dialog.showAndWait(command -> { });

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    private void checkForm(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        try {
            GridPane form = (GridPane) dialogPane.lookup(".dialog-form");
            List<Label> labels = form.getChildren().stream()
                    .filter(Label.class::isInstance)
                    .map(Label.class::cast)
                    .toList();
            long textFieldCount = form.getChildren().stream().filter(TextField.class::isInstance).count();

            assertEquals(List.of("Person ID *", "Roles (comma-separated)", "Tags (comma-separated)"),
                    labels.stream().map(Label::getText).toList());
            assertEquals(3, textFieldCount);
            assertEquals("Add participant to Workshop.", dialogPane.getHeaderText());
            assertTrue(dialogPane.getStylesheets().stream().anyMatch(path -> path.endsWith("DarkTheme.css")));
        } catch (Throwable throwable) {
            failure.set(throwable);
        } finally {
            dialogPane.getScene().getWindow().hide();
        }
    }

    private DialogPane showingDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .map(Scene::getRoot)
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow();
    }
}
