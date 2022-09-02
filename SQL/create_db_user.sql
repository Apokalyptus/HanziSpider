create user 'crawler'@'localhost' identified by password '*BAAD7212EE8812365BB8AC8B4E523678B5A388A7';
GRANT SELECT, INSERT, DELETE, UPDATE ON crawler.* TO 'crawler'@'localhost';