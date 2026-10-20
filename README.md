# Randomizer

A Java library for configurable random integer sequences, standard six-sided dice,
two-sided coins, and random selection from lists. Game rules and interpretation of results belong to the calling code.

## Requirements and build

- JDK 21 or later (the project targets Java 21).
- Maven.

```shell
mvn clean test
mvn clean install
```

The build produces a standard JAR and an attached source JAR. The runtime dependency is Apache Commons Lang 3.
The POM also declares Lombok as provided and JUnit 5 / Mockito for tests.

After installing the project locally, add this dependency to a consuming Maven project:

```xml
<dependency>
    <groupId>ivs.games.accessories</groupId>
    <artifactId>randomizer</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Components

| Package  | Interface         | Implementation            | Operation            |
|----------|-------------------|---------------------------|----------------------|
| Root     | `Randomizer`      | `StandardRandomizer`      | `int[] generate()`   |
| `dice`   | `DiceCup`         | `StandardDiceCup`         | `int[] roll()`       |
| `coin`   | `Coin`            | `StandardCoin`           | `CoinSide getSide()` |
| `picker` | `RandomPicker<T>` | `StandardRandomPicker<T>` | `T pick()`           |

The provided standard implementations use static factories with private constructors.

## Integer sequences

Each position has a positive exclusive upper bound. Results start at zero:
for bounds `[6, 8, 20]`, the three ranges are `[0, 6)`, `[0, 8)`, and `[0, 20)`.
The result length and order match the configured bounds. Bounds are copied at creation;
every generation returns a new array.

```java
import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.StandardRandomizer;

Randomizer randomizer = StandardRandomizer.of(6, 8, 20);
int[] indices = randomizer.generate();

// Five reel positions, with a separate length for each reel.
Randomizer reels = StandardRandomizer.of(20, 24, 18, 30, 22);
int[] stops = reels.generate();

// Five positions with the same exclusive upper bound.
Randomizer repeated = StandardRandomizer.repeated(5, 20);
```

| Factory                                                  | Configuration                                   |
|----------------------------------------------------------|-------------------------------------------------|
| `of(int... bounds)`                                      | Separate bounds, default generator              |
| `of(RandomGenerator random, int... bounds)`              | Separate bounds, supplied generator             |
| `repeated(int count, int bound)`                         | Same bound for all positions, default generator |
| `repeated(RandomGenerator random, int count, int bound)` | Same bound, supplied generator                  |

One generator serves every position and every subsequent call. A custom generator is retained directly,
without copying or reseeding:

```java
import java.util.Random;
import ivs.games.accessories.randomizer.StandardRandomizer;

var randomizer = StandardRandomizer.of(new Random(123), 6, 8, 20);
```

A seeded generator supports repeatable sequences when its algorithm and the order of calls remain the same.

## Dice cup

A cup contains at least one standard six-sided die. Each roll returns a new array of face values from `1` to `6`.
The no-argument factory creates a cup with two dice.

```java
import ivs.games.accessories.randomizer.dice.DiceCup;
import ivs.games.accessories.randomizer.dice.StandardDiceCup;

DiceCup pair = StandardDiceCup.of();
int[] faces = pair.roll();

DiceCup single = StandardDiceCup.of(1);
DiceCup three = StandardDiceCup.of(3);
```

| Factory                                | Configuration                                              |
|----------------------------------------|------------------------------------------------------------|
| `of()`                                 | Two dice, own standard randomizer                          |
| `of(int count)`                        | Specified positive number of dice, own standard randomizer |
| `of(int count, Randomizer randomizer)` | Specified count, supplied randomizer                       |

A supplied randomizer must return exactly `count` values in `[0, 6)`.
The cup validates them and adds one to each value without modifying the supplied array.
For backgammon, interpreting doubles or expanding them into four moves is the engine's responsibility.

## Coin

A coin toss returns either `CoinSide.HEADS` or `CoinSide.TAILS`, with equal probability.
The default factory uses a standard randomizer; an overload accepts a `Randomizer` for controlled results:

```java
import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.coin.Coin;
import ivs.games.accessories.randomizer.coin.CoinSide;
import ivs.games.accessories.randomizer.coin.StandardCoin;

Coin coin = StandardCoin.of();
CoinSide side = coin.getSide();

// A controlled index makes a coin toss deterministic in a test.
Randomizer fixedIndex = () -> new int[]{0};
Coin fixedCoin = StandardCoin.of(fixedIndex);
// fixedCoin.getSide() returns CoinSide.HEADS.
```

| Factory                         | Configuration                                  |
|---------------------------------|------------------------------------------------|
| `of()`                          | Own standard randomizer with a default generator |
| `of(Randomizer randomizer)`     | Supplied randomizer                            |

The supplied randomizer must return exactly one index in `[0, 2)` for each toss.
The coin validates the result length and range, but equal probabilities depend on the randomizer's distribution.
Exceptions from the randomizer propagate unchanged.

## Random picker

A picker selects one element from a fixed, non-empty list. Every list position has equal selection probability.
Selection is with replacement: an element remains available after selection and can be selected again immediately.

```java
import java.util.List;
import ivs.games.accessories.randomizer.picker.RandomPicker;
import ivs.games.accessories.randomizer.picker.StandardRandomPicker;

RandomPicker<String> picker = StandardRandomPicker.of(List.of("A", "B", "C"));
String selected = picker.pick();

// Repeated entries provide weights: A has probability 2/3, B has probability 1/3.
RandomPicker<String> weighted = StandardRandomPicker.of(List.of("A", "A", "B"));

```

| Factory                                               | Configuration                          |
|-------------------------------------------------------|----------------------------------------|
| `of(List<? extends T> values)`                        | List snapshot, own standard randomizer |
| `of(List<? extends T> values, Randomizer randomizer)` | List snapshot, supplied randomizer     |

The picker preserves the list's order and repeated entries. Later structural changes to the source list
have no effect. Null entries are prohibited. Elements are not copied, and selections return the original references;
changes to mutable elements remain visible.

A supplied randomizer must return exactly one index in `[0, list size)`.
The picker validates it and does not modify the supplied array.

## Validation and errors

| Condition                                                                                       | Exception                  |
|-------------------------------------------------------------------------------------------------|----------------------------|
| Null factory arguments or null picker elements                                                  | `NullPointerException`     |
| Empty bounds or picker list; non-positive bound or count                                        | `IllegalArgumentException` |
| Supplied randomizer returns null, the wrong length, or an out-of-range value to a cup, coin, or picker | `IllegalStateException`    |

Exceptions from supplied generators and randomizers propagate unchanged.
Cups, coins, and pickers validate result shape and range; they do not test its statistical distribution.
Custom implementations are responsible for the probability guarantees in the interfaces.

## Deterministic tests

Use the interfaces to supply fixed results in tests of game logic:

```java
import java.util.List;
import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.dice.DiceCup;
import ivs.games.accessories.randomizer.dice.StandardDiceCup;
import ivs.games.accessories.randomizer.picker.StandardRandomPicker;

// Test double for a game that depends on DiceCup.
DiceCup fixedCup = () -> new int[]{3, 6};

// Test the cup's conversion using controlled zero-based indices.
Randomizer fixedIndices = () -> new int[]{2, 5};
var cup = StandardDiceCup.of(2, fixedIndices);
// cup.roll() returns [3, 6].

var picker = StandardRandomPicker.of(List.of("A", "B"), () -> new int[]{1});
// picker.pick() returns "B".
```

These fixed implementations are test doubles, rather than production sources of uniform random values.
The project's tests use controlled values, parameterized validation cases, and singleton ranges/lists
where default-generator results are deterministic. They do not rely on statistical assertions.

## Thread safety

The implementations do not synchronize calls to their random source. Bounds and stored list structure remain fixed;
integer generation and dice rolls return separate arrays, and coin tosses return enum values. Concurrent use depends
on the supplied generator or randomizer. Callers are responsible for shared mutable picker elements as well.

A thread-safe generator does not make an entire multi-value generation atomic: concurrent calls can interleave,
and the values assigned to each session depend on call order. Different randomizer instances still share state
if they receive the same generator.

A practical arrangement is one randomizer and its own generator per session, with sequential processing
within that session. A dedicated permanent thread for each session is not required.

## License

[MIT](LICENSE), copyright 2026 Ihor Shaniuk.
