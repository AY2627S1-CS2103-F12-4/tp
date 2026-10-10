package seedu.address.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ParticipationRoleTest {

    @Test
    public void constructor_validRole_normalizesWhitespace() {
        ParticipationRole role = new ParticipationRole("  Lead   Facilitator  ");

        assertEquals("Lead Facilitator", role.value);
        assertEquals("Lead Facilitator", role.toString());
    }

    @Test
    public void constructor_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ParticipationRole(""));
        assertThrows(IllegalArgumentException.class, () -> new ParticipationRole("   "));
        assertThrows(IllegalArgumentException.class, () -> new ParticipationRole("usher!"));
        assertThrows(IllegalArgumentException.class, () -> new ParticipationRole("a".repeat(51)));
    }

    @Test
    public void constructor_nullRole_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ParticipationRole(null));
    }

    @Test
    public void isValidRole_validAndInvalidRoles_returnsExpectedResult() {
        assertTrue(ParticipationRole.isValidRole("Stage manager (night-shift)"));
        assertTrue(ParticipationRole.isValidRole("Assistant's role 2"));
        assertFalse(ParticipationRole.isValidRole(null));
        assertFalse(ParticipationRole.isValidRole("\t\n"));
        assertFalse(ParticipationRole.isValidRole("role_name"));
        assertFalse(ParticipationRole.isValidRole("a".repeat(51)));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        ParticipationRole role = new ParticipationRole("Usher");
        ParticipationRole sameRole = new ParticipationRole(" Usher ");

        assertEquals(role, role);
        assertEquals(role, sameRole);
        assertEquals(role.hashCode(), sameRole.hashCode());
        assertNotEquals(role, new ParticipationRole("Speaker"));
        assertNotEquals(role, null);
        assertNotEquals(role, "Usher");
    }
}
