package day05;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator.StreamableGenerator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Parser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.Token;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.builder.SerializedParser;
import com.martijndashorst.runcc.patterns.interpreter.parsergenerator.semantics.ReflectSemantic;

/**
 * --- Part Two ---
 * 
 * Everyone will starve if you only plant such a small number of seeds.
 * Re-reading the almanac, it looks like the seeds: line actually describes
 * ranges of seed numbers.
 * 
 * The values on the initial seeds: line come in pairs. Within each pair, the
 * first value is the start of the range and the second value is the length of
 * the range. So, in the first line of the example above:
 * 
 * seeds: 79 14 55 13
 * This line describes two ranges of seed numbers to be planted in the garden.
 * The first range starts with seed number 79 and contains 14 values: 79, 80,
 * ..., 91, 92. The second range starts with seed number 55 and contains 13
 * values: 55, 56, ..., 66, 67.
 * 
 * Now, rather than considering four seed numbers, you need to consider a total
 * of 27 seed numbers.
 * 
 * In the above example, the lowest location number can be obtained from seed
 * number 82, which corresponds to soil 84, fertilizer 84, water 84, light 77,
 * temperature 45, humidity 46, and location 46. So, the lowest location number
 * is 46.
 * 
 * Consider all of the initial seed numbers listed in the ranges on the first
 * line of the almanac. What is the lowest location number that corresponds to
 * any of the initial seed numbers?
 */
public class Day05Part2 {
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
            { "SEEDS", "'seeds'", "':'", "SEEDRANGES" },
            { "SEEDRANGES", "SEEDRANGE" },
            { "SEEDRANGES", "SEEDRANGE", "SEEDRANGES" },
            { "SEEDRANGE", "NUMBER", "NUMBER" },
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
            { "NUMBER", "`integer`" },
            { Token.IGNORED, "`whitespaces`" },
    };

    record Almanac(List<SeedRange> seeds, Mapping seedsMapping, Map<Mapping, List<MappingRange>> mappings) {
        public Mapping getMapping(String from) {
            return mappings.keySet().stream().filter(m -> m.from().equals(from)).findFirst().get();
        }

        public String path(long seed) {
            return seedsMapping.path(seed);
        }

        public long lowestLocationForAllSeeds() {
            var count = seeds.stream().flatMap(SeedRange::stream).count();
            AtomicLong start = new AtomicLong(0);
            var percentageBase = count / 100;
            var prevPerc = new AtomicLong(0);
            var lowest = seeds.parallelStream().flatMap(SeedRange::stream)
                    .peek(s -> start.incrementAndGet())
                    .map(seedsMapping::location)
                    .peek(l -> {
                        var perc = start.get() / percentageBase;
                        if(prevPerc.get() != perc) {
                            prevPerc.set(perc);
                            System.out.println("Progress: " + perc + "% (" + start.get() + " of " + count + ")");
                        }
                    })
                    .min(Long::compare).get();
            return lowest;
        }
    }

    record SeedRange(long start, long count) {
        public Stream<Long> stream() {
            return LongStream.range(start, start + count).mapToObj(Long::valueOf);
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

        public String path(long seed) {
            long destValue = getDestValue(seed);

            if (to().equals("location"))
                return from() + " " + seed + " -> " + destValue;

            var destMapping = almanac().get().getMapping(to());
            return from() + " " + seed + " -> " + destValue + ": " + destMapping.path(destValue);
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
            return src >= srcRange && src < (srcRange + range);
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
            var almanac = new Almanac((List<SeedRange>) SEEDS, seedsMapping, (Map) MAP);
            almanac.mappings().keySet().forEach(m -> m.almanac().set(almanac));
            return almanac;
        }

        // { "SEEDS", "'seeds'", "':'", "SEEDRANGES" },
        public Object SEEDS(Object TOKEN, Object COLON, Object SEEDRANGES) {
            return SEEDRANGES;
        }

        // { "SEEDRANGES", "SEEDRANGE"},
        public Object SEEDRANGES(Object SEEDRANGE) {
            var res = new ArrayList<SeedRange>();
            res.add((SeedRange) SEEDRANGE);
            return res;
        }

        // { "SEEDRANGES", "SEEDRANGE", "SEEDRANGES" },
        public Object SEEDRANGES(Object SEEDRANGE, Object SEEDRANGES) {
            ((List) SEEDRANGES).add(SEEDRANGE);
            return SEEDRANGES;
        }

        // { "SEEDRANGE", "NUMBER", "NUMBER"},
        public Object SEEDRANGE(Object START, Object COUNT) {
            return new SeedRange((Long) START, (Long) COUNT);
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
