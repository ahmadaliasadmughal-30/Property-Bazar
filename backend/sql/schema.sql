-- EstateVault - Lahore Property Marketplace
-- Run: mysql -u root -p < sql/schema.sql

CREATE DATABASE IF NOT EXISTS estate_vault;
USE estate_vault;

CREATE TABLE IF NOT EXISTS persons (
    id    INT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    type  VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS properties (
    id        INT PRIMARY KEY,
    title     VARCHAR(200) NOT NULL,
    location  VARCHAR(100),
    area      DOUBLE,
    price     DOUBLE,
    seller_id INT,
    status    VARCHAR(20) DEFAULT 'AVAILABLE',
    type      VARCHAR(20),
    FOREIGN KEY (seller_id) REFERENCES persons(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id          INT PRIMARY KEY,
    property_id INT,
    buyer_id    INT,
    seller_id   INT,
    type        VARCHAR(10),
    amount      DOUBLE,
    commission  DOUBLE,
    date        VARCHAR(30),
    FOREIGN KEY (property_id) REFERENCES properties(id),
    FOREIGN KEY (buyer_id)    REFERENCES persons(id),
    FOREIGN KEY (seller_id)   REFERENCES persons(id)
);
