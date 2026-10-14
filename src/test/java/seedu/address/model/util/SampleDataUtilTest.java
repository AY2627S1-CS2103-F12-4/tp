package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_idsMatchGeneratedIdFormat() {
        List<String> personIds = List.of(SampleDataUtil.getSamplePersons()).stream()
                .map(Person::getId)
                .map(Object::toString)
                .toList();

        assertEquals(List.of("P1", "P2", "P3", "P4", "P5", "P6"), personIds);
    }
}
