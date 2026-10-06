package ivs.games.accessories.randomizer.dice;

import ivs.games.accessories.randomizer.Randomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardDiceCupTest {

    /** Verifies that a cup can contain a single die. */
    @Test
    void supportsSingleDie() {
        var cup = StandardDiceCup.of(1, () -> new int[]{0});

        assertArrayEquals(new int[]{1}, cup.roll());
    }

    /** Verifies that randomizer values are converted to face values without changing their order. */
    @Test
    void rollConvertsValuesToFacesInOrder() {
        var cup = StandardDiceCup.of(6, () -> new int[]{0, 1, 2, 3, 4, 5});

        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, cup.roll());
    }

    /** Verifies that the default factory creates a cup without requiring any arguments. */
    @Test
    void ofCreatesDefaultCup() {
        assertNotNull(StandardDiceCup.of());
    }

    /** Verifies that each default factory invocation creates a separate cup instance. */
    @Test
    void ofCreatesNewCupOnEveryCall() {
        assertNotSame(StandardDiceCup.of(), StandardDiceCup.of());
    }

    /** Verifies that a roll does not modify the array supplied by the randomizer. */
    @Test
    void rollDoesNotModifyRandomizerResult() {
        int[] values = {0, 5};
        var cup = StandardDiceCup.of(2, () -> values);

        int[] result = cup.roll();

        assertArrayEquals(new int[]{1, 6}, result);
        assertArrayEquals(new int[]{0, 5}, values);
        assertNotSame(values, result);
    }

    /** Verifies that every roll returns a new array even when the randomizer reuses its result array. */
    @Test
    void rollReturnsNewArrayOnEveryCall() {
        int[] values = {2, 4};
        var cup = StandardDiceCup.of(2, () -> values);

        int[] first = cup.roll();
        first[0] = -1;
        int[] second = cup.roll();

        assertNotSame(first, second);
        assertArrayEquals(new int[]{3, 5}, second);
        assertArrayEquals(new int[]{2, 4}, values);
    }

    /** Verifies that each roll obtains a fresh result from the supplied randomizer. */
    @Test
    void rollCallsRandomizerOnEveryInvocation() {
        int[] calls = {0};
        Randomizer randomizer = () -> {
            int call = calls[0]++;
            assertTrue(call < 2, "Unexpected randomizer call");
            return call == 0 ? new int[]{0, 5} : new int[]{3, 1};
        };
        var cup = StandardDiceCup.of(2, randomizer);

        assertEquals(0, calls[0], "Construction must not call the randomizer");
        assertArrayEquals(new int[]{1, 6}, cup.roll());
        assertArrayEquals(new int[]{4, 2}, cup.roll());
        assertEquals(2, calls[0]);
    }

    /** Verifies that both factory overloads reject a non-positive dice count. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void ofRejectsNonPositiveCount(int invalidCount) {
        assertThrows(IllegalArgumentException.class, () -> StandardDiceCup.of(invalidCount));
        assertThrows(IllegalArgumentException.class,
                () -> StandardDiceCup.of(invalidCount, unusedRandomizer()));
    }

    /** Verifies that the factory rejects a null randomizer. */
    @Test
    void ofRejectsNullRandomizer() {
        assertThrows(NullPointerException.class, () -> StandardDiceCup.of(1, null));
    }

    /** Verifies that a null randomizer result is rejected as an invalid state. */
    @Test
    void rollRejectsNullResult() {
        var cup = StandardDiceCup.of(1, () -> null);

        assertThrows(IllegalStateException.class, cup::roll);
    }

    /** Verifies that empty, shorter, and longer results are rejected for a cup containing two dice. */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    void rollRejectsUnexpectedResultLength(int length) {
        var cup = StandardDiceCup.of(2, () -> new int[length]);

        assertThrows(IllegalStateException.class, cup::roll);
    }

    /** Verifies that an out-of-range value is rejected even when surrounded by valid values. */
    @ParameterizedTest
    @ValueSource(ints = {-1, 6, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rollRejectsOutOfRangeValue(int invalidValue) {
        var cup = StandardDiceCup.of(3, () -> new int[]{0, invalidValue, 5});

        assertThrows(IllegalStateException.class, cup::roll);
    }

    /** Verifies that a failed roll leaves the randomizer's array unchanged. */
    @Test
    void rollDoesNotModifyResultWhenValidationFails() {
        int[] values = {0, 6, 5};
        var cup = StandardDiceCup.of(3, () -> values);

        assertThrows(IllegalStateException.class, cup::roll);

        assertArrayEquals(new int[]{0, 6, 5}, values);
    }

    /** Verifies that an exception thrown by the randomizer is propagated unchanged. */
    @Test
    void rollPropagatesRandomizerException() {
        var failure = new IllegalStateException("Randomizer failure");
        Randomizer randomizer = () -> {
            throw failure;
        };
        var cup = StandardDiceCup.of(1, randomizer);

        var actual = assertThrows(IllegalStateException.class, cup::roll);

        assertSame(failure, actual);
    }

    /** Returns a randomizer that fails if construction attempts to generate values. */
    private static Randomizer unusedRandomizer() {
        return () -> {
            throw new AssertionError("Randomizer must not be called during construction");
        };
    }
}
