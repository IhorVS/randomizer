/**
 * Single dice and cups containing fixed collections of dice with configurable face counts.
 *
 * <p>{@link ivs.games.accessories.randomizer.dice.Die} defines the single-die
 * contract. {@link ivs.games.accessories.randomizer.dice.StandardDie} converts
 * a randomizer index into a face value. {@link ivs.games.accessories.randomizer.dice.DiceCup}
 * defines the cup roll contract, and
 * {@link ivs.games.accessories.randomizer.dice.StandardDiceCup} supports mixed
 * face counts while preserving result order. Game rules are handled by the caller.
 */
package ivs.games.accessories.randomizer.dice;
