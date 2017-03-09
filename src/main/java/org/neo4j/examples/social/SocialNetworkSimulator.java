package org.neo4j.examples.social;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

public class SocialNetworkSimulator implements AutoCloseable
{
    private final SocialNetwork network;
    private final RandomDataGenerator random;

    public SocialNetworkSimulator(String uri, AuthToken auth)
    {
        network = new SocialNetwork(uri, auth);
        random = new RandomDataGenerator(0);
    }

    public void turn()
    {
        // add a user
        network.addUser(random.person());

        // write messages
        for (int i = 0; i < random.number(4); i++)
        {
            Person author = network.getRandomUser();
            Message message;
            if (network.countMessages() < 10 || random.number(4) > 0)
            {
                message = author.write("hello, world");
            }
            else
            {
                message = author.write("hello, world", network.getRandomMessage());
            }
            network.postMessage(message);
        }

        // follow users
        if (network.countUsers() >= 10)
        {
            for (int i = 0; i < random.number(4); i++)
            {
                int threshold = random.number(10) + 1;
                Person user1 = network.getRandomUser();
                Person user2 = network.getRandomUser();
                while (user2.luck() < threshold)
                {
                    user2 = network.getRandomUser();
                }
                if (!user1.equals(user2))
                {
                    network.follow(user1, user2);
                }
            }
        }

    }

    @Override
    public void close() throws Exception
    {
        network.close();
    }

    public static void main(String... args) throws Exception
    {
        String uri = "bolt://localhost:7687";
        AuthToken auth = AuthTokens.basic("neo4j", "password");
        try (SocialNetworkSimulator simulator = new SocialNetworkSimulator(uri, auth))
        {
            boolean done = false;
            while (!done)
            {
                try
                {
                    simulator.turn();
                    Thread.sleep(100);
                }
                catch (ServiceUnavailableException ex)
                {
                    System.err.println("The database service has disappeared! Take action!");
                    done = true;
                }
                catch (InterruptedException ex)
                {
                    System.err.println("Someone pressed Ctrl+C. Bye bye!");
                    done = true;
                }
            }
        }
    }
}
