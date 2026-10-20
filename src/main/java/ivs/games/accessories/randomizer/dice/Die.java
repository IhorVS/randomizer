package ivs.games.accessories.randomizer.dice;

/**
 * Rolls one die with a fixed positive number of faces.
 *
 * <p>Implementations must document their thread-safety guarantees.
 */
public interface Die {

    /**
     * Rolls the die.
     *
     * @return a face value between one and the die's number of faces, inclusive
     */
    int roll();
}
