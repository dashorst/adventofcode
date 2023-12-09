package day03;

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
 * --- Day 3: Gear Ratios ---
 * 
 * You and the Elf eventually reach a gondola lift station; he says the gondola
 * lift will take you up to the water source, but this is as far as he can bring
 * you. You go inside.
 * 
 * It doesn't take long to find the gondolas, but there seems to be a problem:
 * they're not moving.
 * 
 * "Aaah!"
 * 
 * You turn around to see a slightly-greasy Elf with a wrench and a look of
 * surprise. "Sorry, I wasn't expecting anyone! The gondola lift isn't working
 * right now; it'll still be a while before I can fix it." You offer to help.
 * 
 * The engineer explains that an engine part seems to be missing from the
 * engine, but nobody can figure out which one. If you can add up all the part
 * numbers in the engine schematic, it should be easy to work out which part is
 * missing.
 * 
 * The engine schematic (your puzzle input) consists of a visual representation
 * of the engine. There are lots of numbers and symbols you don't really
 * understand, but apparently any number adjacent to a symbol, even diagonally,
 * is a "part number" and should be included in your sum. (Periods (.) do not
 * count as a symbol.)
 * 
 * Here is an example engine schematic:
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
 * In this schematic, two numbers are not part numbers because they are not
 * adjacent to a symbol: 114 (top right) and 58 (middle right). Every other
 * number is adjacent to a symbol and so is a part number; their sum is 4361.
 * 
 * Of course, the actual engine schematic is much larger. What is the sum of all
 * of the part numbers in the engine schematic?
 */
public class Day03Part1 {
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

        Set<Pos> neighorsOfSymbols = symbols.stream().map(Symbol::position).flatMap(p -> p.neighbors().stream())
                .collect(Collectors.toSet());

        Set<Number> parts = new HashSet<>();
        for (Number number : numbers) {
            if (Collections.disjoint(neighorsOfSymbols, number.positions())) {
                continue;
            }
            parts.add(number);
        }
        printDebugInfo(numbers, symbols, parts);
        System.out.println("Sum of all partnumbers: " + parts.stream().collect(Collectors.summingInt(Number::value)));
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

    private static void printDebugInfo(List<Number> numbers, List<Symbol> symbols, Set<Number> parts) {
        for (int i = 0; i < Math.min(20, numbers.size()); i++) {
            Number number = numbers.get(i);
            if (parts.contains(number)) {
                var symbol = symbols.stream()
                        .filter(s -> s.position().neighbors().stream()
                                .anyMatch(n -> number.positions().contains(n)))
                        .findFirst().get();
                System.out.println("   positions: " + number.positions());
                System.out.println("   because " + symbol + " is adjecent");
                System.out.println("   neighbors: " + symbol.position().neighbors());
            }
        }
        System.out.println();
    }
}
