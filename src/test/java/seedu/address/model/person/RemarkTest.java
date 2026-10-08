package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_unconstrainedText_preservesValue() {
        assertEquals("", new Remark("").value);
        assertEquals(" \t\n", new Remark(" \t\n").value);
        assertEquals("Some remark.", new Remark("Some remark.").toString());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Some remark.");

        assertTrue(remark.equals(new Remark("Some remark.")));
        assertTrue(remark.equals(remark));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Some remark."));
        assertFalse(remark.equals(new Remark("Other remark.")));
        assertEquals(remark.hashCode(), new Remark("Some remark.").hashCode());
    }
}
