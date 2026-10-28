package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PersonIdTest {
    @Test
    public void compareTo_numericOrder_handlesLargeIdsAndLeadingZeros() {
        List<String> ids = List.of("P999999999999999999999999", "P10", "P2", "P1", "P02", "P002", "P0");
        assertEquals(List.of("P0", "P1", "P002", "P02", "P2", "P10", "P999999999999999999999999"),
                ids.stream().map(PersonId::new).sorted().map(PersonId::toString).toList());
        assertEquals(0, new PersonId("P2").compareTo(new PersonId("P2")));
        assertTrue(new PersonId("P2").compareTo(new PersonId("P10")) < 0);
        assertTrue(new PersonId("P10").compareTo(new PersonId("P2")) > 0);
        assertThrows(NullPointerException.class, () -> new PersonId("P2").compareTo(null));
    }
}
