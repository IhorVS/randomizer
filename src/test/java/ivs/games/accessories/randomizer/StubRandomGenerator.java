package ivs.games.accessories.randomizer;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Supplies predetermined values and checks the number and order of bounded generator calls. */
final class StubRandomGenerator implements RandomGenerator {

    private final int[] expectedBounds;
    private final int[] values;
    private int index;

    StubRandomGenerator(int[] expectedBounds, int[] values) {
        assertEquals(expectedBounds.length, values.length);
        this.expectedBounds = expectedBounds.clone();
        this.values = values.clone();
    }

    @Override
    public int nextInt(int bound) {
        assertTrue(index < values.length, "Unexpected generator call");
        assertEquals(expectedBounds[index], bound, "Unexpected bound at call " + index);

        int value = values[index++];
        assertTrue(value >= 0 && value < bound, "Stub value must be within the requested range");
        return value;
    }

    @Override
    public long nextLong() {
        throw new AssertionError("Unexpected nextLong() call");
    }

    /** Verifies that every expected bounded generator call occurred. */
    void assertExhausted() {
        assertEquals(values.length, index, "Not all expected calls occurred");
    }
}
