package ivs.games.accessories.randomizer.dice;

import ivs.games.accessories.randomizer.Randomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class StandardDieTest {

    /** Verifies the default die produces a standard six-sided result. */
    @Test
    void ofCreatesSixSidedDie() {
        int result = StandardDie.of().roll();

        assertTrue(result >= 1 && result <= 6);
    }

    /** Verifies the default-face overload maps both ends of the range. */
    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    void ofRandomizerUsesSixFaces(int index) {
        var die = StandardDie.of(() -> new int[]{index});

        assertEquals(index + 1, die.roll());
    }

    /** Verifies a die with custom face count maps generated bounds into face values. */
    @ParameterizedTest
    @ValueSource(ints = {0, 19})
    void ofFaceCountAndRandomizerUsesConfiguredFaces(int index) {
        var die = StandardDie.of(20, () -> new int[]{index});

        assertEquals(index + 1, die.roll());
    }

    /** Verifies a one-faced die always returns its only face. */
    @Test
    void supportsOneFace() {
        assertEquals(1, StandardDie.of(1).roll());
    }

    /** Verifies non-positive face counts and a null randomizer are rejected. */
    @Test
    void factoriesRejectInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> StandardDie.of(0));
        assertThrows(IllegalArgumentException.class, () -> StandardDie.of(-1));
        assertThrows(NullPointerException.class, () -> StandardDie.of(null));
        assertThrows(NullPointerException.class, () -> StandardDie.of(6, null));
    }

    /** Verifies a null randomizer result is rejected. */
    @Test
    void rollRejectsNullResult() {
        var die = StandardDie.of(6, () -> null);

        assertThrows(IllegalStateException.class, die::roll);
    }

    /** Verifies the randomizer must return exactly one value. */
    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void rollRejectsUnexpectedResultLength(int length) {
        var die = StandardDie.of(6, () -> new int[length]);

        assertThrows(IllegalStateException.class, die::roll);
    }

    /** Verifies values below or above the configured range are rejected. */
    @ParameterizedTest
    @ValueSource(ints = {-1, 6, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rollRejectsOutOfRangeValue(int value) {
        var die = StandardDie.of(6, () -> new int[]{value});

        assertThrows(IllegalStateException.class, die::roll);
    }

    /** Verifies exceptions from the randomizer are propagated unchanged. */
    @Test
    void rollPropagatesRandomizerException() {
        var failure = new IllegalStateException("Randomizer failure");
        Randomizer randomizer = () -> {
            throw failure;
        };
        var die = StandardDie.of(6, randomizer);

        var actual = assertThrows(IllegalStateException.class, die::roll);

        assertSame(failure, actual);
    }
}
