package ivs.games.accessories.randomizer.coin;

/**
 * Tosses a two-sided coin.
 *
 * <p>Each side, {@link CoinSide#HEADS} and {@link CoinSide#TAILS}, is returned
 * with equal probability.
 */
public interface Coin {

    /**
     * Tosses the coin.
     *
     * @return one of {@link CoinSide#HEADS} or {@link CoinSide#TAILS}
     */
    CoinSide getSide();
}
