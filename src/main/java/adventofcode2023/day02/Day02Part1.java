package adventofcode2023.day02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 * --- Day 2: Cube Conundrum ---
 * 
 * You're launched high into the atmosphere! The apex of your trajectory just
 * barely reaches the surface of a large island floating in the sky. You gently
 * land in a fluffy pile of leaves. It's quite cold, but you don't see much
 * snow. An Elf runs over to greet you.
 * 
 * The Elf explains that you've arrived at Snow Island and apologizes for the
 * lack of snow. He'll be happy to explain the situation, but it's a bit of a
 * walk, so you have some time. They don't get many visitors up here; would you
 * like to play a game in the meantime?
 * 
 * As you walk, the Elf shows you a small bag and some cubes which are either
 * red, green, or blue. Each time you play this game, he will hide a secret
 * number of cubes of each color in the bag, and your goal is to figure out
 * information about the number of cubes.
 * 
 * To get information, once a bag has been loaded with cubes, the Elf will reach
 * into the bag, grab a handful of random cubes, show them to you, and then put
 * them back in the bag. He'll do this a few times per game.
 * 
 * You play several games and record the information from each game (your puzzle
 * input). Each game is listed with its ID number (like the 11 in Game 11: ...)
 * followed by a semicolon-separated list of subsets of cubes that were revealed
 * from the bag (like 3 red, 5 green, 4 blue).
 * 
 * For example, the record of a few games might look like this:
 * 
 * Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
 * Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
 * Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
 * Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
 * Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
 * In game 1, three sets of cubes are revealed from the bag (and then put back
 * again). The first set is 3 blue cubes and 4 red cubes; the second set is 1
 * red cube, 2 green cubes, and 6 blue cubes; the third set is only 2 green
 * cubes.
 * 
 * The Elf would first like to know which games would have been possible if the
 * bag contained only 12 red cubes, 13 green cubes, and 14 blue cubes?
 * 
 * In the example above, games 1, 2, and 5 would have been possible if the bag
 * had been loaded with that configuration. However, game 3 would have been
 * impossible because at one point the Elf showed you 20 red cubes at once;
 * similarly, game 4 would also have been impossible because the Elf showed you
 * 15 blue cubes at once. If you add up the IDs of the games that would have
 * been possible, you get 8.
 * 
 * Determine which games would have been possible if the bag had been loaded
 * with only 12 red cubes, 13 green cubes, and 14 blue cubes. What is the sum of
 * the IDs of those games?
 */
public class Day02Part1 {
    private static final Logger log = LoggerFactory.getLogger(Day02Part1.class);

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
        var bag = new Bag(Map.of(Color.red, 12, Color.green, 13, Color.blue, 14));

        System.out.println("Sum of possible games: " +
                input.map(Day02Part1::parse).filter(bag::isPossible).peek(System.out::println)
                        .collect(Collectors.summingInt(Game::number)));
    }
}
