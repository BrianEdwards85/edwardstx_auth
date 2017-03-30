CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE IF NOT EXISTS core.services (
  service VARCHAR PRIMARY KEY,
  public_key VARCHAR  NOT NULL);

CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth.credentials (
  email VARCHAR PRIMARY KEY,
  hash VARCHAR NOT NULL,
  secret VARCHAR NOT NULL,
  salt VARCHAR NOT NULL);
