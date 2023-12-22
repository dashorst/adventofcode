package day08;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * --- Day 8: Haunted Wasteland ---
 * 
 * You're still riding a camel across Desert Island when you spot a sandstorm
 * quickly approaching. When you turn to warn the Elf, she disappears before
 * your eyes! To be fair, she had just finished warning you about ghosts a few
 * minutes ago.
 * 
 * One of the camel's pouches is labeled "maps" - sure enough, it's full of
 * documents (your puzzle input) about how to navigate the desert. At least,
 * you're pretty sure that's what they are; one of the documents contains a list
 * of left/right instructions, and the rest of the documents seem to describe
 * some kind of network of labeled nodes.
 * 
 * It seems like you're meant to use the left/right instructions to navigate the
 * network. Perhaps if you have the camel follow the same instructions, you can
 * escape the haunted wasteland!
 * 
 * After examining the maps for a bit, two nodes stick out: AAA and ZZZ. You
 * feel like AAA is where you are now, and you have to follow the left/right
 * instructions until you reach ZZZ.
 * 
 * This format defines each node of the network individually. For example:
 * 
 * RL
 * 
 * AAA = (BBB, CCC)
 * BBB = (DDD, EEE)
 * CCC = (ZZZ, GGG)
 * DDD = (DDD, DDD)
 * EEE = (EEE, EEE)
 * GGG = (GGG, GGG)
 * ZZZ = (ZZZ, ZZZ)
 * 
 * Starting with AAA, you need to look up the next element based on the next
 * left/right instruction in your input. In this example, start with AAA and go
 * right (R) by choosing the right element of AAA, CCC. Then, L means to choose
 * the left element of CCC, ZZZ. By following the left/right instructions, you
 * reach ZZZ in 2 steps.
 * 
 * Of course, you might not find ZZZ right away. If you run out of left/right
 * instructions, repeat the whole sequence of instructions as necessary: RL
 * really means RLRLRLRLRLRLRLRL... and so on. For example, here is a situation
 * that takes 6 steps to reach ZZZ:
 * 
 * LLR
 * 
 * AAA = (BBB, BBB)
 * BBB = (AAA, ZZZ)
 * ZZZ = (ZZZ, ZZZ)
 * 
 * Starting at AAA, follow the left/right instructions. How many steps are
 * required to reach ZZZ?
 */
public class Day08Part1 {
    record Node(String key, String left, String right) {
    }

    record Walker(String directions) {
    }

    public static void main(String[] args) throws Exception {
        var input1 = """
                RL

                AAA = (BBB, CCC)
                BBB = (DDD, EEE)
                CCC = (ZZZ, GGG)
                DDD = (DDD, DDD)
                EEE = (EEE, EEE)
                GGG = (GGG, GGG)
                ZZZ = (ZZZ, ZZZ)
                """.lines().toList();

        var input2 = """
                LLR

                AAA = (BBB, BBB)
                BBB = (AAA, ZZZ)
                ZZZ = (ZZZ, ZZZ)
                """.lines().toList();

        var input3 = Files.readString(Path.of("src/main/java/day08/input.txt")).lines().toList();

        var input = input1;
        input = input2;
        input = input3;
        var instructionLine = input.get(0);

        Map<String, Node> tree = new HashMap<>();

        Pattern p = Pattern.compile("([A-Z]{3}) = \\(([A-Z]{3})\\, ([A-Z]{3})\\)");
        for (String line : input.subList(2, input.size())) {
            var m = p.matcher(line);
            if (m.find()) {
                var key = m.group(1);
                var left = m.group(2);
                var right = m.group(3);

                tree.put(key, new Node(key, left, right));
            }
        }
        var node = tree.get("AAA");
        int steps = 0;
        while (!"ZZZ".equals(node.key())) {
            var tape = instructionLine;
            int pos = 0;
            while (pos < tape.length()) {
                steps++;
                var currentNode = node;
                var step = tape.substring(pos++, pos);
                switch (step) {
                    case "L":
                        node = tree.get(node.left());
                        break;
                    case "R":
                        node = tree.get(node.right());
                        break;
                }

                System.out.println(steps + " From: " + currentNode.key() + " " + step + " -> " + node.key());
            }
        }
        System.out.println("Steps using " + instructionLine + " from AAA to ZZZ: " + steps);
    }
}
