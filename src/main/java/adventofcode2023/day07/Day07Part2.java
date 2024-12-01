package adventofcode2023.day07;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * --- Part Two ---
 * 
 * To make things a little more interesting, the Elf introduces one additional
 * rule. Now, J cards are jokers - wildcards that can act like whatever card
 * would make the hand the strongest type possible.
 * 
 * To balance this, J cards are now the weakest individual cards, weaker even
 * than 2. The other cards stay in the same order: A, K, Q, T, 9, 8, 7, 6, 5, 4,
 * 3, 2, J.
 * 
 * J cards can pretend to be whatever card is best for the purpose of
 * determining hand type; for example, QJJQ2 is now considered four of a kind.
 * However, for the purpose of breaking ties between two hands of the same type,
 * J is always treated as J, not the card it's pretending to be: JKKK2 is weaker
 * than QQQQ2 because J is weaker than Q.
 * 
 * Now, the above example goes very differently:
 * 
 * 32T3K 765
 * T55J5 684
 * KK677 28
 * KTJJT 220
 * QQQJA 483
 * 
 * 32T3K is still the only one pair; it doesn't contain any jokers, so its
 * strength doesn't increase.
 * 
 * KK677 is now the only two pair, making it the second-weakest hand.
 * 
 * T55J5, KTJJT, and QQQJA are now all four of a kind! T55J5 gets rank 3, QQQJA
 * gets rank 4, and KTJJT gets rank 5.
 * 
 * With the new joker rule, the total winnings in this example are 5905.
 * 
 * Using the new joker rule, find the rank of every hand in your set. What are
 * the new total winnings?
 */
public class Day07Part2 {
    enum Card {
        A("A"), K("K"), Q("Q"), T("T"), N9("9"), E8("8"), S7("7"), S6("6"), F5("5"), F4("4"), T3("3"), T2("2"), J("J"),;

        private String symbol;

        private Card(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }

        public static Card parse(String s) {
            return switch (s) {
                case "A" -> A;
                case "K" -> K;
                case "Q" -> Q;
                case "T" -> T;
                case "9" -> N9;
                case "8" -> E8;
                case "7" -> S7;
                case "6" -> S6;
                case "5" -> F5;
                case "4" -> F4;
                case "3" -> T3;
                case "2" -> T2;
                case "J" -> J;
                default -> throw new IllegalArgumentException("No enum constant for " + s);
            };
        }
    }

    enum Type {
        FiveOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                Optional<Card> firstNonJ = cards.stream().filter(c -> c != Card.J).findFirst();
                if (firstNonJ.isEmpty())
                    return true; // all Js

                return cards.stream().allMatch(c -> c == firstNonJ.get() || Card.J == c);
            }
        },
        FourOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                boolean fourAreEqual = false;
                int index = 0;
                while (!fourAreEqual && index < cards.size()) {
                    Card base = cards.get(index);
                    fourAreEqual = cards.stream().filter(c -> base == c || Card.J == c).count() == 4;
                    index++;
                }
                return fourAreEqual;
            }
        },
        FullHouse {
            @Override
            public boolean matches(List<Card> cards) {
                Map<Card, Integer> counts = new HashMap<>();
                for (Card card : cards) {
                    if (counts.get(card) == null)
                        counts.put(card, 0);
                    counts.put(card, counts.get(card) + 1);
                }

                Integer jsPresent = counts.get(Card.J);
                jsPresent = jsPresent == null ? 0 : jsPresent;

                boolean hasPair = false;
                boolean hasThree = false;
                for (Integer count : counts.entrySet().stream().filter(e -> e.getKey() != Card.J).map(e -> e.getValue())
                        .toList()) {
                    hasPair = hasPair || count == 2;
                    hasThree = hasThree || count == 3;
                }
                if (hasPair && hasThree)
                    return true;

                if (hasPair && jsPresent == 2)
                    return true;

                var doublePair = counts.values().stream().filter(c -> c == 2).count() == 2;
                return (doublePair && jsPresent == 1);
            }
        },
        ThreeOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                boolean threeAreEqual = false;
                int index = 0;
                while (!threeAreEqual && index < cards.size()) {
                    Card base = cards.get(index);
                    threeAreEqual = (cards.stream().filter(c -> c == base || c == Card.J).count() == 3);
                    index++;
                }
                return threeAreEqual;
            }
        },
        TwoPair {
            @Override
            public boolean matches(List<Card> cards) {
                boolean twoPair = false;
                int index = 0;
                Optional<Card> pair1 = Optional.empty();
                Optional<Card> pair2 = Optional.empty();
                while (!twoPair && index < cards.size()) {
                    Card card = cards.get(index++);
                    var cardIsPartOfPair = cards.stream().filter(card::equals).count() == 2;
                    if (cardIsPartOfPair) {
                        if (pair1.isEmpty()) {
                            pair1 = Optional.of(card);
                            continue;
                        } else if (pair1.get() == card) {
                            continue;
                        }
                        pair2 = Optional.of(card);
                    }
                    twoPair = pair1.isPresent() && pair2.isPresent();
                }
                return twoPair;
            }
        },
        OnePair {
            @Override
            public boolean matches(List<Card> cards) {
                boolean twoAreEqual = false;
                int index = 0;
                while (!twoAreEqual && index < cards.size()) {
                    Card base = cards.get(index);
                    twoAreEqual = (cards.stream().filter(c -> c == base || c == Card.J).count() == 2);
                    index++;
                }
                return twoAreEqual;
            }
        },
        HighCard {
            @Override
            public boolean matches(List<Card> cards) {
                return true;
            }
        };

        public int strength() {
            return values().length - ordinal();
        }

        public abstract boolean matches(List<Card> cards);
    }

    static class Hand implements Comparable<Hand> {
        private List<Card> cards;
        private long bid;
        private Type type;

        Hand(List<Card> cards, long bid) {
            this.cards = cards;
            this.bid = bid;
            this.type = EnumSet.allOf(Type.class).stream().filter(t -> t.matches(cards)).findFirst().get();
        }

        public List<Card> cards() {
            return cards;
        }

        public long bid() {
            return bid;
        }

        public Type type() {
            return type;
        }

        public int strength() {
            return type.strength();
        }

        public static Hand valueOf(String line) {
            String cardsText = line.substring(0, 5);
            String bid = line.substring(6);
            List<Card> cards = cardsText.codePoints()
                    .mapToObj(c -> String.valueOf((char) c)).map(Card::parse).toList();
            return new Hand(cards, Long.valueOf(bid));
        }

        @Override
        public int compareTo(Hand other) {
            int otherStrength = other.strength();
            int thisStrength = this.strength();
            if (thisStrength < otherStrength)
                return -1;
            if (thisStrength > otherStrength)
                return 1;

            for (int i = 0; i < cards().size(); i++) {
                int thisCard = cards().get(i).ordinal();
                int otherCard = other.cards().get(i).ordinal();
                if (thisCard < otherCard)
                    return 1;
                if (thisCard > otherCard)
                    return -1;
            }
            return 0;
        }

        @Override
        public String toString() {
            return "Hand[cards=" + cards + ",bid=" + bid + ",type=" + type + "]";
        }
    }

    public static void main(String[] args) throws Exception {
        var input = """
                32T3K 765
                T55J5 684
                KK677 28
                KTJJT 220
                QQQJA 483
                """.lines();

        input = Files.readString(Path.of("src/main/java/day07/input.txt")).lines();

        var rankedHands = input.map(Hand::valueOf).sorted().toList();
        rankedHands.forEach(System.out::println);
        var sum = 0;
        for (int i = 0; i < rankedHands.size(); i++) {
            sum += (i + 1) * rankedHands.get(i).bid();
        }
        System.out.println("Sum of winnings: " + sum);
    }
}
