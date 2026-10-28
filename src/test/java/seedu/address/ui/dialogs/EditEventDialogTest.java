package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

public class EditEventDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() {
        assertEquals("edit-event E1 Advanced Workshop /cap 40 /desc Advanced Java",
                EditEventDialog.buildCommand("E1", " Advanced Workshop ", " 40 ", " Advanced Java "));
    }

    @Test
    public void buildCommand_blankDescription_includesEmptyDescriptionPrefix() {
        assertEquals("edit-event E1 Workshop /cap 20 /desc ",
                EditEventDialog.buildCommand("E1", "Workshop", "20", " "));
    }

    @Test
    public void submit_blankRequiredFields_showsErrorAndAllowsCorrection() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            AtomicReference<String> submittedCommand = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            Event event = new Event(new EventId("E1"), "Main", 10, "Original");
            EditEventDialog dialog = new EditEventDialog(owner, event);
            Platform.runLater(() -> {
                DialogPane pane = Window.getWindows().stream().filter(Window::isShowing)
                        .map(Window::getScene).map(Scene::getRoot).filter(DialogPane.class::isInstance)
                        .map(DialogPane.class::cast).findFirst().orElseThrow();
                try {
                    GridPane form = (GridPane) pane.lookup(".dialog-form");
                    List<TextField> fields = form.getChildren().stream().filter(TextField.class::isInstance)
                            .map(TextField.class::cast).toList();
                    Button editButton = (Button) pane.lookupButton(pane.getButtonTypes().getFirst());
                    fields.get(1).setText("20");
                    for (String blankName : List.of("", " \t ")) {
                        fields.getFirst().setText(blankName);
                        editButton.fire();

                        assertNull(submittedCommand.get());
                        assertTrue(pane.getScene().getWindow().isShowing());
                        Label error = (Label) pane.lookup(".validation-error-label");
                        assertTrue(error.isVisible());
                        assertEquals("Name is required.", error.getText());
                    }

                    fields.getFirst().setText("Main");
                    fields.get(1).clear();
                    editButton.fire();
                    assertNull(submittedCommand.get());
                    Label error = (Label) pane.lookup(".validation-error-label");
                    assertEquals("Capacity is required.", error.getText());

                    fields.get(1).setText("20");
                    editButton.fire();
                    assertEquals("edit-event E1 Main /cap 20 /desc Original", submittedCommand.get());
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
}
