#!/usr/bin/env python
# coding: utf-8


from neo4j.v1 import GraphDatabase


class SocialNetwork(object):

    def __init__(self, uri, auth):
        self.driver = GraphDatabase.driver(uri, auth=auth)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        pass

    def add_user(self, user):
        with self.driver.session() as session:
            return session.write_transaction(user.save)


class SocialNetworkException(Exception):

    pass
