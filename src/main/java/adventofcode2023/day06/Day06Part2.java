package adventofcode2023.day06;

import java.util.stream.LongStream;

/**
 * --- Part Two ---
 * 
 * As the race is about to start, you realize the piece of paper with race times
 * and record distances you got earlier actually just has very bad kerning.
 * There's really only one race - ignore the spaces between the numbers on each
 * line.
 * 
 * So, the example from before:
 * 
 * Time: 7 15 30
 * Distance: 9 40 200
 * ...now instead means this:
 * 
 * Time: 71530
 * Distance: 940200
 * 
 * Now, you have to figure out how many ways there are to win this single race.
 * In this example, the race lasts for 71530 milliseconds and the record
 * distance you need to beat is 940200 millimeters. You could hold the button
 * anywhere from 14 to 71516 milliseconds and beat the record, a total of 71503
 * ways!
 * 
 * How many ways can you beat the record in this one much longer race?
 */
public class Day06Part2 {
    record Race(long time, long maxDistance) {
        long wins() {
            return LongStream.range(0, time).parallel().filter(t -> {
                var speed = t;
                var timeLeft = time - t;
                var distanceTravelled = speed * timeLeft;
                return distanceTravelled > maxDistance();
            }).count();
        }
    }

    public static void main(String[] args) throws Exception {
        var test = new Race(71530L, 940200L);
        System.out.println("Result: " + test.wins());

        System.out.println();
        // Time: 49 78 79 80
        // Distance: 298 1185 1066 1181

        var prod = new Race(49787980L, 298118510661181L);
        System.out.println("Result: " + prod.wins());
    }
}
