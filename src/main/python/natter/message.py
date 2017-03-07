#!/usr/bin/env python
# coding: utf-8


from uuid import UUID


COUNT_MESSAGES = """\
MATCH (a:Message)
RETURN count(a)
"""
MATCH_RANDOM_MESSAGE = """\
MATCH (a:Person)-[:WROTE]->(m:Message)
RETURN m.uuid AS uuid, a.email AS email, a.text AS text ORDER BY rand() LIMIT 1
"""
CREATE_MESSAGE = """\
MATCH (a:Person) WHERE a.email = $email
WITH a
CREATE (a)-[:WROTE]->(new:Message)
SET new.uuid = $uuid, new.text = $text
RETURN id(new)
"""
CREATE_REPLY = """\
MATCH (a:Person) WHERE a.email = $email
MATCH (old:Message) WHERE old.uuid = $related_uuid
WITH a, old
CREATE (a)-[:WROTE]->(new:Message)-[:IN_REPLY_TO]->(old)
SET new.uuid = $uuid, new.text = $text
RETURN id(new)
"""


class Message(object):

    @classmethod
    def count(cls, tx):
        result = tx.run(COUNT_MESSAGES)
        record = result.single()
        return record[0]

    @classmethod
    def load_random(cls, tx):
        from .person import Person
        result = tx.run(MATCH_RANDOM_MESSAGE)
        record = result.single()
        return Message(UUID(record["uuid"]), Person.load(tx, record["email"]), record["text"])

    def __init__(self, uuid, author, text, in_reply_to):
        self.uuid = uuid
        self.author = author
        self.text = text
        self.in_reply_to = in_reply_to

    def save(self, tx):
        if self.in_reply_to:
            result = tx.run(CREATE_REPLY, email=self.author.email, uuid=self.uuid.toString(),
                            text=self.text, related_uuid=self.in_reply_to.uuid.toString())
        else:
            result = tx.run(CREATE_MESSAGE, email=self.author.email, uuid=self.uuid.toString(),
                            text=self.text)
        records = list(result)
        if records:
            pass
        else:
            from .social import SocialNetworkException
            raise SocialNetworkException("Cannot save message")
