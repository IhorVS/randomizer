/**
 * Two-sided coin tosses with the possible results {@code HEADS} and {@code TAILS}.
 *
 * <p>{@link ivs.games.accessories.randomizer.coin.Coin} defines the toss
 * contract. {@link StandardCoin}
 * selects between both sides using a randomizer. A supplied randomizer must
 * return one index in {@code [0, 2)} per toss; equal probabilities depend on
 * its distribution. The standard implementation validates the result shape
 * and range but does not test statistical distribution.
 *
 * <p>The standard implementation does not synchronize access to its randomizer.
 * Safe concurrent use depends on the randomizer implementation.
 */
package ivs.games.accessories.randomizer.coin;
