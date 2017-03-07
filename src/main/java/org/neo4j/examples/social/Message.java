package org.neo4j.examples.social;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import java.util.UUID;

import static org.neo4j.driver.v1.Values.parameters;

public class Message
{
    private final static String COUNT_MESSAGES = ("" +
            "MATCH (a:Message) " +
            "RETURN count(a)"
    );
    private final static String MATCH_RANDOM_MESSAGE = ("" +
            "MATCH (a:Person)-[:WROTE]->(m:Message) " +
            "RETURN m.uuid AS uuid, a.email AS email, a.text AS text ORDER BY rand() LIMIT 1"
    );
    private final static String CREATE_MESSAGE = ("" +
            "MATCH (a:Person) WHERE a.email = $email " +
            "WITH a " +
            "CREATE (a)-[:WROTE]->(new:Message) " +
            "SET new.uuid = $uuid, new.text = $text " +
            "RETURN id(new)"
    );
    private final static String CREATE_REPLY = ("" +
            "MATCH (a:Person) WHERE a.email = $email " +
            "MATCH (old:Message) WHERE old.uuid = $related_uuid " +
            "WITH a, old " +
            "CREATE (a)-[:WROTE]->(new:Message)-[:IN_REPLY_TO]->(old) " +
            "SET new.uuid = $uuid, new.text = $text " +
            "RETURN id(new)"
    );

    public static long count(Transaction tx)
    {
        StatementResult result = tx.run(COUNT_MESSAGES);
        Record record = result.single();
        return record.get(0).asLong();
    }

    public static Message loadRandom(Transaction tx)
    {
        StatementResult result = tx.run(MATCH_RANDOM_MESSAGE);
        Record record = result.single();
        return new Message(UUID.fromString(record.get("uuid").asString()), Person.load(tx, record.get("email").asString()), record.get("text").asString());
    }

    private final UUID uuid;
    private final Person author;
    private final String text;
    private final Message inReplyTo;

    Message(UUID uuid, Person author, String text, Message inReplyTo)
    {
        this.uuid = uuid;
        this.author = author;
        this.text = text;
        this.inReplyTo = inReplyTo;
    }

    Message(UUID uuid, Person author, String text)
    {
        this(uuid, author, text, null);
    }

    public Person author()
    {
        return author;
    }

    public Message inReplyTo()
    {
        return inReplyTo;
    }

    public UUID uuid()
    {
        return uuid;
    }

    public String text()
    {
        return text;
    }

    public long save(Transaction tx) throws SocialNetworkException
    {
        StatementResult result;
        if (inReplyTo == null)
        {
            result = tx.run(CREATE_MESSAGE, parameters("email", author.email(), "uuid", uuid.toString(),
                    "text", text));
        }
        else {
            result = tx.run(CREATE_REPLY, parameters("email", author.email(), "uuid", uuid.toString(),
                    "text", text, "related_uuid", inReplyTo.uuid.toString()));
        }
        if (result.hasNext())
        {
            tx.success();
            Record record = result.single();
            return record.get(0).asLong();
        }
        else
        {
            throw new SocialNetworkException("Cannot save message");
        }
    }

}
