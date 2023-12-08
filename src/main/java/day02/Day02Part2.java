package day02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Parser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Token;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.builder.SerializedParser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.semantics.ReflectSemantic;

/**
 * --- Part Two ---
 * 
 * The Elf says they've stopped producing snow because they aren't getting any
 * water! He isn't sure why the water stopped; however, he can show you how to
 * get to the water source to check it out for yourself. It's just up ahead!
 * 
 * As you continue your walk, the Elf poses a second question: in each game you
 * played, what is the fewest number of cubes of each color that could have been
 * in the bag to make the game possible?
 * 
 * Again consider the example games from earlier:
 * 
 * Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
 * Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
 * Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
 * Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
 * Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
 * 
 * In game 1, the game could have been played with as few as 4 red, 2 green, and
 * 6 blue cubes. If any color had even one fewer cube, the game would have been
 * impossible.
 * 
 * Game 2 could have been played with a minimum of 1 red, 3 green, and 4 blue
 * cubes.
 * 
 * Game 3 must have been played with at least 20 red, 13 green, and 6 blue
 * cubes.
 * 
 * Game 4 required at least 14 red, 3 green, and 15 blue cubes.
 * 
 * Game 5 needed no fewer than 6 red, 3 green, and 2 blue cubes in the bag.
 * 
 * The power of a set of cubes is equal to the numbers of red, green, and blue
 * cubes multiplied together. The power of the minimum set of cubes in game 1 is
 * 48. In games 2-5 it was 12, 1560, 630, and 36, respectively. Adding up these
 * five powers produces the sum 2286.
 * 
 * For each game, find the minimum set of cubes that must have been present.
 * What is the sum of the power of these sets?
 */
public class Day02Part2 {
    private static final Logger log = LoggerFactory.getLogger(Day02Part2.class);

    @SuppressWarnings({ "unchecked", "unused" })
    private static class AdventOfCodeParser extends ReflectSemantic {
        private static String[][] rules = {
                { "GAME", "'Game'", "COUNT", "':'", "TURNS" },
                { "TURNS", "CUBES" },
                { "TURNS", "CUBES", "';'", "TURNS" },
                { "CUBES", "CUBE" },
                { "CUBES", "CUBE", "','", "CUBES" },
                { "CUBE", "COUNT", "COLOR" },
                { "COUNT", "`integer`" },
                { "COLOR", "'red'" },
                { "COLOR", "'blue'" },
                { "COLOR", "'green'" },
                { Token.IGNORED, "`whitespaces`" },
        };

        public Object GAME(Object GAME, Object COUNT, Object COLON, Object TURNS) {
            log.debug("GAME COUNT ':' TURNS");
            var game = new Game((Integer) COUNT, (List<Turn>) TURNS);
            return game;
        }

        public Object TURNS(Object CUBES) {
            log.debug("TURNS: CUBES");
            Turn turn = new Turn((List<Cube>) CUBES);
            List<Turn> turns = new ArrayList<>();
            turns.add(turn);
            return turns;
        }

        public Object TURNS(Object CUBES, Object SEMICOLON, Object TURNS) {
            log.debug("TURNS: CUBES ';' TURNS");
            Turn turn = new Turn((List<Cube>) CUBES);
            var turns = (List<Turn>) TURNS;
            turns.add(turn);
            return turns;
        }

        public Object CUBES(Object CUBE) {
            log.debug("CUBES: CUBE");
            var result = new ArrayList<Cube>();
            result.add((Cube) CUBE);
            return result;
        }

        public Object CUBES(Object CUBE, Object COMMA, Object CUBES) {
            log.debug("CUBES: CUBE ',' CUBES");
            var list = (ArrayList<Cube>) CUBES;
            list.add((Cube) CUBE);
            return list;
        }

        public Object CUBE(Object COUNT, Object COLOR) {
            log.debug("CUBE: COUNT COLOR");
            return new Cube((Color) COLOR, (Integer) COUNT);
        }

        public Object COUNT(Object INTEGER) {
            log.debug("COUNT: `integer`");
            return Integer.valueOf((String) INTEGER);
        }

        public Object COLOR(Object COLOR) {
            log.debug("COLOR: 'red' | 'blue' |'green';");
            return Color.valueOf((String) COLOR);
        }

        public static Parser parser() throws Exception {
            var parser = new SerializedParser().get(AdventOfCodeParser.rules, "AdventOfCode2023");
            parser.setSemantic(new AdventOfCodeParser());
            return parser;
        }
    }

    enum Color {
        blue, green, red;
    }

    record Cube(Color color, int count) {
    }

    record Turn(List<Cube> cubes) {
    }

    record Game(int number, List<Turn> turns) {
        public int power() {
            Map<Color, Integer> maxes = new HashMap<>();
            for (var color : Color.values()) {
                int max = turns().stream().flatMap(t -> t.cubes().stream()).filter(c -> c.color() == color)
                        .map(Cube::count).max(Integer::compare).get();
                maxes.put(color, max);
            }
            int power = maxes.get(Color.red) * maxes.get(Color.green) * maxes.get(Color.blue);
            return power;
        }
    }

    record Bag(Map<Color, Integer> bagContents) {
        public boolean isPossible(Game game) {
            return game.turns().stream().allMatch(this::isPossible);
        }

        private boolean isPossible(Turn turn) {
            return turn.cubes().stream().allMatch(c -> c.count() <= bagContents.get(c.color()));
        }
    }

    private static Game parse(String line) {
        try {
            parser.parse(line);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return (Game) parser.getResult();
    }

    private static Parser parser;

    public static void main(String[] args) throws Exception {
        parser = AdventOfCodeParser.parser();
        var input = """
                Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
                Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
                Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
                Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
                Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
                """.lines();

        input = Files.readAllLines(Path.of("src/main/java/day02/input.txt")).stream();
        // 12 red cubes, 13 green cubes, and 14 blue cubes
        //var bag = new Bag(Map.of(Color.red, 12, Color.green, 13, Color.blue, 14));

        System.out.println("Sum of the powers: " +
                input.map(Day02Part2::parse)
                        .peek(g -> System.out.println(" - Game " + g.number() + ".power: " + g.power()))
                        .collect(Collectors.summingInt(Game::power)));
    }
}
