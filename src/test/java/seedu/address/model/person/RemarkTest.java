package seedu.address.model.person;

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
    public void isValidRemark() {
        assertThrows(NullPointerException.class, () -> Remark.isValidRemark(null));

        assertTrue(Remark.isValidRemark(""));
        assertTrue(Remark.isValidRemark("Likes to swim."));
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes to swim.");

        assertTrue(remark.equals(new Remark("Likes to swim.")));
        assertTrue(remark.equals(remark));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Likes to swim."));
        assertFalse(remark.equals(new Remark("Different remark")));
    }
}
