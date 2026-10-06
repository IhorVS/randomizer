package ivs.games.accessories.randomizer.picker;

/**
 * Randomly selects an element from a fixed, non-empty list.
 *
 * <p>Each list position has equal selection probability. Repeated elements
 * are allowed and increase the probability of selecting those elements.
 * Null elements are not permitted.
 *
 * <p>Selection does not remove elements: every call selects from the full list,
 * and the same element may be selected on consecutive calls.
 *
 * <p>The list's size, order, and element references remain fixed throughout
 * the lifetime of the picker. Elements themselves are not required to be immutable.
 *
 * <p>Implementations must document their thread-safety guarantees.
 *
 * @param <T> type of elements selected by this picker
 */
public interface RandomPicker<T> {

    /**
     * Randomly selects one element from the configured list.
     *
     * <p>The selected element is returned by reference; it is not copied
     * or removed from the list.
     *
     * @return a non-null element from the configured list
     */
    T pick();
}
