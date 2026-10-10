package seedu.address.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ParticipationTest {
    private static final Event EVENT = new Event(new EventId("E1"), "Workshop", 20, "Description");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();
    private static final Tag TAG = new Tag("vip");
    private static final ParticipationRole ROLE = new ParticipationRole("Speaker");

    @Test
    public void constructor_defaultState_storesMappingWithEmptyState() {
        Participation participation = new Participation(EVENT, PERSON);

        assertEquals(EVENT, participation.getEvent());
        assertEquals(PERSON, participation.getPerson());
        assertFalse(participation.isPresent());
        assertEquals(Set.of(), participation.getTags());
        assertEquals(Set.of(), participation.getRoles());
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Participation(null, PERSON));
        assertThrows(NullPointerException.class, () -> new Participation(EVENT, null));
        assertThrows(NullPointerException.class, () -> new Participation(
                EVENT, PERSON, true, null, Set.of()));
        assertThrows(NullPointerException.class, () -> new Participation(
                EVENT, PERSON, true, Set.of(), null));
    }

    @Test
    public void constructor_mutableSets_makesDefensiveCopies() {
        Set<Tag> tags = new HashSet<>(Set.of(TAG));
        Set<ParticipationRole> roles = new HashSet<>(Set.of(ROLE));
        Participation participation = new Participation(EVENT, PERSON, true, tags, roles);

        tags.clear();
        roles.clear();

        assertEquals(Set.of(TAG), participation.getTags());
        assertEquals(Set.of(ROLE), participation.getRoles());
        assertThrows(UnsupportedOperationException.class, () -> participation.getTags().clear());
        assertThrows(UnsupportedOperationException.class, () -> participation.getRoles().clear());
    }

    @Test
    public void withMethods_updatedCopy_preservesOtherFields() {
        Participation original = new Participation(EVENT, PERSON, false, Set.of(), Set.of());
        Participation present = original.withPresent(true);
        Participation tagged = present.withTags(Set.of(TAG));
        Participation assigned = tagged.withRoles(Set.of(ROLE));

        assertNotSame(original, present);
        assertTrue(present.isPresent());
        assertEquals(Set.of(), present.getTags());
        assertEquals(Set.of(TAG), tagged.getTags());
        assertEquals(Set.of(), tagged.getRoles());
        assertEquals(Set.of(ROLE), assigned.getRoles());
        assertEquals(EVENT, assigned.getEvent());
        assertEquals(PERSON, assigned.getPerson());
    }

    @Test
    public void isSameMapping_sameIds_returnsTrue() {
        Event renamedEvent = new Event(new EventId("E1"), "Renamed", 30, "New description");
        Person renamedPerson = new PersonBuilder(PERSON).withName("Renamed Person").build();
        Participation sameMapping = new Participation(renamedEvent, renamedPerson, true,
                Set.of(TAG), Set.of(ROLE));

        Participation participation = new Participation(EVENT, PERSON);
        assertTrue(participation.isSameMapping(participation));
        assertTrue(participation.isSameMapping(sameMapping));
        assertFalse(participation.isSameMapping(null));
    }

    @Test
    public void isSameMapping_differentId_returnsFalse() {
        Event otherEvent = new Event(new EventId("E2"), "Workshop", 20, "Description");
        Person otherPerson = new PersonBuilder().withId("P2").build();

        Participation participation = new Participation(EVENT, PERSON);
        assertFalse(participation.isSameMapping(new Participation(otherEvent, PERSON)));
        assertFalse(participation.isSameMapping(new Participation(EVENT, otherPerson)));
    }

    @Test
    public void equals_sameMappingAndState_returnsTrue() {
        Participation participation = new Participation(EVENT, PERSON, true, Set.of(TAG), Set.of(ROLE));
        Participation copy = new Participation(EVENT, PERSON, true, Set.of(TAG), Set.of(ROLE));

        assertEquals(participation, participation);
        assertEquals(participation, copy);
        assertEquals(participation.hashCode(), copy.hashCode());
        assertNotEquals(participation, participation.withPresent(false));
        assertNotEquals(participation, participation.withTags(Set.of()));
        assertNotEquals(participation, participation.withRoles(Set.of()));
        assertNotEquals(participation, null);
        assertNotEquals(participation, "participation");
    }
}
