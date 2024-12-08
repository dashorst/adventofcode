package adventofcode2024;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class Day02 {
    private static boolean debug = true;

    public static void main(String[] args) throws IOException {
        System.out.printf("Day 02 - part 1, %-5s: safe routes: %d%n", "demo", part01("demo.txt"));
        System.out.printf("Day 02 - part 1, %-5s: safe routes: %d%n", "input", part01("input.txt"));
        System.out.printf("Day 02 - part 2, %-5s: safe routes: %d%n", "demo", part02("demo.txt"));
        System.out.printf("Day 02 - part 2, %-5s: safe routes: %d%n", "input", part02("input.txt"));
    }

    /**
     * --- Day 2: Red-Nosed Reports ---
     * 
     * Fortunately, the first location The Historians want to search isn't a long
     * walk from the Chief Historian's office.
     * 
     * While the Red-Nosed Reindeer nuclear fusion/fission plant appears to contain
     * no sign of the Chief Historian, the engineers there run up to you as soon as
     * they see you. Apparently, they still talk about the time Rudolph was saved
     * through molecular synthesis from a single electron.
     * 
     * They're quick to add that - since you're already here - they'd really
     * appreciate your help analyzing some unusual data from the Red-Nosed reactor.
     * You turn to check if The Historians are waiting for you, but they seem to
     * have already divided into groups that are currently searching every corner of
     * the facility. You offer to help with the unusual data.
     * 
     * The unusual data (your puzzle input) consists of many reports, one report per
     * line. Each report is a list of numbers called levels that are separated by
     * spaces. For example:
     * 
     * ```
     * 7 6 4 2 1
     * 1 2 7 8 9
     * 9 7 6 2 1
     * 1 3 2 4 5
     * 8 6 4 4 1
     * 1 3 6 7 9
     * ```
     * 
     * This example data contains six reports each containing five levels.
     * 
     * The engineers are trying to figure out which reports are safe. The Red-Nosed
     * reactor safety systems can only tolerate levels that are either gradually
     * increasing or gradually decreasing. So, a report only counts as safe if both
     * of the following are true:
     * 
     * The levels are either all increasing or all decreasing.
     * Any two adjacent levels differ by at least one and at most three.
     * In the example above, the reports can be found safe or unsafe by checking
     * those rules:
     * 
     * 7 6 4 2 1: Safe because the levels are all decreasing by 1 or 2.
     * 1 2 7 8 9: Unsafe because 2 7 is an increase of 5.
     * 9 7 6 2 1: Unsafe because 6 2 is a decrease of 4.
     * 1 3 2 4 5: Unsafe because 1 3 is increasing but 3 2 is decreasing.
     * 8 6 4 4 1: Unsafe because 4 4 is neither an increase or a decrease.
     * 1 3 6 7 9: Safe because the levels are all increasing by 1, 2, or 3.
     * So, in this example, 2 reports are safe.
     * 
     * Analyze the unusual data from the engineers. How many reports are safe?
     * 
     * To begin, get your puzzle input.
     * 
     * @return
     */
    private static int part01(String filename) throws IOException {
        var lines = Files.readAllLines(Path.of("src/main/resources/2024/02/" + filename));
        List<ArrayList<Integer>> routes = lines.stream().map(l -> Arrays.stream(l.split(" ")).map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new))).toList();
        var safeLines = 0;
        for (var route : routes) {
            var report = SafetyReport.of(route, OptionalInt.empty());
            if (debug)
                System.out.println("  - " + route + " " + (report.safe() ? "safe" : "unsafe"));
            if (report.safe())
                safeLines++;
        }
        return safeLines;
    }

    static record SafetyReport(List<Integer> route, OptionalInt removedIndex, boolean safe,
            Set<Integer> problemIndices) {
        int problemCount() {
            return problemIndices.size();
        }

        static SafetyReport of(List<Integer> route, OptionalInt removedIndex) {
            Set<Integer> problemIndices = new LinkedHashSet<>();
            boolean safeRoute = true;
            boolean increasing = true;
            boolean descreasing = true;
            for (int i = 1; i < route.size(); i++) {
                int num1 = route.get(i - 1);
                int num2 = route.get(i);
                int diff = num1 - num2;
                int dist = Math.abs(diff);
                increasing &= diff > 0;
                descreasing &= diff < 0;
                boolean stepIsSafe = (increasing || descreasing) && dist >= 1 && dist <= 3;
                if (!stepIsSafe && (dist >= 1 && dist <= 3)) {
                    problemIndices.add(i);
                } else {
                    problemIndices.add(i);
                    problemIndices.add(i - 1);
                }
                safeRoute &= stepIsSafe;
            }
            return new SafetyReport(route, removedIndex, safeRoute, problemIndices);
        }
    }

    /**
     * --- Part Two ---
     * 
     * The engineers are surprised by the low number of safe reports until they
     * realize they forgot to tell you about the Problem Dampener.
     * 
     * The Problem Dampener is a reactor-mounted module that lets the reactor safety
     * systems tolerate a single bad level in what would otherwise be a safe report.
     * It's like the bad level never happened!
     * 
     * Now, the same rules apply as before, except if removing a single level from
     * an unsafe report would make it safe, the report instead counts as safe.
     * 
     * More of the above example's reports are now safe:
     * 
     * 7 6 4 2 1: Safe without removing any level.
     * 1 2 7 8 9: Unsafe regardless of which level is removed.
     * 9 7 6 2 1: Unsafe regardless of which level is removed.
     * 1 3 2 4 5: Safe by removing the second level, 3.
     * 8 6 4 4 1: Safe by removing the third level, 4.
     * 1 3 6 7 9: Safe without removing any level.
     * Thanks to the Problem Dampener, 4 reports are actually safe!
     * 
     * Update your analysis by handling situations where the Problem Dampener can
     * remove a single level from unsafe reports. How many reports are now safe?
     */
    private static int part02(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("src/main/resources/2024/02/" + filename));
        List<ArrayList<Integer>> routes = lines.stream().map(l -> Arrays.stream(l.split(" ")).map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new))).toList();
        int safeRoutes = 0;
        for (var route : routes) {
            var reports = new ArrayList<SafetyReport>();
            var report = SafetyReport.of(route, OptionalInt.empty());
            reports.add(report);

            var reportsWithOneProblemRemoved = new ArrayList<SafetyReport>();
            for (int i : report.problemIndices()) {
                var routeWithoutProblemI = new ArrayList<>(report.route());
                routeWithoutProblemI.remove(i);
                var reportWithoutProblemI = SafetyReport.of(routeWithoutProblemI, OptionalInt.of(i));
                reportsWithOneProblemRemoved.add(reportWithoutProblemI);
            }
            reports.addAll(reportsWithOneProblemRemoved);

            boolean safeRoute = reports.stream().anyMatch(SafetyReport::safe);
            if (debug)
                System.out.printf("  - route %s is %s%n", report.route(), report
                        .safe() ? "safe"
                                : safeRoute
                                        ? "safe with problem index "
                                                + (reports.stream().filter(SafetyReport::safe).findFirst()
                                                        .get().removedIndex().getAsInt())
                                                + " removed"
                                        : ("unsafe"));
            if (safeRoute)
                safeRoutes++;
            else if (debug) {
                reports.forEach(r -> System.out.println("    - " + r));
            }
        }
        return safeRoutes;
    }
}
