package adventofcode2023.day07;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * --- Day 7: Camel Cards ---
 * 
 * Your all-expenses-paid trip turns out to be a one-way, five-minute ride in an
 * airship. (At least it's a cool airship!) It drops you off at the edge of a
 * vast desert and descends back to Island Island.
 * 
 * "Did you bring the parts?"
 * 
 * You turn around to see an Elf completely covered in white clothing, wearing
 * goggles, and riding a large camel.
 * 
 * "Did you bring the parts?" she asks again, louder this time. You aren't sure
 * what parts she's looking for; you're here to figure out why the sand stopped.
 * 
 * "The parts! For the sand, yes! Come with me; I will show you." She beckons
 * you onto the camel.
 * 
 * After riding a bit across the sands of Desert Island, you can see what look
 * like very large rocks covering half of the horizon. The Elf explains that the
 * rocks are all along the part of Desert Island that is directly above Island
 * Island, making it hard to even get there. Normally, they use big machines to
 * move the rocks and filter the sand, but the machines have broken down because
 * Desert Island recently stopped receiving the parts they need to fix the
 * machines.
 * 
 * You've already assumed it'll be your job to figure out why the parts stopped
 * when she asks if you can help. You agree automatically.
 * 
 * Because the journey will take a few days, she offers to teach you the game of
 * Camel Cards. Camel Cards is sort of similar to poker except it's designed to
 * be easier to play while riding a camel.
 * 
 * In Camel Cards, you get a list of hands, and your goal is to order them based
 * on the strength of each hand. A hand consists of five cards labeled one of A,
 * K, Q, J, T, 9, 8, 7, 6, 5, 4, 3, or 2. The relative strength of each card
 * follows this order, where A is the highest and 2 is the lowest.
 * 
 * Every hand is exactly one type. From strongest to weakest, they are:
 * 
 * Five of a kind, where all five cards have the same label: AAAAA
 * 
 * Four of a kind, where four cards have the same label and one card has a
 * different label: AA8AA
 * 
 * Full house, where three cards have the same label, and the remaining two
 * cards share a different label: 23332
 * 
 * Three of a kind, where three cards have the same label, and the remaining two
 * cards are each different from any other card in the hand: TTT98
 * 
 * Two pair, where two cards share one label, two other cards share a second
 * label, and the remaining card has a third label: 23432
 * 
 * One pair, where two cards share one label, and the other three cards have a
 * different label from the pair and each other: A23A4
 * 
 * High card, where all cards' labels are distinct: 23456
 * 
 * Hands are primarily ordered based on type; for example, every full house is
 * stronger than any three of a kind.
 * 
 * If two hands have the same type, a second ordering rule takes effect. Start
 * by comparing the first card in each hand. If these cards are different, the
 * hand with the stronger first card is considered stronger. If the first card
 * in each hand have the same label, however, then move on to considering the
 * second card in each hand. If they differ, the hand with the higher second
 * card wins; otherwise, continue with the third card in each hand, then the
 * fourth, then the fifth.
 * 
 * So, 33332 and 2AAAA are both four of a kind hands, but 33332 is stronger
 * because its first card is stronger. Similarly, 77888 and 77788 are both a
 * full house, but 77888 is stronger because its third card is stronger (and
 * both hands have the same first and second card).
 * 
 * To play Camel Cards, you are given a list of hands and their corresponding
 * bid (your puzzle input). For example:
 * 
 * 32T3K 765
 * T55J5 684
 * KK677 28
 * KTJJT 220
 * QQQJA 483
 * 
 * This example shows five hands; each hand is followed by its bid amount. Each
 * hand wins an amount equal to its bid multiplied by its rank, where the
 * weakest hand gets rank 1, the second-weakest hand gets rank 2, and so on up
 * to the strongest hand. Because there are five hands in this example, the
 * strongest hand will have rank 5 and its bid will be multiplied by 5.
 * 
 * So, the first step is to put the hands in order of strength:
 * 
 * 32T3K is the only one pair and the other hands are all a stronger type, so it
 * gets rank 1.
 * 
 * KK677 and KTJJT are both two pair. Their first cards both have the same
 * label, but the second card of KK677 is stronger (K vs T), so KTJJT gets rank
 * 2 and KK677 gets rank 3.
 * 
 * T55J5 and QQQJA are both three of a kind. QQQJA has a stronger first card, so
 * it gets rank 5 and T55J5 gets rank 4.
 * 
 * Now, you can determine the total winnings of this set of hands by adding up
 * the result of multiplying each hand's bid with its rank (765 * 1 + 220 * 2 +
 * 28 * 3 + 684 * 4 + 483 * 5). So the total winnings in this example are 6440.
 * 
 * Find the rank of every hand in your set. What are the total winnings?
 */
public class Day07Part1 {
    enum Card {
        A("A"), K("K"), Q("Q"), J("J"), T("T"), N9("9"), E8("8"), S7("7"), S6("6"), F5("5"), F4("4"), T3("3"), T2("2");

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
                case "J" -> J;
                case "T" -> T;
                case "9" -> N9;
                case "8" -> E8;
                case "7" -> S7;
                case "6" -> S6;
                case "5" -> F5;
                case "4" -> F4;
                case "3" -> T3;
                case "2" -> T2;
                default -> throw new IllegalArgumentException("No enum constant for " + s);
            };
        }
    }

    enum Type {
        FiveOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                return cards.stream().allMatch(c -> c.equals(cards.get(0)));
            }
        },
        FourOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                boolean fourAreEqual = false;
                int index = 0;
                while (!fourAreEqual && index < cards.size()) {
                    Card base = cards.get(index);
                    fourAreEqual = cards.stream().filter(base::equals).count() == 4;
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
                boolean hasPair = false;
                boolean hasThree = false;
                for (Integer count : counts.values()) {
                    hasPair = hasPair || count == 2;
                    hasThree = hasThree || count == 3;
                }
                return hasPair && hasThree;
            }
        },
        ThreeOfAKind {
            @Override
            public boolean matches(List<Card> cards) {
                boolean threeAreEqual = false;
                int index = 0;
                while (!threeAreEqual && index < cards.size()) {
                    Card base = cards.get(index);
                    threeAreEqual = (cards.stream().filter(base::equals).count() == 3);
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
                    twoAreEqual = (cards.stream().filter(base::equals).count() == 2);
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
