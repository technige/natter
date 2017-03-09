package org.neo4j.examples.social;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import java.util.UUID;

import static java.lang.String.format;
import static org.neo4j.driver.v1.Values.parameters;

public class Person
{
    private final static String COUNT_PEOPLE = ("" +
            "MATCH (a:Person) " +
            "RETURN count(a)"
    );
    private final static String MATCH_PERSON = ("" +
            "MATCH (a:Person) WHERE a.email = $email " +
            "RETURN a.name AS name, a.luck AS luck"
    );
    private final static String MATCH_RANDOM_PERSON = ("" +
            "MATCH (a:Person) " +
            "RETURN a.email AS email, a.name AS name, a.luck AS luck ORDER BY rand() LIMIT 1"
    );
    private final static String MERGE_PERSON = ("" +
            "MERGE (a:Person {email: $email}) " +
            "SET a.name = $name, a.luck = $luck " +
            "RETURN id(a)"
    );
    private final static String MERGE_FOLLOW = ("" +
            "MATCH (a:Person) WHERE a.email = $follower_email " +
            "MATCH (b:Person) WHERE b.email = $followed_email " +
            "WITH a, b " +
            "MERGE (a)-[ab:FOLLOWS]->(b) " +
            "RETURN id(ab)"
    );

    public static long count(Transaction tx)
    {
        StatementResult result = tx.run(COUNT_PEOPLE);
        return result.single().get(0).asLong();
    }

    public static Person load(Transaction tx, String email)
    {
        StatementResult result = tx.run(MATCH_PERSON, parameters("email", email));
        Record record = result.single();
        return new Person(email, record.get("name").asString(), record.get("luck").asInt());
    }

    public static Person loadRandom(Transaction tx)
    {
        StatementResult result = tx.run(MATCH_RANDOM_PERSON);
        Record record = result.single();
        return new Person(record.get("email").asString(), record.get("name").asString(), record.get("luck").asInt());
    }

    private final String email;
    private final String name;
    private final int luck;

    public Person(String email, String name, int luck)
    {
        this.email = email;
        this.name = name;
        this.luck = luck;
    }

    public String email()
    {
        return email;
    }

    public String name()
    {
        return name;
    }

    public int luck()
    {
        return luck;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Person person = (Person) o;
        return email.equals(person.email);
    }

    @Override
    public String toString()
    {
        return format("%s <%s>", name, email);
    }

    public long save(Transaction tx)
    {
        StatementResult result = tx.run(MERGE_PERSON, parameters("email", email, "name", name, "luck", luck));
        Record record = result.single();
        return record.get(0).asLong();
    }

    public long follow(Transaction tx, Person other)
    {
        StatementResult result = tx.run(MERGE_FOLLOW, parameters("follower_email", email, "followed_email", other.email));
        Record record = result.single();
        return record.get(0).asLong();
    }

    public Message write(String text)
    {
        return new Message(UUID.randomUUID(), this, text);
    }

    public Message write(String text, Message inReplyTo)
    {
        return new Message(UUID.randomUUID(), this, text, inReplyTo);
    }

}
