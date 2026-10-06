package ivs.games.accessories.randomizer.picker;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Randomly selects an element from a fixed, non-empty list using a randomizer.
 *
 * <p>The list is copied during construction, preserving its order and repeated
 * elements. Elements themselves are not copied. Null elements are not permitted.
 *
 * <p>The randomizer must return exactly one value in the range from {@code 0}
 * (inclusive) to the list size (exclusive). This value is validated and used
 * as the index of the selected element.
 *
 * <p>Selection does not remove elements or modify the stored list.
 * The default factory creates a standard randomizer with a default generator.
 * Uniform selection probabilities depend on the supplied randomizer; only
 * result length and index range are validated.
 *
 * <p>The supplied randomizer is retained and used directly.
 * This implementation does not synchronize access to it. Concurrent selection
 * is supported only if the randomizer supports it. The caller is responsible
 * for ensuring safe access if the randomizer or selected mutable elements are shared.
 *
 * @param <T> type of elements selected by this picker
 */
public final class StandardRandomPicker<T> implements RandomPicker<T> {

    private final List<T> values;
    private final Randomizer randomizer;

    private StandardRandomPicker(List<? extends T> values, Randomizer randomizer) {
        Validate.notNull(values, "Values must not be null");
        Validate.isTrue(!values.isEmpty(), "At least one value is required");

        this.values = List.copyOf(values);
        this.randomizer = Validate.notNull(randomizer, "Randomizer must not be null");
    }

    /**
     * Creates a picker with its own standard randomizer.
     *
     * @param values elements available for selection, in index order
     * @param <T>    type of elements selected by the picker
     * @return a new picker
     * @throws NullPointerException     if {@code values} or any element is null
     * @throws IllegalArgumentException if {@code values} is empty
     */
    public static <T> StandardRandomPicker<T> of(List<? extends T> values) {
        return new StandardRandomPicker<>(values, StandardRandomizer.of(values.size()));
    }

    /**
     * Creates a picker with the supplied randomizer.
     *
     * <p>The randomizer must generate exactly one value per call, uniformly
     * distributed in the range from {@code 0} (inclusive) to the list size
     * (exclusive). Its result length and index range are checked on every selection.
     *
     * @param values     elements available for selection, in index order
     * @param randomizer randomizer used for every selection
     * @param <T>        type of elements selected by the picker
     * @return a new picker
     * @throws NullPointerException     if {@code values}, any element, or {@code randomizer} is null
     * @throws IllegalArgumentException if {@code values} is empty
     */
    public static <T> StandardRandomPicker<T> of(List<? extends T> values, Randomizer randomizer) {
        return new StandardRandomPicker<>(values, randomizer);
    }

    /**
     * Generates and validates an index, then returns the corresponding element.
     *
     * <p>The element is returned by reference; it is not copied or removed.
     * The array returned by the randomizer is not modified.
     * Exceptions thrown by the randomizer are propagated unchanged.
     *
     * @return a non-null element from the configured list
     * @throws IllegalStateException if the randomizer returns null, an array whose length is not one,
     *                               or an index outside the range {@code [0, size)}
     */
    @Override
    public T pick() {
        int[] indices = randomizer.generate();

        Validate.validState(indices != null, "Randomizer must not return null");
        Validate.validState(indices.length == 1,
                "Expected one random value, but received %d", indices.length);

        int index = indices[0];
        Validate.validState(index >= 0 && index < values.size(),
                "Random index must be between 0 and %d: %d", values.size() - 1, index);

        return values.get(index);
    }

}
