package adventofcode2024;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class Day03 {
    public static void main(String[] args) throws Exception {
        System.out.printf("Day 03 - part 1, %-5s: sum: %d%n", "demo", part01("demo1.txt"));
        System.out.printf("Day 03 - part 1, %-5s: sum: %d%n", "input", part01("input.txt"));
        System.out.printf("Day 03 - part 2, %-5s: sum: %d%n", "demo", part02("demo2.txt"));
        System.out.printf("Day 03 - part 2, %-5s: sum: %d%n", "input", part02("input.txt"));
    }

    private static boolean debug = false;

    /**
     * --- Day 3: Mull It Over ---
     * 
     * "Our computers are having issues, so I have no idea if we have any Chief
     * Historians in stock! You're welcome to check the warehouse, though," says the
     * mildly flustered shopkeeper at the North Pole Toboggan Rental Shop. The
     * Historians head out to take a look.
     * 
     * The shopkeeper turns to you. "Any chance you can see why our computers are
     * having issues again?"
     * 
     * The computer appears to be trying to run a program, but its memory (your
     * puzzle input) is corrupted. All of the instructions have been jumbled up!
     * 
     * It seems like the goal of the program is just to multiply some numbers. It
     * does that with instructions like mul(X,Y), where X and Y are each 1-3 digit
     * numbers. For instance, mul(44,46) multiplies 44 by 46 to get a result of
     * 2024. Similarly, mul(123,4) would multiply 123 by 4.
     * 
     * However, because the program's memory has been corrupted, there are also many
     * invalid characters that should be ignored, even if they look like part of a
     * mul instruction. Sequences like mul(4*, mul(6,9!, ?(12,34), or mul ( 2 , 4 )
     * do nothing.
     * 
     * For example, consider the following section of corrupted memory:
     * 
     * xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
     * Only the four highlighted sections are real mul instructions. Adding up the
     * result of each instruction produces 161 (2*4 + 5*5 + 11*8 + 8*5).
     * 
     * Scan the corrupted memory for uncorrupted mul instructions. What do you get
     * if you add up all of the results of the multiplications?
     */
    private static int part01(String filename) throws Exception {
        var regex = Pattern.compile("(mul\\((\\d+)\\,(\\d+)\\))");

        var input = Files.readString(Path.of("src/main/resources/2024/03/" + filename));
        var matcher = regex.matcher(input);
        int pos = 0;
        int sum = 0;
        while (matcher.find(pos)) {
            String group = matcher.group();
            int number1 = Integer.parseInt(matcher.group(2));
            int number2 = Integer.parseInt(matcher.group(3));
            if (debug)
                System.out.println("  - " + group + ": " + (number1 + "*" + number2 + "=" + number1 * number2));
            sum += number1 * number2;
            pos = matcher.end();
        }
        return sum;
    }

    /**
     * --- Part Two ---
     * 
     * As you scan through the corrupted memory, you notice that some of the
     * conditional statements are also still intact. If you handle some of the
     * uncorrupted conditional statements in the program, you might be able to get
     * an even more accurate result.
     * 
     * There are two new instructions you'll need to handle:
     * 
     * The do() instruction enables future mul instructions.
     * The don't() instruction disables future mul instructions.
     * Only the most recent do() or don't() instruction applies. At the beginning of
     * the program, mul instructions are enabled.
     * 
     * For example:
     * 
     * xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
     * This corrupted memory is similar to the example from before, but this time
     * the mul(5,5) and mul(11,8) instructions are disabled because there is a
     * don't() instruction before them. The other mul instructions function
     * normally, including the one at the end that gets re-enabled by a do()
     * instruction.
     * 
     * This time, the sum of the results is 48 (2*4 + 8*5).
     * 
     * Handle the new instructions; what do you get if you add up all of the results
     * of just the enabled multiplications?
     */
    private static int part02(String filename) throws IOException {
        var input = Files.readString(Path.of("src/main/resources/2024/03/" + filename));
        var regex = Pattern.compile("(do|don't|mul)\\(((\\d+)\\,(\\d+))?\\)");

        var matcher = regex.matcher(input);
        int pos = 0;
        int sum = 0;
        boolean activated = true;
        while (matcher.find(pos)) {
            String group = matcher.group();
            String verb = matcher.group(1);
            if ("do".equals(verb)) {
                activated = true;
                if (debug)
                    System.out.println("  - activated");
            }
            if ("don't".equals(verb)) {
                activated = false;
                if (debug)
                    System.out.println("  - deactivated");
            }
            if ("mul".equals(verb) && activated) {
                int number1 = Integer.parseInt(matcher.group(3));
                int number2 = Integer.parseInt(matcher.group(4));
                if (debug)
                    System.out.println("  - " + group + ": " + (number1 + "*" + number2 + "=" + number1 * number2));
                sum += number1 * number2;
            }
            pos = matcher.end();
        }
        return sum;
    }
}
