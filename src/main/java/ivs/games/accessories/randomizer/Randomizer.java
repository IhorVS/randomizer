package ivs.games.accessories.randomizer;

/**
 * Generates random integer sequences with a configured exclusive upper bound
 * for each position.
 *
 * <p>Each value is generated in the range from {@code 0} (inclusive)
 * to the corresponding upper bound (exclusive), with uniform probability.
 * Every upper bound must be positive.
 *
 * <p>For example, bounds {@code [6, 6, 20]} define a sequence of three
 * values in the ranges {@code [0, 6)}, {@code [0, 6)}, and {@code [0, 20)}.
 * Generated values can be used as indices into arrays or lists.
 *
 * <p>The positive number of positions and their upper bounds remain fixed
 * throughout the lifetime of the randomizer.
 *
 * <p>Implementations must document their thread-safety guarantees.
 */
public interface Randomizer {

    /**
     * Generates one random value for each configured position.
     *
     * <p>The returned array preserves the order of the configured bounds.
     * Each element is non-negative and less than its corresponding
     * exclusive upper bound.
     *
     * @return a new, non-null array whose length equals the number
     *         of configured positions
     */
    int[] generate();
}
