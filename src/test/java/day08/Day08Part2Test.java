package day08;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Day08Part2Test {
    @Test
    public void banana() {
        assertEquals("AAA", Day08Part2.fromNodeToString(Day08Part2.fromStringToNode("AAA")));
    }

    @Test
    public void banana2() {
        assertEquals('Z' - '0', Day08Part2.fromStringToNode("AAZ") % 43);
    }
}
