package org.neo4j.examples.social;

import java.util.Random;

import static java.lang.String.format;

public class RandomDataGenerator
{
    private final static String[] INITIALS = new String[]{"", "",
            "b", "bj", "bl", "br", "c", "ch", "chr", "cl", "cr", "cz", "d", "dj", "dr", "dw", "f", "fl", "fj", "fr",
            "g", "gl", "gr", "h", "j", "k", "kr", "l", "m", "n", "p", "ph", "phr", "pl", "pr", "r",
            "s", "sc", "sch", "scr", "sh", "shr", "sk", "sl", "sm", "sn", "sp", "st", "str", "sv", "sw",
            "t", "th", "thr", "tr", "tw", "v", "w", "wh", "wr", "z", "zh"};
    private final static String[] FINALS = new String[]{"", "", "b", "ch", "ck", "cks", "d", "g", "gh",
            "l", "ld", "lf", "lk", "lm", "lp", "ls", "lt", "m", "mp", "n", "nd", "nt", "p", "r", "rb", "rd", "rk", "rm", "rn", "rp", "rs", "rt", "rx", "s", "t", "x", "z"};
    private final static String[] VOWELS = new String[]{"a", "a", "ai", "au", "e", "e", "ee", "eu", "i", "i", "o", "o", "oo", "ou", "u", "u"};

    private final Random random;

    public RandomDataGenerator(long seed)
    {
        random = new Random(seed);
    }

    public int number(int i)
    {
        return random.nextInt(i);
    }

    public Person person()
    {
        int i = random.nextInt();
        while (i < 0)
        {
            i = random.nextInt();
        }
        String firstName = name();
        String lastName = name();
        String email = format("%s.%d@%s", firstName.toLowerCase(), i, "example.com");
        String name = format("%s %s", firstName, lastName);
        int luck = random.nextInt(11);
        return new Person(email, name, luck);
    }

    String name()
    {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < random.nextInt(3) + 1; i++)
        {
            s.append(syllable());
        }
        String name = s.toString();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    String syllable()
    {
        return INITIALS[random.nextInt(INITIALS.length)] + VOWELS[random.nextInt(VOWELS.length)] + FINALS[random.nextInt(FINALS.length)];
    }

    public static void main(String... args)
    {
        RandomDataGenerator random = new RandomDataGenerator(0);
        for (int i = 0; i < 20; i++)
        {
            System.out.println(random.person());
        }
    }

}
