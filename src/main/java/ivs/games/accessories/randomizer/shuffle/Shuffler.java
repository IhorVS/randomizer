package ivs.games.accessories.randomizer.shuffle;

import java.util.List;

/**
 * Randomly reorders the elements of a fixed list.
 *
 * <p>The input list's structure and order remain unchanged. Each result
 * contains the same element references in a shuffled order.
 *
 * <p>Implementations must document their thread-safety guarantees.
 *
 * @param <T> type of elements being shuffled
 */
public interface Shuffler<T> {

    /**
     * Returns a new list with the configured elements in random order.
     *
     * <p>The result is independent of the input list's structure and can be
     * modified without changing the configured elements.
     *
     * @return a new list containing all configured elements
     */
    List<T> shuffle();
}
