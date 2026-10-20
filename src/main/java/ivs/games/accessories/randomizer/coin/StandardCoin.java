package ivs.games.accessories.randomizer.coin;

import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;
import ivs.games.accessories.randomizer.picker.RandomPicker;
import ivs.games.accessories.randomizer.picker.StandardRandomPicker;
import lombok.NonNull;

import java.util.Arrays;

/**
 * Tosses a two-sided coin by randomly selecting one of its sides.
 *
 * <p>The supplied randomizer must produce exactly one index in the range
 * {@code [0, 2)} for each toss. Result length and range are validated;
 * equal probabilities depend on the randomizer's distribution.
 *
 * <p>The no-argument factory creates a standard randomizer with a default
 * generator. This implementation does not synchronize access to its
 * randomizer; concurrent use is supported only if the randomizer supports it.
 */
public class StandardCoin implements Coin {

    private final RandomPicker<CoinSide> randomPicker;

    private StandardCoin(@NonNull Randomizer randomizer) {
        this.randomPicker = StandardRandomPicker.of(Arrays.asList(CoinSide.values()), randomizer);
    }

    @Override
    public CoinSide getSide() {
        return randomPicker.pick();
    }

    /**
     * Creates a coin using the supplied randomizer for every toss.
     *
     * @param randomizer randomizer that returns one index in {@code [0, 2)}
     * @return a new coin
     * @throws NullPointerException if {@code randomizer} is null
     */
    public static StandardCoin of(@NonNull Randomizer randomizer) {
        return new StandardCoin(randomizer);
    }

    /**
     * Creates a coin with its own standard randomizer.
     *
     * @return a new coin
     */
    public  static StandardCoin of() {
        return new StandardCoin(StandardRandomizer.of(2));
    }
}
