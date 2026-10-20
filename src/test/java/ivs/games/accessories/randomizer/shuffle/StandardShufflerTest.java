package ivs.games.accessories.randomizer.shuffle;

import ivs.games.accessories.randomizer.Randomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardShufflerTest {

    /** Verifies the Fisher-Yates steps use indices from their respective ranges. */
    @Test
    void shuffleUsesGeneratedIndicesInOrder() {
        var shuffler = StandardShuffler.of(List.of("A", "B", "C", "D"), () -> new int[]{2, 1, 0});

        assertEquals(List.of("D", "A", "B", "C"), shuffler.shuffle());
    }

    /** Verifies that shuffling preserves element references and multiplicity. */
    @Test
    void shufflePreservesElementReferencesAndDuplicates() {
        var shared = new Object();
        var other = new Object();
        var shuffler = StandardShuffler.of(Arrays.asList(shared, other, shared), () -> new int[]{0, 0});

        List<Object> result = shuffler.shuffle();

        assertEquals(3, result.size());
        assertEquals(2, result.stream().filter(value -> value == shared).count());
        assertTrue(result.stream().anyMatch(value -> value == other));
    }

    /** Verifies that the source list is snapshotted at construction. */
    @Test
    void ofCopiesSourceList() {
        var values = new ArrayList<>(List.of("A", "B", "C"));
        var shuffler = StandardShuffler.of(values, () -> new int[]{0, 0});

        values.set(0, "Changed");
        values.clear();

        assertEquals(List.of("B", "C", "A"), shuffler.shuffle());
    }

    /** Verifies that each shuffle returns a new list and does not mutate prior results. */
    @Test
    void shuffleReturnsIndependentListOnEveryCall() {
        var shuffler = StandardShuffler.of(List.of("A", "B"), () -> new int[]{0});
        List<String> first = shuffler.shuffle();
        first.set(0, "Changed");

        List<String> second = shuffler.shuffle();

        assertNotSame(first, second);
        assertEquals(List.of("B", "A"), second);
    }

    /** Verifies that shuffling does not modify the randomizer's result array. */
    @Test
    void shuffleDoesNotModifyRandomizerResult() {
        int[] indices = {2, 1, 0};
        var shuffler = StandardShuffler.of(List.of("A", "B", "C", "D"), () -> indices);

        assertEquals(List.of("D", "A", "B", "C"), shuffler.shuffle());
        assertEquals(List.of(2, 1, 0), Arrays.stream(indices).boxed().toList());
    }

    /** Verifies that empty and singleton lists need no random values. */
    @Test
    void shuffleSupportsListsWithFewerThanTwoElements() {
        Randomizer unusedRandomizer = () -> {
            throw new AssertionError("Randomizer must not be called");
        };

        assertEquals(List.of(), StandardShuffler.of(List.of(), unusedRandomizer).shuffle());
        assertEquals(List.of("A"), StandardShuffler.of(List.of("A"), unusedRandomizer).shuffle());
        assertEquals(List.of(), StandardShuffler.of(List.of()).shuffle());
        assertEquals(List.of("A"), StandardShuffler.of(List.of("A")).shuffle());
    }

    /** Verifies that each shuffle obtains fresh indices and construction is lazy. */
    @Test
    void shuffleCallsRandomizerOnEveryInvocation() {
        int[] calls = {0};
        Randomizer randomizer = () -> {
            int call = calls[0]++;
            assertTrue(call < 2, "Unexpected randomizer call");
            return new int[]{call, 0};
        };
        var shuffler = StandardShuffler.of(List.of("A", "B", "C"), randomizer);

        assertEquals(0, calls[0], "Construction must not call the randomizer");
        assertEquals(List.of("B", "C", "A"), shuffler.shuffle());
        assertEquals(List.of("C", "A", "B"), shuffler.shuffle());
        assertEquals(2, calls[0]);
    }

    /** Verifies that the factories reject null lists and randomizers. */
    @Test
    void ofRejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> StandardShuffler.of(null));
        assertThrows(NullPointerException.class, () -> StandardShuffler.of(null, unusedRandomizer()));
        assertThrows(NullPointerException.class, () -> StandardShuffler.of(List.of("A"), null));
    }

    /** Verifies that both factories reject null elements. */
    @Test
    void ofRejectsNullElement() {
        var values = Arrays.asList("A", null);

        assertThrows(NullPointerException.class, () -> StandardShuffler.of(values));
        assertThrows(NullPointerException.class, () -> StandardShuffler.of(values, unusedRandomizer()));
    }

    /** Verifies that a null randomizer result is rejected as an invalid state. */
    @Test
    void shuffleRejectsNullResult() {
        var shuffler = StandardShuffler.of(List.of("A", "B"), () -> null);

        assertThrows(IllegalStateException.class, shuffler::shuffle);
    }

    /** Verifies that the randomizer must return exactly one index per shuffle step. */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    void shuffleRejectsUnexpectedResultLength(int length) {
        var shuffler = StandardShuffler.of(List.of("A", "B", "C"), () -> new int[length]);

        assertThrows(IllegalStateException.class, shuffler::shuffle);
    }

    /** Verifies that every generated index must fit its own descending range. */
    @Test
    void shuffleRejectsOutOfRangeIndexAtEveryStep() {
        var firstIndexInvalid = StandardShuffler.of(List.of("A", "B", "C"), () -> new int[]{3, 0});
        var secondIndexInvalid = StandardShuffler.of(List.of("A", "B", "C"), () -> new int[]{0, 2});
        var negativeIndex = StandardShuffler.of(List.of("A", "B", "C"), () -> new int[]{0, -1});

        assertThrows(IllegalStateException.class, firstIndexInvalid::shuffle);
        assertThrows(IllegalStateException.class, secondIndexInvalid::shuffle);
        assertThrows(IllegalStateException.class, negativeIndex::shuffle);
    }

    /** Verifies that exceptions thrown by the randomizer are propagated unchanged. */
    @Test
    void shufflePropagatesRandomizerException() {
        var failure = new IllegalStateException("Randomizer failure");
        Randomizer randomizer = () -> {
            throw failure;
        };
        var shuffler = StandardShuffler.of(List.of("A", "B"), randomizer);

        var actual = assertThrows(IllegalStateException.class, shuffler::shuffle);

        assertSame(failure, actual);
    }

    /** Returns a randomizer that fails if construction attempts to generate values. */
    private static Randomizer unusedRandomizer() {
        return () -> {
            throw new AssertionError("Randomizer must not be called during construction");
        };
    }
}
