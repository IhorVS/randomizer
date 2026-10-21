# Randomizer

A Java library for configurable random integer sequences, standard six-sided dice,
two-sided coins, random selection from lists, and shuffling. Game rules and interpretation of results belong to the calling code.

## Requirements and build

- JDK 21 or later (the project targets Java 21).
- Maven Wrapper downloads the configured Maven version automatically.

```shell
sh ./mvnw clean test
sh ./mvnw clean install
```

On Windows, use `mvnw.cmd` instead of `sh ./mvnw`.

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

| Package   | Interface         | Implementation            | Operation            |
|-----------|-------------------|---------------------------|----------------------|
| Root      | `Randomizer`      | `StandardRandomizer`      | `int[] generate()`   |
| `dice`    | `Die`             | `StandardDie`             | `int roll()`         |
| `dice`    | `DiceCup`         | `StandardDiceCup`         | `int[] roll()`       |
| `coin`    | `Coin`             | `StandardCoin`             | `CoinSide getSide()` |
| `picker`  | `RandomPicker<T>` | `StandardRandomPicker<T>` | `T pick()`           |
| `shuffle` | `Shuffler<T>`     | `StandardShuffler<T>`     | `List<T> shuffle()`  |

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

## Dice and dice cup

A die has a positive number of faces and returns a value from `1` through that number. The default die is a standard d6.
The cup can contain dice with different face counts. The existing count-based factories remain compatible and create d6 dice;
the no-argument cup factory creates two dice.

```java
import ivs.games.accessories.randomizer.dice.Die;
import ivs.games.accessories.randomizer.dice.DiceCup;
import ivs.games.accessories.randomizer.dice.StandardDie;
import ivs.games.accessories.randomizer.dice.StandardDiceCup;

Die d6 = StandardDie.of();
Die d20 = StandardDie.of(20);

DiceCup pair = StandardDiceCup.of();
int[] faces = pair.roll();

DiceCup single = StandardDiceCup.of(1);
DiceCup three = StandardDiceCup.of(3);

// One d6, one d8, and one d20; result positions follow this order.
DiceCup mixed = StandardDiceCup.ofFaces(6, 8, 20);
int[] mixedFaces = mixed.roll();
```

| Factory                                         | Configuration                                              |
|-------------------------------------------------|------------------------------------------------------------|
| `StandardDie.of()`                              | One d6, own standard randomizer                            |
| `StandardDie.of(int faceCount)`                 | One die with specified positive face count                 |
| `StandardDie.of(Randomizer randomizer)`         | One d6 using the supplied randomizer                       |
| `StandardDie.of(int faceCount, Randomizer)`     | One configured die using the supplied randomizer           |
| `StandardDiceCup.of()`                          | Two d6 dice, own standard randomizer                       |
| `StandardDiceCup.of(int count)`                 | Specified positive number of d6 dice                       |
| `StandardDiceCup.of(int count, Randomizer)`     | Specified number of d6 dice, supplied randomizer           |
| `StandardDiceCup.ofFaces(int... faceCounts)`    | One die per face count, own standard randomizer            |
| `StandardDiceCup.ofFaces(Randomizer, int...)`   | One die per face count, supplied randomizer                |

A supplied die randomizer must return exactly one value in `[0, faceCount)`.
A supplied cup randomizer must return exactly one value per die; each value's exclusive upper bound is that die's face count.
Both validate result length and ranges, then add one to each value without modifying the supplied array.
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

## Shuffling

A shuffler stores a snapshot of a list and returns a new, mutable list in shuffled order.
The source list is not changed; element references and duplicates are preserved. Empty and singleton lists are supported.

```java
import java.util.List;
import ivs.games.accessories.randomizer.shuffle.Shuffler;
import ivs.games.accessories.randomizer.shuffle.StandardShuffler;

Shuffler<String> deck = StandardShuffler.of(List.of("A", "B", "C", "D"));
List<String> shuffled = deck.shuffle();
```

| Factory                                              | Configuration                          |
|------------------------------------------------------|-----------------------------------------|
| `of(List<? extends T> values)`                       | List snapshot, own standard randomizer  |
| `of(List<? extends T> values, Randomizer randomizer)` | List snapshot, supplied randomizer    |

The implementation uses Fisher-Yates. For a list of size `n`, each shuffle requests `n - 1` indices,
with exclusive upper bounds `n`, `n - 1`, down to `2`. No random values are requested for lists of size zero or one.
The result shape and index ranges are validated; uniform shuffle probabilities depend on the randomizer's distribution.

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
| Null factory arguments or null list elements                                                    | `NullPointerException`     |
| Empty bounds or picker list; non-positive bound, count, or die face count                       | `IllegalArgumentException` |
| Supplied randomizer returns null, the wrong length, or an out-of-range value to a die, cup, coin, picker, or shuffler | `IllegalStateException`    |

Exceptions from supplied generators and randomizers propagate unchanged.
Dice cups, coins, pickers, and shufflers validate result shape and range; they do not test its statistical distribution.
Custom implementations are responsible for the probability guarantees in the interfaces.

## Deterministic tests

Use the interfaces to supply fixed results in tests of game logic:

```java
import java.util.List;
import ivs.games.accessories.randomizer.Randomizer;
import ivs.games.accessories.randomizer.dice.DiceCup;
import ivs.games.accessories.randomizer.dice.StandardDiceCup;
import ivs.games.accessories.randomizer.picker.StandardRandomPicker;
import ivs.games.accessories.randomizer.shuffle.StandardShuffler;

// Test double for a game that depends on DiceCup.
DiceCup fixedCup = () -> new int[]{3, 6};

// Test the cup's conversion using controlled zero-based indices.
Randomizer fixedIndices = () -> new int[]{2, 5};
var cup = StandardDiceCup.of(2, fixedIndices);
// cup.roll() returns [3, 6].

var picker = StandardRandomPicker.of(List.of("A", "B"), () -> new int[]{1});
// picker.pick() returns "B".

var shuffler = StandardShuffler.of(List.of("A", "B", "C"), () -> new int[]{0, 0});
// shuffler.shuffle() returns ["B", "C", "A"].
```

These fixed implementations are test doubles, rather than production sources of uniform random values.
The project's tests use controlled values, parameterized validation cases, and singleton ranges/lists
where default-generator results are deterministic. They do not rely on statistical assertions.

## Thread safety

The implementations do not synchronize calls to their random source. Bounds and stored list structure remain fixed;
integer generation and dice rolls return separate arrays, coins return enum values, and shufflers return separate lists.
Concurrent use depends on the supplied generator or randomizer. Callers are responsible for shared mutable elements as well.

A thread-safe generator does not make an entire multi-value generation atomic: concurrent calls can interleave,
and the values assigned to each session depend on call order. Different randomizer instances still share state
if they receive the same generator.

A practical arrangement is one randomizer and its own generator per session, with sequential processing
within that session. A dedicated permanent thread for each session is not required.

## License

[MIT](LICENSE), copyright 2026 Ihor Shaniuk.
