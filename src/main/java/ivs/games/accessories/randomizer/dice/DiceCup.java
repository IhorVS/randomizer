package ivs.games.accessories.randomizer.dice;

/**
 * Rolls a fixed positive number of standard six-sided dice.
 *
 * <p>Each die produces a value from {@code 1} to {@code 6}, inclusive,
 * with uniform probability. Each position in the result corresponds
 * to one die.
 *
 * <p>The number of dice remains fixed throughout the lifetime of the cup.
 * Interpretation of roll results according to game rules is the caller's
 * responsibility.
 *
 * <p>Implementations must document their thread-safety guarantees.
 */
public interface DiceCup {

    /**
     * Rolls all dice and returns their face values in die order.
     *
     * <p>Each call returns a new array containing one value per die.
     * Every value is between {@code 1} and {@code 6}, inclusive.
     *
     * @return a new, non-null array whose length equals the number of dice
     */
    int[] roll();
}
