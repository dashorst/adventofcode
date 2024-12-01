package adventofcode2023.day09;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * --- Day 9: Mirage Maintenance ---
 * 
 * You ride the camel through the sandstorm and stop where the ghost's maps told
 * you to stop. The sandstorm subsequently subsides, somehow seeing you standing
 * at an oasis!
 * 
 * The camel goes to get some water and you stretch your neck. As you look up,
 * you discover what must be yet another giant floating island, this one made of
 * metal! That must be where the parts to fix the sand machines come from.
 * 
 * There's even a hang glider partially buried in the sand here; once the sun
 * rises and heats up the sand, you might be able to use the glider and the hot
 * air to get all the way up to the metal island!
 * 
 * While you wait for the sun to rise, you admire the oasis hidden here in the
 * middle of Desert Island. It must have a delicate ecosystem; you might as well
 * take some ecological readings while you wait. Maybe you can report any
 * environmental instabilities you find to someone so the oasis can be around
 * for the next sandstorm-worn traveler.
 * 
 * You pull out your handy Oasis And Sand Instability Sensor and analyze your
 * surroundings. The OASIS produces a report of many values and how they are
 * changing over time (your puzzle input). Each line in the report contains the
 * history of a single value. For example:
 * 
 * <pre>
 * 0 3 6 9 12 15
 * 1 3 6 10 15 21
 * 10 13 16 21 30 45
 * </pre>
 *
 * To best protect the oasis, your environmental report should include a
 * prediction of the next value in each history. To do this, start by making a
 * new sequence from the difference at each step of your history. If that
 * sequence is not all zeroes, repeat this process, using the sequence you just
 * generated as the input sequence. Once all of the values in your latest
 * sequence are zeroes, you can extrapolate what the next value of the original
 * history should be.
 * 
 * In the above dataset, the first history is 0 3 6 9 12 15. Because the values
 * increase by 3 each step, the first sequence of differences that you generate
 * will be 3 3 3 3 3. Note that this sequence has one fewer value than the input
 * sequence because at each step it considers two numbers from the input. Since
 * these values aren't all zero, repeat the process: the values differ by 0 at
 * each step, so the next sequence is 0 0 0 0. This means you have enough
 * information to extrapolate the history! Visually, these sequences can be
 * arranged like this:
 * 
 * <pre>
 * 0   3   6   9  12  15
 *   3   3   3   3   3
 *     0   0   0   0
 * </pre>
 * 
 * To extrapolate, start by adding a new zero to the end of your list of zeroes;
 * because the zeroes represent differences between the two values above them,
 * this also means there is now a placeholder in every sequence above it:
 * 
 * <pre>
 * 0 3 6 9 12 15 B
 *  3 3 3 3 3 A
 *   0 0 0 0 0
 * </pre>
 * 
 * You can then start filling in placeholders from the bottom up. A needs to be
 * the result of increasing 3 (the value to its left) by 0 (the value below it);
 * this means A must be 3:
 * 
 * <pre>
 * 0   3   6   9  12  15   B
 *   3   3   3   3   3   3
 *     0   0   0   0   0
 * </pre>
 * 
 * Finally, you can fill in B, which needs to be the result of increasing 15
 * (the value to its left) by 3 (the value below it), or 18:
 * 
 * <pre>
 * 0   3   6   9  12  15  18
 *   3   3   3   3   3   3
 *     0   0   0   0   0
 * </pre>
 * 
 * So, the next value of the first history is 18.
 * 
 * Finding all-zero differences for the second history requires an additional
 * sequence:
 * 
 * <pre>
 * 1   3   6  10  15  21
 *   2   3   4   5   6
 *     1   1   1   1
 *       0   0   0
 * </pre>
 * 
 * Then, following the same process as before, work out the next value in each
 * sequence from the bottom up:
 * 
 * <pre>
 * 1   3   6  10  15  21  28
 *   2   3   4   5   6   7
 *     1   1   1   1   1
 *       0   0   0   0
 * </pre>
 * 
 * So, the next value of the second history is 28.
 * 
 * The third history requires even more sequences, but its next value can be
 * found the same way:
 * 
 * <pre>
 * 10  13  16  21  30  45  68
 *    3   3   5   9  15  23
 *      0   2   4   6   8
 *        2   2   2   2
 *          0   0   0
 * </pre>
 * 
 * So, the next value of the third history is 68.
 * 
 * If you find the next value for each history in this example and add them
 * together, you get 114.
 * 
 * Analyze your OASIS report and extrapolate the next value for each history.
 * What is the sum of these extrapolated values?
 */
public class Day09Part1 {
    public static void main(String[] args) throws IOException {
        var input1 = """
                0 3 6 9 12 15
                1 3 6 10 15 21
                10 13 16 21 30 45
                """.lines().toList();
        var input2 = Files.readAllLines(Path.of("src/main/java/day09/input.txt"));

        var results = new ArrayList<Integer>();
        for (String line : input2) {
            var values = mapToInts(line);
            int level = 0;
            List<List<Integer>> lineResults = new ArrayList<>();
            lineResults.add(values);
            do {
                var diffs = diffs(values);
                lineResults.add(diffs);
                level++;
                values = diffs;
            } while (!isZeros(values));

            OptionalInt prev = OptionalInt.empty();
            for(int i = lineResults.size() - 1; i >= 0; i--) {
                var updateLine = lineResults.get(i);
                int val = updateLine.getLast();
                int sum = prev.isEmpty() ? 0 : val + prev.getAsInt();
                updateLine.add(sum);
                prev = OptionalInt.of(sum);
            }
            for (int i = 0; i < lineResults.size(); i++)
                debugPrint(i, lineResults.get(i));
            System.out.println();
            results.add(lineResults.get(0).getLast());
        }
        System.out.println(results + " -> " + results.stream().mapToInt(i -> i).sum());
    }

    private static List<Integer> mapToInts(String line) {
        ArrayList<Integer> result = new ArrayList<>();
        Stream.of(line.split(" ")).map(Integer::valueOf).forEach(result::add);
        return result;
    }

    private static List<Integer> diffs(List<Integer> values) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0, j = 1; i < values.size() - 1 && j < values.size(); i++, j++) {
            result.add(values.get(j) - values.get(i));
        }
        return result;
    }

    private static void debugPrint(int level, List<Integer> values) {
        System.out.print("     ".repeat(level));
        for (int i = 0; i < values.size(); i++)
            System.out.printf("%-10d", values.get(i));
        System.out.println();
    }

    private static boolean isZeros(List<Integer> values) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != 0)
                return false;
        }
        return true;
    }
}
