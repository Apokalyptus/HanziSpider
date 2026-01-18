-- This script creates a user and a database for PostgreSQL.
-- These commands should be run by a superuser (e.g., 'postgres').

-- Create the user/role 'crawler'. 
-- Using 'CREATE ROLE' is standard. 'CREATE USER' is an alias for 'CREATE ROLE ... LOGIN'.
-- This command will error if the role already exists. In a script, you might want to drop it first.
-- DROP ROLE IF EXISTS crawler;
CREATE ROLE crawler LOGIN PASSWORD 'PASSWORD';

-- Create the database 'crawler' and set the owner to the new role.
-- This command must be run from a different database (e.g., the default 'postgres' database).
CREATE DATABASE crawler OWNER crawler;