package ivs.games.accessories.randomizer.coin;

import ivs.games.accessories.randomizer.Randomizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardCoinTest {

    /** Verifies that each valid random index selects the corresponding coin side. */
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void getSideReturnsSideAtGeneratedIndex(int index) {
        var coin = StandardCoin.of(() -> new int[]{index});

        assertEquals(CoinSide.values()[index], coin.getSide());
    }

    /** Verifies that the default factory creates a coin. */
    @Test
    void ofCreatesDefaultCoin() {
        assertNotNull(StandardCoin.of().getSide());
    }

    /** Verifies that every factory invocation creates a separate coin instance. */
    @Test
    void ofCreatesNewCoinOnEveryCall() {
        assertNotSame(StandardCoin.of(), StandardCoin.of());
    }

    /** Verifies construction is lazy and each toss requests a new index. */
    @Test
    void getSideCallsRandomizerOnEveryInvocation() {
        int[] calls = {0};
        Randomizer randomizer = () -> {
            int call = calls[0]++;
            assertTrue(call < 2, "Unexpected randomizer call");
            return new int[]{call};
        };
        var coin = StandardCoin.of(randomizer);

        assertEquals(0, calls[0], "Construction must not call the randomizer");
        assertEquals(CoinSide.HEADS, coin.getSide());
        assertEquals(CoinSide.TAILS, coin.getSide());
        assertEquals(2, calls[0]);
    }

    /** Verifies that the factory rejects a null randomizer. */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void ofRejectsNullRandomizer() {
        assertThrows(NullPointerException.class, () -> StandardCoin.of(null));
    }

    /** Verifies that a null randomizer result is rejected as an invalid state. */
    @Test
    void getSideRejectsNullResult() {
        var coin = StandardCoin.of(() -> null);

        assertThrows(IllegalStateException.class, coin::getSide);
    }

    /** Verifies that the randomizer must return exactly one index. */
    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void getSideRejectsUnexpectedResultLength(int length) {
        var coin = StandardCoin.of(() -> new int[length]);

        assertThrows(IllegalStateException.class, coin::getSide);
    }

    /** Verifies that indices outside the two-side range are rejected. */
    @ParameterizedTest
    @ValueSource(ints = {-1, 2, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void getSideRejectsOutOfRangeIndex(int index) {
        var coin = StandardCoin.of(() -> new int[]{index});

        assertThrows(IllegalStateException.class, coin::getSide);
    }

    /** Verifies that exceptions thrown by the randomizer are propagated unchanged. */
    @Test
    void getSidePropagatesRandomizerException() {
        var failure = new IllegalStateException("Randomizer failure");
        Randomizer randomizer = () -> {
            throw failure;
        };
        var coin = StandardCoin.of(randomizer);

        var actual = assertThrows(IllegalStateException.class, coin::getSide);

        assertSame(failure, actual);
    }
}
