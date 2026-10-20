package ivs.games.accessories.randomizer.dice;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import org.apache.commons.lang3.Validate;

/**
 * Rolls one die with a configurable positive number of faces.
 *
 * <p>The randomizer must return exactly one value per roll in the range
 * {@code [0, face count)}. The value is validated and converted to the
 * corresponding face from {@code 1} to the face count, inclusive.
 *
 * <p>The randomizer is retained and used directly. Uniform face probabilities
 * depend on its distribution; only result length and range are validated.
 * This implementation does not synchronize access to the randomizer.
 */
public final class StandardDie implements Die {

    private static final int DEFAULT_FACE_COUNT = 6;

    private final int faceCount;
    private final Randomizer randomizer;

    private StandardDie(int faceCount, Randomizer randomizer) {
        Validate.isTrue(faceCount > 0, "Face count must be positive");
        this.faceCount = faceCount;
        this.randomizer = Validate.notNull(randomizer, "Randomizer must not be null");
    }

    /**
     * Creates a standard six-sided die with its own standard randomizer.
     *
     * @return a new die
     */
    public static StandardDie of() {
        return of(DEFAULT_FACE_COUNT);
    }

    /**
     * Creates a die with the specified number of faces and its own standard randomizer.
     *
     * @param faceCount number of faces, at least one
     * @return a new die
     * @throws IllegalArgumentException if {@code faceCount} is not positive
     */
    public static StandardDie of(int faceCount) {
        return new StandardDie(faceCount, StandardRandomizer.of(faceCount));
    }

    /**
     * Creates a standard six-sided die using the supplied randomizer.
     *
     * @param randomizer randomizer used for every roll
     * @return a new die
     * @throws NullPointerException if {@code randomizer} is null
     */
    public static StandardDie of(Randomizer randomizer) {
        return of(DEFAULT_FACE_COUNT, randomizer);
    }

    /**
     * Creates a die with the specified number of faces using the supplied randomizer.
     *
     * @param faceCount number of faces, at least one
     * @param randomizer randomizer used for every roll
     * @return a new die
     * @throws IllegalArgumentException if {@code faceCount} is not positive
     * @throws NullPointerException if {@code randomizer} is null
     */
    public static StandardDie of(int faceCount, Randomizer randomizer) {
        return new StandardDie(faceCount, randomizer);
    }

    /**
     * Generates and validates a randomizer value, then converts it to a die face.
     *
     * <p>Exceptions thrown by the randomizer are propagated unchanged.
     *
     * @return a face value between {@code 1} and the configured face count
     * @throws IllegalStateException if the randomizer returns null, an array
     *                               whose length is not one, or a value outside
     *                               {@code [0, face count)}
     */
    @Override
    public int roll() {
        int[] values = randomizer.generate();

        Validate.validState(values != null, "Randomizer must not return null");
        Validate.validState(values.length == 1,
                "Expected one random value, but received %d", values.length);
        Validate.validState(values[0] >= 0 && values[0] < faceCount,
                "Random value must be between 0 and %d: %d", faceCount - 1, values[0]);

        return values[0] + 1;
    }
}
