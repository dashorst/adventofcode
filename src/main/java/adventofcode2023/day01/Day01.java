package adventofcode2023.day01;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * You try to ask why they can't just use a weather machine ("not powerful
 * enough") and where they're even sending you ("the sky") and why your map
 * looks mostly blank ("you sure ask a lot of questions") and hang on did you
 * just say the sky ("of course, where do you think snow comes from") when you
 * realize that the Elves are already loading you into a trebuchet ("please hold
 * still, we need to strap you in").
 * 
 * As they're making the final adjustments, they discover that their calibration
 * document (your puzzle input) has been amended by a very young Elf who was
 * apparently just excited to show off her art skills. Consequently, the Elves
 * are having trouble reading the values on the document.
 * 
 * The newly-improved calibration document consists of lines of text; each line
 * originally contained a specific calibration value that the Elves now need to
 * recover. On each line, the calibration value can be found by combining the
 * first digit and the last digit (in that order) to form a single two-digit
 * number.
 * 
 * For example:
 * 
 * 1abc2
 * pqr3stu8vwx
 * a1b2c3d4e5f
 * treb7uchet
 * 
 * In this example, the calibration values of these four lines are 12, 38, 15,
 * and 77. Adding these together produces 142.
 * 
 * Consider your entire calibration document. What is the sum of all of the
 * calibration values?
 */
public class Day01 {
    public static void main(String args[]) throws IOException {
        var inputs = Files.readAllLines(Path.of("src/main/java/day01/input.txt"));
        // var inputs = Arrays.asList("1abc2", "pqr3stu8vwx", "a1b2c3d4e5f", "treb7uchet");
        // var inputs = Arrays.asList("two1nine", "eightwothree", "abcone2threexyz", "xtwone3four", "4nineeightseven2", "zoneight234", "7pqrstsixteen");
        System.out.println(
                inputs.stream().peek(i -> System.out.print(i + "\t")).map(input -> callibratePartTwo(input)).peek(System.out::println)
                        .collect(Collectors.summingInt(Integer::valueOf)));
    }

    private static String callibratePartTwo(String input) {
        var digits = new HashMap<String, String>();
        digits.put("one", "1");
        digits.put("two", "2");
        digits.put("three", "3");
        digits.put("four", "4");
        digits.put("five", "5");
        digits.put("six", "6");
        digits.put("seven", "7");
        digits.put("eight", "8");
        digits.put("nine", "9");
        // digits.put("0", "0");
        digits.put("1", "1");
        digits.put("2", "2");
        digits.put("3", "3");
        digits.put("4", "4");
        digits.put("5", "5");
        digits.put("6", "6");
        digits.put("7", "7");
        digits.put("8", "8");
        digits.put("9", "9");

        String first = "", last = "";

        first = firstDigit(input, digits);
        last = lastDigit(input, digits);

        return first + last;
    }

    private static String firstDigit(String input, HashMap<String, String> digits) {
        String result = "";
        int index = input.length();
        for(var keyValue : digits.entrySet()) {
            var pos = input.indexOf(keyValue.getKey());
            if(pos >= 0 && pos < index) {
                index = pos;
                result = keyValue.getValue();
            }
        }
        return result;
    }

    private static String lastDigit(String input, HashMap<String, String> digits) {
        String result = "";
        int index = -1;
        for(var keyValue : digits.entrySet()) {
            var pos = input.lastIndexOf(keyValue.getKey());
            if(pos >= 0 && pos > index) {
                index = pos;
                result = keyValue.getValue();
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static String callibratePartOne(String input) {
        String first = "", last = "";
        for (int i = 0; i < input.length(); i++) {
            char possibleDigit = input.charAt(i);
            if (Character.isDigit(possibleDigit)) {
                first = Character.toString(possibleDigit);
                break;
            }
        }
        for (int i = input.length() - 1; i >= 0; i--) {
            char possibleDigit = input.charAt(i);
            if (Character.isDigit(possibleDigit)) {
                last = Character.toString(possibleDigit);
                break;
            }
        }
        return first + last;
    }
}
