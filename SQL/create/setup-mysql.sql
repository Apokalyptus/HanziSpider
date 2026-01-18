-- This script creates the database and a user for MySQL/MariaDB.

-- Create the database.
CREATE DATABASE IF NOT EXISTS crawler
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Create the user 'crawler' with password 'crawlerX' for local connections.
-- Use 'crawler'@'%' to allow connections from any host.
CREATE USER IF NOT EXISTS 'crawler'@'localhost' IDENTIFIED BY 'PASSWORD';

-- Grant all privileges on the 'crawler' database to the 'crawler' user.
GRANT ALL PRIVILEGES ON crawler.* TO 'crawler'@'localhost';

-- Apply the privilege changes.
FLUSH PRIVILEGES;