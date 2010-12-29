CREATE USER 'optimist'@'localhost' IDENTIFIED BY 'optimist';

CREATE DATABASE IF NOT EXISTS optimist_test;

USE optimist_test;

CREATE TABLE IF NOT EXISTS entity (
	id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	value VARCHAR(10) NULL,
	version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS inherited_entity (
	id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	value VARCHAR(10) NULL,
	additional_value VARCHAR(10) NULL,
	version INTEGER NOT NULL
);

GRANT ALL ON optimist_test.* TO 'optimist'@'localhost';