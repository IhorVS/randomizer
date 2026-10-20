package ivs.games.accessories.randomizer.dice;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

/**
 * Rolls a fixed non-empty collection of dice using one randomizer.
 *
 * <p>Each die may have a different positive number of faces. For a cup with
 * {@code n} dice, the randomizer must return exactly {@code n} values on each
 * roll. Each value must be in the range from {@code 0} (inclusive) to the
 * corresponding die's face count (exclusive). Values are converted to face
 * results from {@code 1} to that die's face count, inclusive.
 *
 * <p>The supplied randomizer is retained and used directly. Its result array
 * is never modified; each roll returns a new array. The no-argument factory
 * creates a cup with two six-sided dice. Uniform face probabilities depend
 * on the supplied randomizer; only result length and value ranges are validated.
 *
 * <p>This implementation does not synchronize access to the randomizer.
 * Concurrent use is supported only if the randomizer supports it.
 * The caller is responsible for ensuring safe access if it is shared.
 */
public final class StandardDiceCup implements DiceCup {

    private static final int FACE_COUNT = 6;

    private final int[] faceCounts;
    private final Randomizer randomizer;

    private StandardDiceCup(int[] faceCounts, Randomizer randomizer) {
        validateFaceCounts(faceCounts);
        this.faceCounts = faceCounts.clone();
        this.randomizer = Validate.notNull(randomizer, "Randomizer must not be null");
    }

    /**
     * Creates a cup containing two standard six-sided dice with its own
     * standard randomizer.
     *
     * @return a new dice cup
     */
    public static StandardDiceCup of() {
        return of(2);
    }

    /**
     * Creates a cup containing the specified number of six-sided dice.
     *
     * @param count number of dice, at least one
     * @return a new dice cup
     * @throws IllegalArgumentException if {@code count} is not positive
     */
    public static StandardDiceCup of(int count) {
        return new StandardDiceCup(repeatedFaces(count), StandardRandomizer.repeated(count, FACE_COUNT));
    }

    /**
     * Creates a cup containing the specified number of six-sided dice, using
     * the supplied randomizer.
     *
     * <p>The randomizer must generate exactly {@code count} values per call,
     * each uniformly distributed in {@code [0, 6)}. Its result length and
     * value ranges are checked on every roll.
     *
     * @param count      number of dice, at least one
     * @param randomizer randomizer used for every roll
     * @return a new dice cup
     * @throws IllegalArgumentException if {@code count} is not positive
     * @throws NullPointerException     if {@code randomizer} is null
     */
    public static StandardDiceCup of(int count, Randomizer randomizer) {
        return new StandardDiceCup(repeatedFaces(count), randomizer);
    }

    /**
     * Creates a cup containing one die for each supplied face count.
     *
     * <p>The cup uses its own standard randomizer with the corresponding
     * exclusive upper bound for each die.
     *
     * @param faceCounts number of faces for each die, in roll-result order
     * @return a new dice cup
     * @throws IllegalArgumentException if no face counts are supplied or any face count is not positive
     * @throws NullPointerException     if {@code faceCounts} is null
     */
    public static StandardDiceCup ofFaces(int... faceCounts) {
        validateFaceCounts(faceCounts);
        return new StandardDiceCup(faceCounts, StandardRandomizer.of(faceCounts));
    }

    /**
     * Creates a cup containing one die for each supplied face count, using
     * the supplied randomizer for every roll.
     *
     * <p>The randomizer must return one value per die, uniformly distributed
     * from zero (inclusive) to the corresponding face count (exclusive).
     * Its result length and ranges are checked on every roll.
     *
     * @param randomizer randomizer used for every roll
     * @param faceCounts number of faces for each die, in roll-result order
     * @return a new dice cup
     * @throws IllegalArgumentException if no face counts are supplied or any face count is not positive
     * @throws NullPointerException     if {@code randomizer} or {@code faceCounts} is null
     */
    public static StandardDiceCup ofFaces(Randomizer randomizer, int... faceCounts) {
        validateFaceCounts(faceCounts);
        return new StandardDiceCup(faceCounts, randomizer);
    }

    /**
     * Generates and validates randomizer values, then converts them to die face values.
     *
     * <p>Exceptions thrown by the randomizer are propagated unchanged.
     *
     * @return a new array containing one value per die, between {@code 1} and
     *         the corresponding die's face count
     * @throws IllegalStateException if the randomizer returns null, an array
     *                               of unexpected length, or a value outside
     *                               the corresponding face-count range
     */
    @Override
    public int[] roll() {
        int[] values = randomizer.generate();

        Validate.validState(values != null, "Randomizer must not return null");
        Validate.validState(values.length == faceCounts.length,
                "Expected %d random values, but received %d", faceCounts.length, values.length);

        int[] result = new int[values.length];
        for (int index = 0; index < values.length; index++) {
            Validate.validState(values[index] >= 0 && values[index] < faceCounts[index],
                    "Random value for die %d must be between 0 and %d: %d",
                    index, faceCounts[index] - 1, values[index]);
            result[index] = values[index] + 1;
        }
        return result;
    }

    private static int[] repeatedFaces(int count) {
        Validate.isTrue(count > 0, "Dice count must be positive");
        int[] faceCounts = new int[count];
        Arrays.fill(faceCounts, FACE_COUNT);
        return faceCounts;
    }

    private static void validateFaceCounts(int[] faceCounts) {
        Validate.notNull(faceCounts, "Face counts must not be null");
        Validate.isTrue(faceCounts.length > 0, "At least one die is required");
        for (int faceCount : faceCounts) {
            Validate.isTrue(faceCount > 0, "Face count must be positive: %d", faceCount);
        }
    }
}
