package adventofcode2023.day09;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * --- Part Two ---
 * 
 * Of course, it would be nice to have even more history included in your
 * report. Surely it's safe to just extrapolate backwards as well, right?
 * 
 * For each history, repeat the process of finding differences until the
 * sequence of differences is entirely zero. Then, rather than adding a zero to
 * the end and filling in the next values of each previous sequence, you should
 * instead add a zero to the beginning of your sequence of zeroes, then fill in
 * new first values for each previous sequence.
 * 
 * In particular, here is what the third example history looks like when
 * extrapolating back in time:
 * 
 * <pre>
 * 5  10  13  16  21  30  45
 *   5   3   3   5   9  15
 *    -2   0   2   4   6
 *       2   2   2   2
 *         0   0   0
 * </pre>
 * 
 * Adding the new values on the left side of each sequence from bottom to top
 * eventually reveals the new left-most history value: 5.
 * 
 * Doing this for the remaining example data above results in previous values of
 * -3 for the first history and 0 for the second history. Adding all three new
 * values together produces 2.
 * 
 * Analyze your OASIS report again, this time extrapolating the previous value
 * for each history. What is the sum of these extrapolated values?
 */
public class Day09Part2 {
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
            for (int i = lineResults.size() - 1; i >= 0; i--) {
                var updateLine = lineResults.get(i);
                int val = updateLine.getLast();
                int sum = prev.isEmpty() ? 0 : val + prev.getAsInt();
                updateLine.add(sum);
                prev = OptionalInt.of(sum);
            }

            do {
                var diffs = diffs(values);
                lineResults.add(diffs);
                level++;
                values = diffs;
            } while (!isZeros(values));

            prev = OptionalInt.empty();
            for (int i = lineResults.size() - 1; i >= 0; i--) {
                var updateLine = lineResults.get(i);
                int val = updateLine.getFirst();
                int sum = prev.isEmpty() ? 0 : val - prev.getAsInt();
                updateLine.addFirst(sum);
                prev = OptionalInt.of(sum);
            }

            for (int i = 0; i < lineResults.size(); i++)
                debugPrint(i, lineResults.get(i));

            System.out.println();
            results.add(lineResults.get(0).getFirst());
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
