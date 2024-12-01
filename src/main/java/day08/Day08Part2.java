package day08;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventPoller.Handler;

/**
 * --- Part Two ---
 * 
 * The sandstorm is upon you and you aren't any closer to escaping the
 * wasteland. You had the camel follow the instructions, but you've barely left
 * your starting position. It's going to take significantly more steps to
 * escape!
 * 
 * What if the map isn't for people - what if the map is for ghosts? Are ghosts
 * even bound by the laws of spacetime? Only one way to find out.
 * 
 * After examining the maps a bit longer, your attention is drawn to a curious
 * fact: the number of nodes with names ending in A is equal to the number
 * ending in Z! If you were a ghost, you'd probably just start at every node
 * that ends with A and follow all of the paths at the same time until they all
 * simultaneously end up at nodes that end with Z.
 * 
 * For example:
 * 
 * LR
 * 
 * 11A = (11B, XXX)
 * 11B = (XXX, 11Z)
 * 11Z = (11B, XXX)
 * 22A = (22B, XXX)
 * 22B = (22C, 22C)
 * 22C = (22Z, 22Z)
 * 22Z = (22B, 22B)
 * XXX = (XXX, XXX)
 * 
 * Here, there are two starting nodes, 11A and 22A (because they both end with
 * A). As you follow each left/right instruction, use that instruction to
 * simultaneously navigate away from both nodes you're currently on. Repeat this
 * process until all of the nodes you're currently on end with Z. (If only some
 * of the nodes you're on end with Z, they act like any other node and you
 * continue as normal.) In this example, you would proceed as follows:
 * 
 * Step 0: You are at 11A and 22A.
 * Step 1: You choose all of the left paths, leading you to 11B and 22B.
 * Step 2: You choose all of the right paths, leading you to 11Z and 22C.
 * Step 3: You choose all of the left paths, leading you to 11B and 22Z.
 * Step 4: You choose all of the right paths, leading you to 11Z and 22B.
 * Step 5: You choose all of the left paths, leading you to 11B and 22C.
 * Step 6: You choose all of the right paths, leading you to 11Z and 22Z.
 * 
 * So, in this example, you end up entirely on nodes that end in Z after 6
 * steps.
 * 
 * Simultaneously start on every node that ends with A. How many steps does it
 * take before you're only on nodes that end with Z?
 */
public class Day08Part2 {
    record Node(String key, String left, String right) {
    }

    public static void main(String[] args) throws Exception {
        var input1 = """
                LR

                11A = (11B, XXX)
                11B = (XXX, 11Z)
                11Z = (11B, XXX)
                22A = (22B, XXX)
                22B = (22C, 22C)
                22C = (22Z, 22Z)
                22Z = (22B, 22B)
                XXX = (XXX, XXX)
                """.lines().toList();

        var input2 = Files.readString(Path.of("src/main/java/day08/input.txt")).lines().toList();

        var input = input1;
        // input = input2;

        var instructions = constructInstructions(input.get(0));

        Map<String, String[]> tree = constructTreeFromInput(input);

        var startNodes = tree.keySet().stream().filter(k -> k.endsWith("A")).toList();
        var endNodes = tree.keySet().stream().filter(k -> k.endsWith("Z")).collect(Collectors.toSet());

        System.out.println("Start nodes: " + startNodes);
        System.out.println("End nodes: " + endNodes);

        bruteForceParallel(instructions, tree, startNodes, endNodes);
    }

    static final int chars = 'Z' - '0' + 1;

    private static void bruteForceOptimized(int[] instructions, Map<String, String[]> textTree,
            List<String> textStartNodes, Set<String> endNodes) {
        int[][] nodes = new int[chars * chars * chars][2];
        for (int i = 0; i < nodes.length; i++)
            Arrays.fill(nodes[i], -1);

        textTree.entrySet().forEach(e -> {
            var key = fromStringToNode(e.getKey());
            nodes[key][0] = fromStringToNode(e.getValue()[0]);
            nodes[key][1] = fromStringToNode(e.getValue()[1]);
        });

        int[] startNodes = new int[textStartNodes.size()];
        for (int i = 0; i < startNodes.length; i++) {
            startNodes[i] = fromStringToNode(textStartNodes.get(i));
        }
        int[] currentNodes = new int[textStartNodes.size()];
        System.arraycopy(startNodes, 0, currentNodes, 0, currentNodes.length);

        boolean debug = true;
        long stepCounter = 0;
        boolean done = false;

        long result = 10_668_805_667_831L;

        int z = 'Z' - '0';

        Instant startTime = Instant.now();
        while (!done) {
            int instruction = instructions[(int) (stepCounter % instructions.length)];
            stepCounter++;

            if (stepCounter % 1_000_000_000 == 0) {
                Duration timeBetweenTicks = Duration.between(startTime, Instant.now());
                System.out.print("Time between ticks: " + timeBetweenTicks.toSeconds() + "s");
                System.out.print(", expected duration: " + timeBetweenTicks.multipliedBy(10_668).toHours() + "h");
                System.out.println(String.format(", percentage done: %.1f%%", (stepCounter * 100.0D) / result));
                startTime = Instant.now();
            }

            boolean allEndNodes = true;

            for (int i = 0; i < currentNodes.length; i++) {
                var nextNode = nodes[currentNodes[i]][instruction];
                currentNodes[i] = nextNode;
                allEndNodes &= nextNode % chars == z;
            }
            done = allEndNodes;
        }
        System.out.println("Steps from AAA to ZZZ: " + stepCounter);
    }

    private static AtomicLongArray currentSteps;
    private static int numberOfNodes;

    private static long currentMinStep() {
        long min = Long.MAX_VALUE;
        for (int i = 0; i < numberOfNodes; i++) {
            long val = currentSteps.get(i);
            if (val < min)
                min = val;
        }
        return min;
    }

    private static void bruteForceParallel(int[] instructions, Map<String, String[]> textTree,
            List<String> textStartNodes, Set<String> endNodes) throws InterruptedException {

        numberOfNodes = textStartNodes.size();

        int[][] nodes = new int[chars * chars * chars][2];
        for (int i = 0; i < nodes.length; i++)
            Arrays.fill(nodes[i], -1);

        textTree.entrySet().forEach(e -> {
            var key = fromStringToNode(e.getKey());
            nodes[key][0] = fromStringToNode(e.getValue()[0]);
            nodes[key][1] = fromStringToNode(e.getValue()[1]);
        });

        int[] startNodes = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            startNodes[i] = fromStringToNode(textStartNodes.get(i));
        }

        int[] currentNodes = new int[numberOfNodes];
        System.arraycopy(startNodes, 0, currentNodes, 0, currentNodes.length);

        currentSteps = new AtomicLongArray(new long[numberOfNodes]);

        boolean debug = true;
        long stepCounter = 0;

        AtomicBoolean done = new AtomicBoolean(false);
        Deque<Long> results = new ConcurrentLinkedDeque<>();

        long result = 10_668_805_667_831L;

        int z = 'Z' - '0';

        CountDownLatch countdown = new CountDownLatch(numberOfNodes);

        class Worker implements Runnable {
            private long stepCounter = 0;
            private int workerId;
            private int currentNode;

            Worker(int id, int startNode) {
                this.workerId = id;
                this.currentNode = startNode;
            }

            @Override
            public void run() {
                int lastNode = -1;
                while (!done.get()) {
                    int instruction = instructions[(int) (stepCounter % instructions.length)];
                    stepCounter++;

                    var nextNode = nodes[currentNode][instruction];
                    currentNode = nextNode;

                    if (nextNode % chars == z) {
                        results.addLast(stepCounter);
                        System.out.println(
                                "Worker " + workerId + " found end node at " + stepCounter + ", queue: " + results);
                    }
                    currentSteps.set(workerId, stepCounter);
                    if (stepCounter > 20)
                        break;
                }
                countdown.countDown();
                ;
            }
        }

        class Watcher implements Runnable {
            @Override
            public void run() {
                while (!done.get()) {
                    long processUntil = currentMinStep();
                    System.out.println(
                            " -> Watcher processing up til: " + processUntil + ", queue size: " + results.size());
                    System.out.println("    " + results);

                    int recordsProcessed = 0;
                    var it = results.iterator();
                    while (it.hasNext()) {
                        int count = 1;
                        long currentValueToCompare = it.next();
                        recordsProcessed++;
                        long nextValueToCompare = -1;
                        do {
                            nextValueToCompare = results.peekFirst();
                            System.out.println("   #" + recordsProcessed + " " + currentValueToCompare + "?="
                                    + nextValueToCompare + ", count=" + count);
                            if (currentValueToCompare == nextValueToCompare) {
                                count++;
                                results.removeFirst();
                                recordsProcessed++;
                            }
                        } while (nextValueToCompare == currentValueToCompare);

                        if (count == numberOfNodes) {
                            System.out.println("Jay! Result = " + currentValueToCompare);
                            done.set(true);
                            System.exit(0);
                        }
                    }
                    System.out.println(
                            " <- Watcher processed up til: " + processUntil + ", processed: " + recordsProcessed);
                }
            }
        }
        Thread.startVirtualThread(new Watcher());

        var startTime = Instant.now();
        var workers = new ArrayList<Worker>();
        for (int i = 0; i < startNodes.length; i++) {
            Worker w = new Worker(i, startNodes[i]);
            workers.add(w);
            Thread.startVirtualThread(w);
        }

        while (!done.get() && !countdown.await(10, TimeUnit.SECONDS)) {
            long progress = workers.stream().mapToLong(w -> w.stepCounter).sum();
            var duration = Duration.between(startTime, Instant.now());
            long millis = duration.toMillis();
            var expectedDuration = Duration.ofMillis((long) (millis * (result / (progress * 1.0D))));

            System.out.printf("%s %.1f%% %dmin, %s%n",
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()),
                    (progress * 100.0D) / result, expectedDuration.toMinutes(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plus(expectedDuration)));
        }

        System.out.println("Steps from AAA to ZZZ: " + stepCounter);
    }

    static String fromNodeToString(int node) {
        char char1 = (char) ('0' + (node / chars / chars));
        char char2 = (char) ('0' + ((node % chars * chars) / chars));
        char char3 = (char) ('0' + (node % chars));
        return String.valueOf(new char[] { char1, char2, char3 });
    }

    static int fromStringToNode(String text) {
        int char1 = text.charAt(0) - '0';
        int char2 = text.charAt(1) - '0';
        int char3 = text.charAt(2) - '0';
        int res = chars * chars * char1 + chars * char2 + char3;
        if (res < 0)
            throw new IllegalStateException(
                    "Dit wordt negatief: " + text + " " + Arrays.asList(char1, char2, char3) + ": " + res);
        if (res >= chars * chars * chars)
            throw new IllegalStateException(
                    "Dit wordt te groot: " + text + " " + Arrays.asList(char1, char2, char3) + ": " + res);
        return res;
    }

    private static void bruteForce(int[] instructions, Map<String, String[]> tree, List<String> startNodes,
            Set<String> endNodes) {
        boolean debug = true;
        long stepCounter = 0;
        boolean done = false;
        String[] currentNodes = new String[startNodes.size()];
        for (int i = 0; i < currentNodes.length; i++) {
            currentNodes[i] = startNodes.get(i);
        }
        long result = 10_668_805_667_831L;

        Instant startTime = Instant.now();
        while (!done) {
            int instruction = instructions[(int) (stepCounter % instructions.length)];
            stepCounter++;

            debug = stepCounter % 1_000_000_000_000L == 0;

            if (stepCounter % 1_000_000_000 == 0) {
                Duration timeBetweenTicks = Duration.between(startTime, Instant.now());
                System.out.println("Time between ticks: " + timeBetweenTicks.toSeconds() + "s");
                System.out.println("Expected duration: " + timeBetweenTicks.multipliedBy(10_668).toHours() + "h");
            }
            if (debug) {
                System.out.println(String.format("%-12d From: %s", stepCounter, Arrays.toString(currentNodes)));
            }
            boolean allEndNodes = true;
            for (int i = 0; i < currentNodes.length; i++) {
                var nextNode = tree.get(currentNodes[i])[instruction];
                allEndNodes &= nextNode.charAt(2) == 'Z';
                currentNodes[i] = nextNode;
            }
            if (debug) {
                System.out.println(String.format("%-12d To:   %s", stepCounter, Arrays.toString(currentNodes)));
                System.out.println();
            }
            done = done(currentNodes, endNodes);
        }
        System.out.println("Steps from AAA to ZZZ: " + stepCounter);
    }

    private static boolean done(String[] nodes, Set<String> endNodes) {
        for (int i = 0; i < nodes.length; i++) {
            if (!endNodes.contains(nodes[i]))
                return false;
        }
        return true;
    }

    private static int[] constructInstructions(String line) {
        var result = new int[line.length()];
        for (int i = 0; i < line.length(); i++) {
            result[i] = switch (line.substring(i, i + 1)) {
                case "L" -> 0;
                case "R" -> 1;
                default -> throw new IllegalArgumentException("Unexpected value: " + line.substring(i, i + 1));
            };
        }
        return result;
    }

    private static Map<String, String[]> constructTreeFromInput(List<String> input) {
        Map<String, String[]> tree = new ConcurrentHashMap<>();

        Pattern p = Pattern.compile("([0-9A-Z]{3}) = \\(([0-9A-Z]{3})\\, ([0-9A-Z]{3})\\)");
        for (String line : input.subList(2, input.size())) {
            var m = p.matcher(line);
            if (m.find()) {
                var key = m.group(1);
                var left = m.group(2);
                var right = m.group(3);

                tree.put(key, new String[] { left, right });
            }
        }
        return tree;
    }
}
