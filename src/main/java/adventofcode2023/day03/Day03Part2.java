package adventofcode2023.day03;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * --- Part Two ---
 * 
 * The engineer finds the missing part and installs it in the engine! As the
 * engine springs to life, you jump in the closest gondola, finally ready to
 * ascend to the water source.
 * 
 * You don't seem to be going very fast, though. Maybe something is still wrong?
 * Fortunately, the gondola has a phone labeled "help", so you pick it up and
 * the engineer answers.
 * 
 * Before you can explain the situation, she suggests that you look out the
 * window. There stands the engineer, holding a phone in one hand and waving
 * with the other. You're going so slowly that you haven't even left the
 * station. You exit the gondola.
 * 
 * The missing part wasn't the only issue - one of the gears in the engine is
 * wrong. A gear is any * symbol that is adjacent to exactly two part numbers.
 * Its gear ratio is the result of multiplying those two numbers together.
 * 
 * This time, you need to find the gear ratio of every gear and add them all up
 * so that the engineer can figure out which gear needs to be replaced.
 * 
 * Consider the same engine schematic again:
 * 
 * 467..114..
 * ...*......
 * ..35..633.
 * ......#...
 * 617*......
 * .....+.58.
 * ..592.....
 * ......755.
 * ...$.*....
 * .664.598..
 * 
 * In this schematic, there are two gears. The first is in the top left; it has
 * part numbers 467 and 35, so its gear ratio is 16345. The second gear is in
 * the lower right; its gear ratio is 451490. (The * adjacent to 617 is not a
 * gear because it is only adjacent to one part number.) Adding up all of the
 * gear ratios produces 467835.
 * 
 * What is the sum of all of the gear ratios in your engine schematic?
 */
public class Day03Part2 {
    record Pos(int row, int col) {
        public List<Pos> neighbors() {
            List<Pos> result = new ArrayList<>();
            for (int r = Math.max(0, row() - 1); r <= Math.min(maxRow, row() + 1); r++) {
                for (int c = Math.max(0, col() - 1); c <= Math.min(maxCol, col() + 1); c++) {
                    result.add(new Pos(r, c));
                }
            }
            result.remove(this);
            return result;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d)", row, col);
        }
    }

    record Gear(Number firstPart, Symbol symbol, Number secondPart) {
        public int gearRatio() {
            return firstPart.value() * secondPart().value();
        }
    }

    record Symbol(Character symbol, Pos position) {
        @Override
        public String toString() {
            return String.format("%c%s", symbol, position());
        }
    }

    record Number(int value, Pos firstDigit, Pos lastDigit) {
        public List<Pos> positions() {
            List<Pos> result = new ArrayList<>();
            int row = firstDigit().row();
            for (int c = firstDigit().col(); c <= lastDigit().col(); c++) {
                result.add(new Pos(row, c));
            }
            return result;
        }

        @Override
        public String toString() {
            return String.format("%d(%d,%d..%d)", value, firstDigit.row, firstDigit.col, lastDigit.col);
        }
    }

    static int maxRow;
    static int maxCol;

    public static void main(String[] args) throws IOException {
        var input = """
                467..114..
                ...*......
                ..35..633.
                ......#...
                617*......
                .....+.58.
                ..592.....
                ......755.
                ...$.*....
                .664.598..
                """;

        var lines = input.lines().toList();
        lines = Files.readAllLines(Path.of("src/main/java/day03/input.txt"));
        maxRow = lines.size();
        maxCol = lines.get(0).length();

        List<Number> numbers = getNumbers(lines);
        List<Symbol> symbols = getSymbols(lines);
        List<Symbol> gearSymbols = symbols.stream().filter(s -> s.symbol() == '*').collect(Collectors.toList());

        Set<Pos> neighorsOfGearSymbols = gearSymbols.stream().map(Symbol::position).flatMap(p -> p.neighbors().stream())
                .collect(Collectors.toSet());

        Set<Number> possibleGearParts = new HashSet<>();
        for (Number number : numbers) {
            if (Collections.disjoint(neighorsOfGearSymbols, number.positions())) {
                continue;
            }
            possibleGearParts.add(number);
        }

        Set<Gear> gears = new HashSet<>();
        for (var gearSymbol : gearSymbols) {
            var gearsOfSymbol = possibleGearParts.stream()
                    .filter(p -> !Collections.disjoint(p.positions(), gearSymbol.position.neighbors())).toList();
            // System.out.println("Symbol " + gearSymbol + " has these parts: " + gearsOfSymbol);
            if (gearsOfSymbol.size() == 2) {
                var gear = new Gear(gearsOfSymbol.get(0), gearSymbol, gearsOfSymbol.get(1)); 
                gears.add(gear);
                System.out.println("Gear found: " + gear);
            }
        }
        System.out
                .println("Sum of all gear ratios: " + gears.stream().collect(Collectors.summingInt(Gear::gearRatio)));
    }

    private static List<Number> getNumbers(List<String> lines) {
        Pattern numbersPattern = Pattern.compile("(\\d+)");
        List<Number> numbers = new ArrayList<>();
        for (int row = 0; row < lines.size(); row++) {
            var line = lines.get(row);
            var numbersMatcher = numbersPattern.matcher(line);
            while (numbersMatcher.find()) {
                var n = Integer.parseInt(numbersMatcher.group(1));
                var colStart = numbersMatcher.start();
                var colEnd = numbersMatcher.end() - 1;
                numbers.add(new Number(n, new Pos(row, colStart), new Pos(row, colEnd)));
            }
        }
        return numbers;
    }

    private static List<Symbol> getSymbols(List<String> lines) {
        List<Symbol> symbols = new ArrayList<>();
        Pattern symbolsPattern = Pattern.compile("([^\\d\\.\\n].)");
        for (int row = 0; row < lines.size(); row++) {
            var line = lines.get(row);
            var symbolsMatcher = symbolsPattern.matcher(line);
            while (symbolsMatcher.find()) {
                int col = symbolsMatcher.start();
                char value = line.charAt(col);
                symbols.add(new Symbol(value, new Pos(row, col)));
            }
        }
        return symbols;
    }
}
