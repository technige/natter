package org.neo4j.examples.social;

import org.neo4j.driver.v1.*;

import static java.lang.String.format;

public class SocialNetwork implements AutoCloseable
{
    private final Driver driver;

    public SocialNetwork(String uri, AuthToken auth)
    {
        driver = GraphDatabase.driver(uri, auth);
    }

    @Override
    public void close()
    {
        driver.close();
    }

    public long addUser(Person user)
    {
        try (Session session = driver.session())
        {
            return session.writeTransaction(user::save);
        }
        finally
        {
            System.out.println(format("Added user %s", user));
        }
    }

    public Person getUserByEmail(String email)
    {
        try (Session session = driver.session())
        {
            return session.readTransaction((tx) -> Person.load(tx, email));
        }
    }

    public Person getRandomUser()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Person::loadRandom);
        }
    }

    public Message getRandomMessage()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Message::loadRandom);
        }
    }

    public long countUsers()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Person::count);
        }
    }

    public long countMessages()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Message::count);
        }
    }

    public long follow(Person a, Person b)
    {
        try (Session session = driver.session())
        {
            return session.writeTransaction((tx) -> a.follow(tx, b));
        }
        finally
        {
            System.out.println(format("%s followed %s", a.name(), b.name()));
        }
    }

    public long postMessage(Message message)
    {
        try (Session session = driver.session())
        {
            return session.writeTransaction(message::save);
        }
        finally
        {
            System.out.println(format("%s wrote a message", message.author().name()));
        }
    }

}
