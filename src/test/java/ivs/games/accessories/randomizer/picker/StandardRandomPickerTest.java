package ivs.games.accessories.randomizer.picker;

import ivs.games.accessories.randomizer.Randomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardRandomPickerTest {

    /** Verifies that each valid index selects the corresponding element. */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void pickReturnsElementAtGeneratedIndex(int index) {
        var values = List.of("A", "B", "C");
        var picker = StandardRandomPicker.of(values, () -> new int[]{index});

        assertEquals(values.get(index), picker.pick());
    }

    /** Verifies that the selected object is returned by reference without copying. */
    @Test
    void pickReturnsOriginalElementReference() {
        var value = new Object();
        var picker = StandardRandomPicker.of(List.of(value), () -> new int[]{0});

        assertSame(value, picker.pick());
    }

    /** Verifies that selection does not remove elements and allows consecutive repeated selections. */
    @Test
    void pickDoesNotRemoveSelectedElement() {
        var picker = StandardRandomPicker.of(List.of("A", "B"), () -> new int[]{1});

        assertEquals("B", picker.pick());
        assertEquals("B", picker.pick());
    }

    /** Verifies that repeated elements retain their individual positions in the list. */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void supportsRepeatedElements(int index) {
        var repeated = new Object();
        var other = new Object();
        var values = List.of(repeated, repeated, other);
        var picker = StandardRandomPicker.of(values, () -> new int[]{index});

        assertSame(values.get(index), picker.pick());
    }

    /** Verifies that changing the source list does not affect the stored size, order, or references. */
    @Test
    void ofCopiesSourceList() {
        var values = new ArrayList<>(List.of("A", "B", "C"));
        var picker = StandardRandomPicker.of(values, () -> new int[]{1});

        values.set(1, "Changed");
        values.clear();

        assertEquals("B", picker.pick());
    }

    /** Verifies that mutable elements are retained by reference rather than copied. */
    @Test
    void ofDoesNotCopyElements() {
        var value = new StringBuilder("A");
        var picker = StandardRandomPicker.of(List.of(value), () -> new int[]{0});

        value.append("B");

        assertSame(value, picker.pick());
        assertEquals("AB", value.toString());
    }

    /** Verifies that selection does not modify the array supplied by the randomizer. */
    @Test
    void pickDoesNotModifyRandomizerResult() {
        int[] indices = {1};
        var picker = StandardRandomPicker.of(List.of("A", "B"), () -> indices);

        assertEquals("B", picker.pick());
        assertArrayEquals(new int[]{1}, indices);
    }

    /** Verifies that each selection obtains a fresh index and construction does not generate values. */
    @Test
    void pickCallsRandomizerOnEveryInvocation() {
        int[] calls = {0};
        Randomizer randomizer = () -> {
            int call = calls[0]++;
            assertTrue(call < 2, "Unexpected randomizer call");
            return new int[]{call};
        };
        var picker = StandardRandomPicker.of(List.of("A", "B"), randomizer);

        assertEquals(0, calls[0], "Construction must not call the randomizer");
        assertEquals("A", picker.pick());
        assertEquals("B", picker.pick());
        assertEquals(2, calls[0]);
    }

    /** Verifies the default factory with a singleton list whose selection is deterministic. */
    @Test
    void ofWithDefaultRandomizerSupportsSingleElement() {
        var value = new Object();
        var picker = StandardRandomPicker.of(List.of(value));

        assertSame(value, picker.pick());
        assertSame(value, picker.pick());
    }

    /** Verifies that the default factory retains a snapshot of the source list. */
    @Test
    void ofWithDefaultRandomizerCopiesSourceList() {
        var value = new Object();
        var values = new ArrayList<>(List.of(value));
        var picker = StandardRandomPicker.of(values);

        values.clear();

        assertSame(value, picker.pick());
    }

    /** Verifies that both factory overloads reject a null list. */
    @Test
    void ofRejectsNullList() {
        assertThrows(NullPointerException.class, () -> StandardRandomPicker.of(null));
        assertThrows(NullPointerException.class, () -> StandardRandomPicker.of(null, unusedRandomizer()));
    }

    /** Verifies that both factory overloads reject an empty list. */
    @Test
    void ofRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> StandardRandomPicker.of(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> StandardRandomPicker.of(List.of(), unusedRandomizer()));
    }

    /** Verifies that both factory overloads reject a null element at any list position. */
    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void ofRejectsNullElement(int index) {
        var values = new ArrayList<>(Arrays.asList("A", "B", "C"));
        values.set(index, null);

        assertThrows(NullPointerException.class, () -> StandardRandomPicker.of(values));
        assertThrows(NullPointerException.class, () -> StandardRandomPicker.of(values, unusedRandomizer()));
    }

    /** Verifies that the factory rejects a null randomizer. */
    @Test
    void ofRejectsNullRandomizer() {
        assertThrows(NullPointerException.class, () -> StandardRandomPicker.of(List.of("A"), null));
    }

    /** Verifies that a null randomizer result is rejected as an invalid state. */
    @Test
    void pickRejectsNullResult() {
        var picker = StandardRandomPicker.of(List.of("A"), () -> null);

        assertThrows(IllegalStateException.class, picker::pick);
    }

    /** Verifies that the randomizer must return exactly one index. */
    @ParameterizedTest
    @ValueSource(ints = {0, 2, 3})
    void pickRejectsUnexpectedResultLength(int length) {
        var picker = StandardRandomPicker.of(List.of("A"), () -> new int[length]);

        assertThrows(IllegalStateException.class, picker::pick);
    }

    /** Verifies that negative indices and indices at or above the list size are rejected. */
    @ParameterizedTest
    @ValueSource(ints = {-1, 3, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void pickRejectsOutOfRangeIndex(int index) {
        var picker = StandardRandomPicker.of(List.of("A", "B", "C"), () -> new int[]{index});

        assertThrows(IllegalStateException.class, picker::pick);
    }

    /** Verifies that an exception thrown by the randomizer is propagated unchanged. */
    @Test
    void pickPropagatesRandomizerException() {
        var failure = new IllegalStateException("Randomizer failure");
        Randomizer randomizer = () -> {
            throw failure;
        };
        var picker = StandardRandomPicker.of(List.of("A"), randomizer);

        var actual = assertThrows(IllegalStateException.class, picker::pick);

        assertSame(failure, actual);
    }

    /** Returns a randomizer that fails if construction attempts to generate values. */
    private static Randomizer unusedRandomizer() {
        return () -> {
            throw new AssertionError("Randomizer must not be called during construction");
        };
    }
}
