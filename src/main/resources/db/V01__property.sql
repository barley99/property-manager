CREATE EXTENSION IF NOT EXISTS btree_gist;

DROP TABLE IF EXISTS PremisesInContracts;

DROP TABLE IF EXISTS Contracts;

DROP TABLE IF EXISTS Premises;

DROP TABLE IF EXISTS Buildings;

DROP TABLE IF EXISTS Users;

CREATE TABLE Buildings (
    id SERIAL PRIMARY KEY,
    address text UNIQUE
);

CREATE TABLE Users (
    id BIGSERIAL PRIMARY KEY,
    first_name text NOT NULL,
    last_name text NOT NULL,
    email text NOT NULL,
    phone text NOT NULL,
    passwd text NOT NULL,
    role text NOT NULL
);

CREATE TABLE Premises (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES Buildings (id),
    landlord_id BIGINT NOT NULL REFERENCES Users (id),
    floor integer NOT NULL,
    number text NOT NULL,
    area numeric(10, 1) NOT NULL,
    description text,
    advertised_price INT NOT NULL CHECK(advertised_price > 0),
    CONSTRAINT unq_space UNIQUE (building_id, floor, number)
);

CREATE TABLE Contracts (
    id BIGSERIAL PRIMARY KEY,
    number text NOT NULL,
    agreement_dt date NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES Users (id),
    start_dt date NOT NULL,
    end_dt date NOT NULL,
    payment_dt date NOT NULL,
    utilities_in_price boolean NOT NULL,
    CONSTRAINT end_after_start CHECK (end_dt >= start_dt)
);

CREATE TABLE PremisesInContracts (
    contract_id BIGINT NOT NULL REFERENCES Contracts (id),
    premise_id BIGINT NOT NULL REFERENCES Premises (id),
    price INT NOT NULL CHECK(price > 0),
    start_dt date NOT NULL,
    end_dt date NOT NULL,
    CONSTRAINT end_after_start CHECK (end_dt >= start_dt),
    CONSTRAINT premise_to_contract UNIQUE (contract_id, premise_id),
    EXCLUDE USING gist (
        premise_id WITH =,
        daterange(start_dt, end_dt, '[]') WITH &&
    )
);

INSERT INTO Users (
    first_name,
    last_name,
    email,
    phone,
    passwd,
    role
  )
VALUES (
    'John',
    'De Goes',
    'degoes@example.com',
    '2223322',
    'password',
    'landlord'
  );