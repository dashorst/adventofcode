package day05;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Parser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Token;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.builder.SerializedParser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.semantics.ReflectSemantic;

/**
 * --- Day 5: If You Give A Seed A Fertilizer ---
 * 
 * You take the boat and find the gardener right where you were told he would
 * be: managing a giant "garden" that looks more to you like a farm.
 * 
 * "A water source? Island Island is the water source!" You point out that Snow
 * Island isn't receiving any water.
 * 
 * "Oh, we had to stop the water because we ran out of sand to filter it with!
 * Can't make snow with dirty water. Don't worry, I'm sure we'll get more sand
 * soon; we only turned off the water a few days... weeks... oh no." His face
 * sinks into a look of horrified realization.
 * 
 * "I've been so busy making sure everyone here has food that I completely
 * forgot to check why we stopped getting more sand! There's a ferry leaving
 * soon that is headed over in that direction - it's much faster than your boat.
 * Could you please go check it out?"
 * 
 * You barely have time to agree to this request when he brings up another.
 * "While you wait for the ferry, maybe you can help us with our food production
 * problem. The latest Island Island Almanac just arrived and we're having
 * trouble making sense of it."
 * 
 * The almanac (your puzzle input) lists all of the seeds that need to be
 * planted. It also lists what type of soil to use with each kind of seed, what
 * type of fertilizer to use with each kind of soil, what type of water to use
 * with each kind of fertilizer, and so on. Every type of seed, soil, fertilizer
 * and so on is identified with a number, but numbers are reused by each
 * category - that is, soil 123 and fertilizer 123 aren't necessarily related to
 * each other.
 * 
 * For example:
 * 
 * seeds: 79 14 55 13
 * 
 * seed-to-soil map:
 * 50 98 2
 * 52 50 48
 * 
 * soil-to-fertilizer map:
 * 0 15 37
 * 37 52 2
 * 39 0 15
 * 
 * fertilizer-to-water map:
 * 49 53 8
 * 0 11 42
 * 42 0 7
 * 57 7 4
 * 
 * water-to-light map:
 * 88 18 7
 * 18 25 70
 * 
 * light-to-temperature map:
 * 45 77 23
 * 81 45 19
 * 68 64 13
 * 
 * temperature-to-humidity map:
 * 0 69 1
 * 1 0 69
 * 
 * humidity-to-location map:
 * 60 56 37
 * 56 93 4
 * The almanac starts by listing which seeds need to be planted: seeds 79, 14,
 * 55, and 13.
 * 
 * The rest of the almanac contains a list of maps which describe how to convert
 * numbers from a source category into numbers in a destination category. That
 * is, the section that starts with seed-to-soil map: describes how to convert a
 * seed number (the source) to a soil number (the destination). This lets the
 * gardener and his team know which soil to use with which seeds, which water to
 * use with which fertilizer, and so on.
 * 
 * Rather than list every source number and its corresponding destination number
 * one by one, the maps describe entire ranges of numbers that can be converted.
 * Each line within a map contains three numbers: the destination range start,
 * the source range start, and the range length.
 * 
 * Consider again the example seed-to-soil map:
 * 
 * 50 98 2
 * 52 50 48
 * The first line has a destination range start of 50, a source range start of
 * 98, and a range length of 2. This line means that the source range starts at
 * 98 and contains two values: 98 and 99. The destination range is the same
 * length, but it starts at 50, so its two values are 50 and 51. With this
 * information, you know that seed number 98 corresponds to soil number 50 and
 * that seed number 99 corresponds to soil number 51.
 * 
 * The second line means that the source range starts at 50 and contains 48
 * values: 50, 51, ..., 96, 97. This corresponds to a destination range starting
 * at 52 and also containing 48 values: 52, 53, ..., 98, 99. So, seed number 53
 * corresponds to soil number 55.
 * 
 * Any source numbers that aren't mapped correspond to the same destination
 * number. So, seed number 10 corresponds to soil number 10.
 * 
 * So, the entire list of seed numbers and their corresponding soil numbers
 * looks like this:
 * 
 * seed soil
 * 0 0
 * 1 1
 * ... ...
 * 48 48
 * 49 49
 * 50 52
 * 51 53
 * ... ...
 * 96 98
 * 97 99
 * 98 50
 * 99 51
 * With this map, you can look up the soil number required for each initial seed
 * number:
 * 
 * Seed number 79 corresponds to soil number 81.
 * Seed number 14 corresponds to soil number 14.
 * Seed number 55 corresponds to soil number 57.
 * Seed number 13 corresponds to soil number 13.
 * The gardener and his team want to get started as soon as possible, so they'd
 * like to know the closest location that needs a seed. Using these maps, find
 * the lowest location number that corresponds to any of the initial seeds. To
 * do this, you'll need to convert each seed number through other categories
 * until you can find its corresponding location number. In this example, the
 * corresponding types are:
 * 
 * Seed 79, soil 81, fertilizer 81, water 81, light 74, temperature 78, humidity
 * 78, location 82.
 * Seed 14, soil 14, fertilizer 53, water 49, light 42, temperature 42, humidity
 * 43, location 43.
 * Seed 55, soil 57, fertilizer 57, water 53, light 46, temperature 82, humidity
 * 82, location 86.
 * Seed 13, soil 13, fertilizer 52, water 41, light 34, temperature 34, humidity
 * 35, location 35.
 * So, the lowest location number in this example is 35.
 * 
 * What is the lowest location number that corresponds to any of the initial
 * seed numbers?
 */
public class Day05Part1 {
    private static final String DEMO_INPUT = """
            seeds: 79 14 55 13

            seed-to-soil map:
            50 98 2
            52 50 48

            soil-to-fertilizer map:
            0 15 37
            37 52 2
            39 0 15

            fertilizer-to-water map:
            49 53 8
            0 11 42
            42 0 7
            57 7 4

            water-to-light map:
            88 18 7
            18 25 70

            light-to-temperature map:
            45 77 23
            81 45 19
            68 64 13

            temperature-to-humidity map:
            0 69 1
            1 0 69

            humidity-to-location map:
            60 56 37
            56 93 4
                """;
    static String[][] grammar = {
            { "ALMANAC", "SEEDS", "MAPS" },
            { "SEEDS", "'seeds'", "':'", "NUMBERS" },
            { "MAPS", "MAP" },
            { "MAPS", "MAP", "MAPS" },
            { "MAP", "MAPPINGNAME", "':'", "MAPPINGS" },
            { "MAPPINGNAME", "FROM", "'-to-'", "TO", "'map'" },
            { "FROM", "NAME" },
            { "TO", "NAME" },
            { "MAPPINGS", "MAPPING" },
            { "MAPPINGS", "MAPPING", "MAPPINGS" },
            { "MAPPING", "NUMBER", "NUMBER", "NUMBER" },
            { "NAME", "'seed'" },
            { "NAME", "'soil'" },
            { "NAME", "'fertilizer'" },
            { "NAME", "'water'" },
            { "NAME", "'light'" },
            { "NAME", "'temperature'" },
            { "NAME", "'humidity'" },
            { "NAME", "'location'" },
            { "NUMBERS", "NUMBER" },
            { "NUMBERS", "NUMBER", "NUMBERS" },
            { "NUMBER", "`integer`" },
            { Token.IGNORED, "`whitespaces`" },
    };

    record Almanac(List<Long> seeds, Mapping seedsMapping, Map<Mapping, List<MappingRange>> mappings) {
        public Mapping getMapping(String from) {
            return mappings.keySet().stream().filter(m -> m.from().equals(from)).findFirst().get();
        }

        public long lowestLocationForAllSeeds() {
            var lowest = seeds.stream()
                    .peek(s -> System.out.print("Location for seed " + s + " is "))
                    .map(seedsMapping::location)
                    .peek(l -> System.out.println(": " + l))
                    .min(Long::compare).get();
            return lowest;
        }
    }

    record Mapping(AtomicReference<Almanac> almanac, String from, String to) {
        public long location(long src) {
            Almanac almanac = almanac().get();
            if (!"location".equals(to())) {
                var destValue = getDestValue(src);
                var location = almanac.getMapping(to()).location(destValue);
                return location;
            }
            return getDestValue(src);
        }

        private List<MappingRange> ranges() {
            return almanac().get().mappings().get(this);
        }

        private long getDestValue(long src) {
            for (MappingRange range : ranges()) {
                if (range.isInRange(src)) {
                    return range.get(src);
                }
            }
            return src;
        }

        @Override
        public String toString() {
            return String.format("Mapping[from=%s,to=%s]", from, to);
        }
    }

    record MappingRange(long destRange, long srcRange, long range) {
        boolean isInRange(long src) {
            return src >= srcRange && src <= (srcRange + range);
        }

        long get(long src) {
            if (isInRange(src)) {
                return destRange + (src - srcRange);
            }
            return src;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static class Semantic extends ReflectSemantic {
        // { "ALMANAC", "SEEDS", "MAP" },
        public Object ALMANAC(Object SEEDS, Object MAP) {
            var seedsMapping = ((Map<Mapping, List<?>>) MAP).keySet().stream().filter(m -> m.from().equals("seed"))
                    .findFirst().get();
            var almanac = new Almanac((List<Long>) SEEDS, seedsMapping, (Map) MAP);
            almanac.mappings().keySet().forEach(m -> m.almanac().set(almanac));
            return almanac;
        }

        // { "SEEDS", "'seeds'", "':'", "NUMBERS" },
        public Object SEEDS(Object TOKEN, Object COLON, Object NUMBERS) {
            System.out.println(" > SEEDS: " + NUMBERS);
            return NUMBERS;
        }

        // { "NUMBERS", "NUMBER" },
        public Object NUMBERS(Object NUMBER) {
            List<Long> result = new ArrayList<>();
            result.add((Long) NUMBER);
            return result;
        }

        // { "NUMBERS", "NUMBER", "NUMBERS" },
        public Object NUMBERS(Object NUMBER, Object NUMBERS) {
            ((List) NUMBERS).add(NUMBER);
            return NUMBERS;
        }

        // { "MAPS", "MAP" },
        public Object MAPS(Object MAP) {
            return MAP;
        }

        // { "MAPS", "MAP", "MAPS" },
        public Object MAPS(Object MAP, Object MAPS) {
            Map<Mapping, List<MappingRange>> maps = (Map<Mapping, List<MappingRange>>) MAPS;
            Map<Mapping, List<MappingRange>> map = (Map<Mapping, List<MappingRange>>) MAP;
            var key = map.keySet().iterator().next();
            var list = maps.get(key);
            if (list == null) {
                maps.put(key, map.get(key));
            } else {
                list.addAll(map.get(key));
            }
            return maps;
        }

        // { "MAP", "MAPPINGNAME", "':'", "MAPPINGS"},
        public Object MAP(Object MAPPINGNAME, Object COLON, Object MAPPINGS) {
            Map<Mapping, List<MappingRange>> MAP = new LinkedHashMap<>();
            MAP.put((Mapping) MAPPINGNAME, (List<MappingRange>) MAPPINGS);
            return MAP;
        }

        // { "MAPPINGNAME", "FROM", "'-to-'", "TO", "'map'" },
        public Object MAPPINGNAME(Object FROM, Object TOTOKEN, Object TO, Object MAPTOKEN) {
            return new Mapping(new AtomicReference<>(), (String) FROM, (String) TO);
        }

        // { "FROM", "NAME" },
        public Object FROM(Object NAME) {
            return NAME;
        }

        // { "TO", "NAME" },
        public Object TO(Object NAME) {
            return NAME;
        }

        // { "MAPPINGS", "MAPPING" },
        public Object MAPPINGS(Object MAPPING) {
            List<MappingRange> ranges = new ArrayList<>();
            ranges.add((MappingRange) MAPPING);
            return ranges;
        }

        // { "MAPPINGS", "MAPPING", "MAPPINGS" },
        public Object MAPPINGS(Object MAPPING, Object MAPPINGS) {
            List<MappingRange> ranges = (List<MappingRange>) MAPPINGS;
            ranges.add((MappingRange) MAPPING);
            return ranges;
        }

        // { "MAPPING", "NUMBER", "NUMBER", "NUMBER" },
        public Object MAPPING(Object DEST, Object SRC, Object RANGE) {
            return new MappingRange(Long.valueOf(DEST.toString()), Long.valueOf(SRC.toString()),
                    Long.valueOf(RANGE.toString()));
        }

        // { "NAME", "'seed'" },
        // { "NAME", "'soil'" },
        // { "NAME", "'fertilizer'" },
        // { "NAME", "'water'" },
        // { "NAME", "'light'" },
        // { "NAME", "'temperature'" },
        // { "NAME", "'humidity'" },
        // { "NAME", "'location'" },
        public Object NAME(Object VALUE) {
            return VALUE.toString();
        }

        // { "NUMBER", "`integer`" },
        public Object NUMBER(Object integer) {
            return Long.valueOf(integer.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        var input = DEMO_INPUT;

        input = Files.readString(Path.of("src/main/java/day05/input.txt"));

        Parser parser = new SerializedParser(false).get(grammar, "Day05");
        parser.setSemantic(new Semantic());
        if (!parser.parse(input)) {
            System.err.println("Failed to read input");
            System.exit(1);
        }
        Almanac almanac = (Almanac) parser.getResult();

        System.out.println("Lowest location is: " + almanac.lowestLocationForAllSeeds());
    }
}
