package ivs.games.accessories.randomizer;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.random.RandomGenerator;

/**
 * Generates random integer sequences using one random generator
 * and a configured exclusive upper bound for each position.
 *
 * <p>Each value is generated in the range from {@code 0} (inclusive)
 * to its corresponding upper bound (exclusive). All bounds must be positive.
 * The number of positions equals the number of configured bounds.
 * Selection probabilities are determined by the supplied generator, which must
 * satisfy the uniform bounded generation contract of {@link RandomGenerator#nextInt(int)}.
 *
 * <p>The bounds are copied during construction and are never modified.
 * Each call to {@link #generate()} returns a new array.
 *
 * <p>The random generator is retained and used directly; it is not copied
 * or reseeded. The same generator is used for all positions and subsequent
 * calls to {@code generate()}.
 *
 * <p>This implementation does not synchronize access to the generator.
 * Concurrent use is supported only if the generator supports it.
 * The caller is responsible for ensuring safe access when sharing a generator
 * between threads or randomizer instances.
 *
 * <p>Even with a thread-safe generator, an entire sequence is not generated
 * atomically: generator calls from concurrent operations may interleave.
 * Results therefore depend on the order in which those calls occur.
 */
public final class StandardRandomizer implements Randomizer {

    private final RandomGenerator random;
    private final int[] bounds;

    /**
     * Creates a randomizer with the supplied generator and bounds.
     *
     * @param random random generator
     * @param bounds exclusive upper bound for each position, in result order
     * @throws NullPointerException     if {@code random} or {@code bounds} is null
     * @throws IllegalArgumentException if {@code bounds} is empty
     *                                  or any bound is not positive
     */
    @SuppressWarnings("SimplifyStreamApiCallChains")
    private StandardRandomizer(RandomGenerator random, int[] bounds) {
        this.random = Validate.notNull(random, "Random generator must not be null");
        Validate.notNull(bounds, "Bounds must not be null");
        Validate.isTrue(bounds.length > 0, "At least one bound is required");

        this.bounds = Arrays.stream(bounds)
                .map(bound -> {
                    Validate.isTrue(bound > 0, "Bound must be positive: %d", bound);
                    return bound;
                })
                .toArray();
    }

    /**
     * Creates a randomizer with a separate upper bound for each position
     * and a newly obtained default random generator.
     *
     * @param bounds exclusive upper bound for each position, in result order
     * @return a new randomizer
     * @throws NullPointerException     if {@code bounds} is null
     * @throws IllegalArgumentException if {@code bounds} is empty
     *                                  or any bound is not positive
     */
    public static StandardRandomizer of(int... bounds) {
        return of(RandomGenerator.getDefault(), bounds);
    }

    /**
     * Creates a randomizer with a separate upper bound for each position
     * and the supplied random generator.
     *
     * <p>The bounds are copied. The generator is retained and used directly.
     * The caller is responsible for ensuring safe access if it is shared.
     *
     * @param random random generator
     * @param bounds exclusive upper bound for each position, in result order
     * @return a new randomizer
     * @throws NullPointerException     if {@code random} or {@code bounds} is null
     * @throws IllegalArgumentException if {@code bounds} is empty
     *                                  or any bound is not positive
     */
    public static StandardRandomizer of(RandomGenerator random, int... bounds) {
        return new StandardRandomizer(random, bounds);
    }

    /**
     * Creates a randomizer with the same upper bound for all positions
     * and a newly obtained default random generator.
     *
     * @param count number of positions
     * @param bound exclusive upper bound for every position
     * @return a new randomizer
     * @throws IllegalArgumentException if {@code count} or {@code bound}
     *                                  is not positive
     */
    public static StandardRandomizer repeated(int count, int bound) {
        return repeated(RandomGenerator.getDefault(), count, bound);
    }

    /**
     * Creates a randomizer with the same upper bound for all positions
     * and the supplied random generator.
     *
     * <p>The generator is retained and used directly.
     * The caller is responsible for ensuring safe access if it is shared.
     *
     * @param random random generator
     * @param count  number of positions
     * @param bound  exclusive upper bound for every position
     * @return a new randomizer
     * @throws NullPointerException     if {@code random} is null
     * @throws IllegalArgumentException if {@code count} or {@code bound}
     *                                  is not positive
     */
    public static StandardRandomizer repeated(
            RandomGenerator random,
            int count,
            int bound
    ) {
        Validate.notNull(random, "Random generator must not be null");
        Validate.isTrue(count > 0, "Position count must be positive");
        Validate.isTrue(bound > 0, "Bound must be positive");

        int[] bounds = new int[count];
        Arrays.fill(bounds, bound);

        return new StandardRandomizer(random, bounds);
    }

    /**
     * Generates values in position order by calling
     * {@link RandomGenerator#nextInt(int)} once for each configured bound.
     *
     * <p>This operation does not synchronize access to the generator
     * and is not atomic when called concurrently. Exceptions thrown by the
     * generator are propagated unchanged.
     *
     * @return a new array containing one value per configured position,
     * each in the range from {@code 0} (inclusive)
     * to its corresponding upper bound (exclusive)
     */
    @Override
    public int[] generate() {
        int[] result = new int[bounds.length];

        for (int i = 0; i < bounds.length; i++) {
            result[i] = random.nextInt(bounds[i]);
        }

        return result;
    }
}
