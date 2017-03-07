#!/usr/bin/env python
# coding: utf-8


COUNT_PEOPLE = """\
MATCH (a:Person)
RETURN count(a)
"""
MATCH_PERSON = """\
MATCH (a:Person) WHERE a.email = $email
RETURN a.name AS name, a.luck AS luck
"""
MATCH_RANDOM_PERSON = """\
MATCH (a:Person)
RETURN a.email AS email, a.name AS name, a.luck AS luck ORDER BY rand() LIMIT 1
"""
MERGE_PERSON = """\
MERGE (a:Person {email: $email})
SET a.name = $name, a.luck = $luck
RETURN id(a)
"""
MERGE_FOLLOW = """\
MATCH (a:Person) WHERE a.email = $follower_email
MATCH (b:Person) WHERE b.email = $followed_email
WITH a, b
MERGE (a)-[ab:FOLLOWS]->(b)
RETURN id(ab)
"""


class Person(object):

    @classmethod
    def count(cls, tx):
        result = tx.run(COUNT_PEOPLE)
        record = result.single()
        return record[0]

    @classmethod
    def load(cls, tx, email):
        result = tx.run(MATCH_PERSON, email=email)
        record = result.single()
        return Person(email, record["name"], record["luck"])

    @classmethod
    def load_random(cls, tx):
        result = tx.run(MATCH_RANDOM_PERSON)
        record = result.single()
        return Person(record["email"], record["name"], record["luck"])

    def __init__(self, email, name, luck):
        self.email = email
        self.name = name
        self.luck = luck

    def __repr__(self):
        return format("%s <%s>", self.name, self.email)

    def __eq__(self, other):
        try:
            return self.email == other.email
        except AttributeError:
            return False

    def save(self, tx):
        result = tx.run(MERGE_PERSON, email=self.email, name=self.name, luck=self.luck)
        record = result.single()
        return record[0]

    def follow(self, tx, other):
        result = tx.run(MERGE_FOLLOW, follower_email=self.email, followed_email=other.email)
        record = result.single()
        return record[0]
