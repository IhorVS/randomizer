package ivs.games.accessories.randomizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class StandardRandomizerTest {

    /** Verifies that each position uses its corresponding bound in the configured order. */
    @Test
    void ofUsesCorrespondingBoundsInOrder() {
        var random = new StubRandomGenerator(new int[]{6, 8, 20}, new int[]{5, 0, 19});
        var randomizer = StandardRandomizer.of(random, 6, 8, 20);

        assertArrayEquals(new int[]{5, 0, 19}, randomizer.generate());
        random.assertExhausted();
    }

    /** Verifies that the repeated factory applies the same bound to every position. */
    @Test
    void repeatedUsesSameBoundForEveryPosition() {
        var random = new StubRandomGenerator(new int[]{6, 6}, new int[]{0, 5});
        var randomizer = StandardRandomizer.repeated(random, 2, 6);

        assertArrayEquals(new int[]{0, 5}, randomizer.generate());
        random.assertExhausted();
    }

    /** Verifies that subsequent generations continue consuming values from the supplied generator. */
    @Test
    void generateContinuesUsingSuppliedGenerator() {
        var random = new StubRandomGenerator(new int[]{6, 8, 6, 8}, new int[]{1, 7, 5, 0});
        var randomizer = StandardRandomizer.of(random, 6, 8);

        assertArrayEquals(new int[]{1, 7}, randomizer.generate());
        assertArrayEquals(new int[]{5, 0}, randomizer.generate());
        random.assertExhausted();
    }

    /** Verifies that changing the original bounds array does not affect the randomizer. */
    @Test
    void ofCopiesBounds() {
        int[] bounds = {6, 8};
        var random = new StubRandomGenerator(new int[]{6, 8}, new int[]{5, 7});
        var randomizer = StandardRandomizer.of(random, bounds);

        bounds[0] = 1;
        bounds[1] = 1;

        assertArrayEquals(new int[]{5, 7}, randomizer.generate());
        random.assertExhausted();
    }

    /** Verifies that each generation returns a separate array unaffected by changes to earlier results. */
    @Test
    void generateReturnsNewArrayOnEveryCall() {
        var random = new StubRandomGenerator(new int[]{6, 6}, new int[]{3, 3});
        var randomizer = StandardRandomizer.of(random, 6);

        int[] first = randomizer.generate();
        first[0] = -1;
        int[] second = randomizer.generate();

        assertNotSame(first, second);
        assertArrayEquals(new int[]{3}, second);
        random.assertExhausted();
    }

    /** Verifies the default-generator factory using a range whose only possible value is zero. */
    @Test
    void boundOfOneAlwaysProducesZero() {
        var randomizer = StandardRandomizer.of(1, 1);

        assertArrayEquals(new int[]{0, 0}, randomizer.generate());
    }

    /** Verifies the repeated default-generator factory using a deterministic range of [0, 1). */
    @Test
    void repeatedWithDefaultGeneratorSupportsBoundOfOne() {
        var randomizer = StandardRandomizer.repeated(3, 1);

        assertArrayEquals(new int[]{0, 0, 0}, randomizer.generate());
    }

    /** Verifies that the largest positive integer is accepted as an exclusive upper bound. */
    @Test
    void supportsMaximumIntegerBound() {
        var random = new StubRandomGenerator(new int[]{Integer.MAX_VALUE}, new int[]{Integer.MAX_VALUE - 1});
        var randomizer = StandardRandomizer.of(random, Integer.MAX_VALUE);

        assertArrayEquals(new int[]{Integer.MAX_VALUE - 1}, randomizer.generate());
        random.assertExhausted();
    }

    /** Verifies that the factory rejects a null random generator. */
    @Test
    void ofRejectsNullGenerator() {
        assertThrows(NullPointerException.class, () -> StandardRandomizer.of(null, 6));
    }

    /** Verifies that both factory overloads reject a null bounds array. */
    @Test
    void ofRejectsNullBounds() {
        assertThrows(NullPointerException.class, () -> StandardRandomizer.of((int[]) null));
        assertThrows(NullPointerException.class, () -> StandardRandomizer.of(unusedGenerator(), (int[]) null));
    }

    /** Verifies that both factory overloads reject an empty sequence of bounds. */
    @Test
    void ofRejectsEmptyBounds() {
        assertThrows(IllegalArgumentException.class, StandardRandomizer::of);
        assertThrows(IllegalArgumentException.class, () -> StandardRandomizer.of(unusedGenerator()));
    }

    /** Verifies that both factory overloads reject a non-positive bound among valid bounds. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void ofRejectsNonPositiveBounds(int invalidBound) {
        assertThrows(IllegalArgumentException.class, () -> StandardRandomizer.of(6, invalidBound, 8));
        assertThrows(IllegalArgumentException.class,
                () -> StandardRandomizer.of(unusedGenerator(), 6, invalidBound, 8));
    }

    /** Verifies that the repeated factory rejects a null random generator. */
    @Test
    void repeatedRejectsNullGenerator() {
        assertThrows(NullPointerException.class, () -> StandardRandomizer.repeated(null, 2, 6));
    }

    /** Verifies that both repeated factory overloads reject a non-positive position count. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void repeatedRejectsNonPositiveCount(int invalidCount) {
        assertThrows(IllegalArgumentException.class, () -> StandardRandomizer.repeated(invalidCount, 6));
        assertThrows(IllegalArgumentException.class,
                () -> StandardRandomizer.repeated(unusedGenerator(), invalidCount, 6));
    }

    /** Verifies that both repeated factory overloads reject a non-positive upper bound. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void repeatedRejectsNonPositiveBound(int invalidBound) {
        assertThrows(IllegalArgumentException.class, () -> StandardRandomizer.repeated(2, invalidBound));
        assertThrows(IllegalArgumentException.class,
                () -> StandardRandomizer.repeated(unusedGenerator(), 2, invalidBound));
    }

    /** Verifies that a generator exception is propagated unchanged. */
    @Test
    void generatePropagatesGeneratorException() {
        var failure = new IllegalStateException("Generator failure");
        RandomGenerator random = () -> {
            throw failure;
        };
        var randomizer = StandardRandomizer.of(random, 6);

        var actual = assertThrows(IllegalStateException.class, randomizer::generate);

        assertSame(failure, actual);
    }

    /** Returns a generator that fails if construction attempts to consume random values. */
    private static RandomGenerator unusedGenerator() {
        return () -> {
            throw new AssertionError("Generator must not be called during construction");
        };
    }
}
