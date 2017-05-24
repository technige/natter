package org.neo4j.examples.social;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

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

    public long countUsers()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Person::count);
        }
    }

    public long addUser(Person user)
    {
        System.out.println(format("Adding user %s", user));
        try (Session session = driver.session())
        {
            return session.writeTransaction(user::save);
        }
    }

    public long follow(Person a, Person b)
    {
        System.out.println(format("%s followed %s", a.name(), b.name()));
        try (Session session = driver.session())
        {
            return session.writeTransaction((tx) -> a.follow(tx, b));
        }
    }

    public Person getRandomUser()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Person::loadRandom);
        }
    }

    public long countMessages()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Message::count);
        }
    }

    public Message getRandomMessage()
    {
        try (Session session = driver.session())
        {
            return session.readTransaction(Message::loadRandom);
        }
    }

    public long postMessage(Message message)
    {
        System.out.println(format("Posting a message from %s", message.author().name()));
        try (Session session = driver.session())
        {
            return session.writeTransaction(message::save);
        }
    }

}
