#!/usr/bin/env python
# coding: utf-8


from .person import Person
from .social import SocialNetwork


def main():
    uri = "bolt://localhost:7687"
    auth = ("neo4j", "password")
    with SocialNetwork(uri, auth) as network:
        network.add_user(Person("alice@example.com", "Alice Smith", 5))


if __name__ == "__main__":
    main()
