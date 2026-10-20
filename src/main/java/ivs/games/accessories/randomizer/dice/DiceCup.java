package ivs.games.accessories.randomizer.dice;

/**
 * Rolls a fixed non-empty collection of dice.
 *
 * <p>Each position in the result corresponds to one die and contains a value
 * from {@code 1} to that die's number of faces, inclusive. Uniform face
 * probabilities depend on the implementation.
 *
 * <p>The dice and their order remain fixed throughout the lifetime of the cup.
 * Interpretation of roll results according to game rules is the caller's
 * responsibility.
 *
 * <p>Implementations must document their thread-safety guarantees.
 */
public interface DiceCup {

    /**
     * Rolls all dice and returns their face values in die order.
     *
     * <p>Each call returns a new array containing one value per die, in die
     * order. Every value is between {@code 1} and the corresponding die's
     * number of faces, inclusive.
     *
     * @return a new, non-null array whose length equals the number of dice
     */
    int[] roll();
}
