package ivs.games.accessories.randomizer.shuffle;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Randomly reorders a snapshot of a list using the Fisher-Yates algorithm.
 *
 * <p>The configured list structure and order are copied during construction,
 * but its elements are retained by reference. The input may be empty and
 * cannot contain null elements.
 *
 * <p>For a list of size {@code n}, the randomizer must return exactly
 * {@code n - 1} values per shuffle. In order, these values must be in the
 * ranges {@code [0, n)}, {@code [0, n - 1)}, down to {@code [0, 2)}.
 * Result length and ranges are validated; uniform shuffle probabilities
 * depend on the randomizer's distribution. The randomizer is not called for
 * lists of size zero or one.
 *
 * <p>This implementation does not synchronize access to its randomizer.
 * Concurrent use is supported only if the randomizer supports it.
 *
 * @param <T> type of elements being shuffled
 */
public final class StandardShuffler<T> implements Shuffler<T> {

    private final List<T> values;
    private final Randomizer randomizer;

    private StandardShuffler(List<? extends T> values, Randomizer randomizer) {
        Validate.notNull(values, "Values must not be null");
        this.values = List.copyOf(values);
        this.randomizer = Validate.notNull(randomizer, "Randomizer must not be null");
    }

    /**
     * Creates a shuffler with its own standard randomizer.
     *
     * @param values elements to shuffle, in initial order
     * @param <T>    type of elements being shuffled
     * @return a new shuffler
     * @throws NullPointerException if {@code values} or any element is null
     */
    public static <T> StandardShuffler<T> of(List<? extends T> values) {
        Validate.notNull(values, "Values must not be null");
        int[] bounds = shuffleBounds(values.size());
        Randomizer randomizer = bounds.length == 0
                ? () -> new int[0]
                : StandardRandomizer.of(bounds);
        return new StandardShuffler<>(values, randomizer);
    }

    /**
     * Creates a shuffler using the supplied randomizer.
     *
     * <p>For a list of size {@code n}, the randomizer must return exactly
     * {@code n - 1} values per shuffle, each in the corresponding Fisher-Yates
     * range. It is not called for lists of size zero or one.
     *
     * @param values     elements to shuffle, in initial order
     * @param randomizer randomizer used for each shuffle
     * @param <T>        type of elements being shuffled
     * @return a new shuffler
     * @throws NullPointerException if {@code values}, any element, or {@code randomizer} is null
     */
    public static <T> StandardShuffler<T> of(List<? extends T> values, Randomizer randomizer) {
        return new StandardShuffler<>(values, randomizer);
    }

    /**
     * Returns a shuffled copy of the configured elements.
     *
     * <p>The input snapshot is not modified. Exceptions thrown by the
     * randomizer are propagated unchanged.
     *
     * @return a new mutable list containing all configured elements
     * @throws IllegalStateException if the randomizer returns null, a result of
     *                                unexpected length, or a value outside its
     *                                corresponding Fisher-Yates range
     */
    @Override
    public List<T> shuffle() {
        List<T> result = new ArrayList<>(values);
        if (result.size() < 2) {
            return result;
        }

        int[] indices = randomizer.generate();
        Validate.validState(indices != null, "Randomizer must not return null");
        Validate.validState(indices.length == result.size() - 1,
                "Expected %d random values, but received %d", result.size() - 1, indices.length);

        for (int index = 0; index < indices.length; index++) {
            int upperBound = result.size() - index;
            int selectedIndex = indices[index];
            Validate.validState(selectedIndex >= 0 && selectedIndex < upperBound,
                    "Random index must be between 0 and %d: %d", upperBound - 1, selectedIndex);

            int currentIndex = result.size() - index - 1;
            T currentValue = result.get(currentIndex);
            result.set(currentIndex, result.get(selectedIndex));
            result.set(selectedIndex, currentValue);
        }

        return result;
    }

    private static int[] shuffleBounds(int size) {
        int[] bounds = new int[Math.max(0, size - 1)];
        for (int index = 0; index < bounds.length; index++) {
            bounds[index] = size - index;
        }
        return bounds;
    }
}
