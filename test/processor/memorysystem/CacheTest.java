package processor.memorysystem;

import processor.Clock;

/**
 * Unit tests for the set associative cache.
 *
 * The point of interest is the behaviour that distinguishes a set associative
 * cache from a direct mapped one: two addresses that land on the same set must
 * be able to live in the cache at the same time, and when the set is full it
 * must be the least recently used way that gets evicted.
 *
 *   javac -d out $(find src test -name "*.java")
 *   java -cp out processor.memorysystem.CacheTest
 */
public class CacheTest {

    static int passed = 0, failed = 0;

    static void check(String what, boolean ok) {
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + what);
        if (ok) passed++; else failed++;
    }

    /** Mimics the fill that handleResponse performs after a miss. */
    static void install(Cache c, int address, int data) {
        CacheLine victim = c.selectVictim(c.setIndexOf(address));
        victim.fill(c.tagOf(address), data);
    }

    public static void main(String[] args) {
        // 16B cache with 4B lines gives 4 lines; 2-way gives 2 sets of 2 ways.
        Cache c = new Cache(null, 16, 2);
        System.out.println("Cache: size=16B, associativity=" + c.getAssociativity()
                + ", sets=" + c.getNumberOfSets());

        check("associativity is 2", c.getAssociativity() == 2);
        check("number of sets is 2", c.getNumberOfSets() == 2);

        System.out.println("\n-- address decomposition --");
        // Even addresses fall in set 0, odd ones in set 1.
        check("addr 0 -> set 0", c.setIndexOf(0) == 0);
        check("addr 2 -> set 0", c.setIndexOf(2) == 0);
        check("addr 1 -> set 1", c.setIndexOf(1) == 1);
        check("addr 0 and 2 differ in tag", c.tagOf(0) != c.tagOf(2));

        System.out.println("\n-- addresses that would conflict in a direct mapped cache --");
        Clock.incrementClock();
        install(c, 0, 100);
        Clock.incrementClock();
        install(c, 2, 200);

        check("addr 0 still cached", c.lookup(0) != null);
        check("addr 2 still cached", c.lookup(2) != null);
        check("addr 0 holds its value", c.lookup(0).getData() == 100);
        check("addr 2 holds its value", c.lookup(2).getData() == 200);
        check("an unrelated address misses", c.lookup(4) == null);

        System.out.println("\n-- LRU replacement within a set --");
        Clock.incrementClock();
        c.lookup(0).getData();      // touch addr 0, so addr 2 becomes the LRU way
        Clock.incrementClock();
        install(c, 4, 400);         // set 0 is full, so this evicts the LRU way

        check("newly filled address is cached", c.lookup(4) != null);
        check("recently used address survived", c.lookup(0) != null);
        check("least recently used address evicted", c.lookup(2) == null);

        System.out.println("\n-- sets are independent --");
        check("set 1 still empty", c.lookup(1) == null);
        Clock.incrementClock();
        install(c, 1, 111);
        check("address cached in set 1", c.lookup(1) != null);
        check("set 0 untouched by set 1 fill", c.lookup(0) != null && c.lookup(4) != null);

        System.out.println("\n-- sizing --");
        Cache big = new Cache(null, 1024, 2);
        check("1024B / 4B = 256 lines, 2-way -> 128 sets", big.getNumberOfSets() == 128);

        Cache clamped = new Cache(null, 16, 64);   // more ways than the cache has lines
        check("associativity clamped to line count", clamped.getAssociativity() == 4);
        check("fully associative cache has one set", clamped.getNumberOfSets() == 1);

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
