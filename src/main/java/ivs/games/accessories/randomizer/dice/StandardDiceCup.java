package ivs.games.accessories.randomizer.dice;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

/**
 * Rolls a fixed positive number of standard six-sided dice using a randomizer.
 *
 * <p>The randomizer must return exactly one value per die, each in the range
 * from {@code 0} (inclusive) to {@code 6} (exclusive). These values are validated
 * and converted to face values from {@code 1} to {@code 6}, inclusive.
 *
 * <p>The supplied randomizer is retained and used directly. Its result array
 * is never modified; each roll returns a new array.
 * The no-argument factory creates a cup with two dice. Factories without
 * a supplied randomizer create a standard randomizer with a default generator.
 * Uniform face probabilities depend on the supplied randomizer; only result
 * length and value ranges are validated.
 *
 * <p>This implementation does not synchronize access to the randomizer.
 * Concurrent use is supported only if the randomizer supports it.
 * The caller is responsible for ensuring safe access if it is shared.
 */
public final class StandardDiceCup implements DiceCup {

    private static final int FACE_COUNT = 6;

    private final int count;
    private final Randomizer randomizer;

    private StandardDiceCup(int count, Randomizer randomizer) {
        Validate.isTrue(count > 0, "Dice count must be positive");
        this.count = count;
        this.randomizer = Validate.notNull(randomizer, "Randomizer must not be null");
    }

    /**
     * Creates a cup containing two standard six-sided dice
     * with its own standard randomizer.
     *
     * @return a new dice cup
     */
    public static StandardDiceCup of() {
        return of(2);
    }

    /**
     * Creates a cup with its own standard randomizer.
     *
     * @param count number of dice, at least one
     * @return a new dice cup
     * @throws IllegalArgumentException if {@code count} is not positive
     */
    public static StandardDiceCup of(int count) {
        return new StandardDiceCup(count, StandardRandomizer.repeated(count, FACE_COUNT));
    }

    /**
     * Creates a cup with the supplied randomizer.
     *
     * <p>The randomizer must generate exactly {@code count} values per call,
     * each between {@code 0} and {@code 5}, inclusive, with uniform probability.
     * Its result length and value ranges are checked on every roll.
     *
     * @param count      number of dice, at least one
     * @param randomizer randomizer used for every roll
     * @return a new dice cup
     * @throws IllegalArgumentException if {@code count} is not positive
     * @throws NullPointerException     if {@code randomizer} is null
     */
    public static StandardDiceCup of(int count, Randomizer randomizer) {
        return new StandardDiceCup(count, randomizer);
    }

    /**
     * Generates and validates randomizer values, then converts them to die face values.
     *
     * <p>Exceptions thrown by the randomizer are propagated unchanged.
     *
     * @return a new array containing one value per die, each between {@code 1} and {@code 6}, inclusive
     * @throws IllegalStateException if the randomizer returns null, an array of an unexpected length,
     *                               or a value outside the range {@code [0, 6)}
     */
    @Override
    public int[] roll() {
        int[] values = randomizer.generate();

        Validate.validState(values != null, "Randomizer must not return null");
        Validate.validState(values.length == count,
                "Expected %d random values, but received %d", count, values.length);

        return Arrays.stream(values)
                .map(value -> {
                    Validate.validState(value >= 0 && value < FACE_COUNT,
                            "Random value must be between 0 and 5: %d", value);
                    return value + 1;
                })
                .toArray();
    }
}
