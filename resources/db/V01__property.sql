CREATE TABLE Premises (
    id SERIAL PRIMARY KEY,
    building_address text NOT NULL,
    landlord_id integer NOT NULL,
    floor integer NOT NULL,
    number text NOT NULL,
    area numeric(10,1) NOT NULL,
    description text,
    advertised_price money NOT NULL CHECK(advertised_price > 0),
    CONSTRAINT UNIQUE unq_space (building_address, floor, number)
);

CREATE TABLE Landlords (
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    inn text,
    description text,
    identity_info text
);

CREATE TABLE Tenants (
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    inn text,
    main_phone_number text NOT NULL,
    contacts text,
    identity_info text
);

CREATE TABLE PremisesInLeases (
    lease_id integer NOT NULL REFERENCES Leases (id),
    premise_id integer NOT NULL REFERENCES Premises (id),
    price money NOT NULL CHECK(price > 0),
    start_dt date NULL,
    end_dt date NULL,
    date_range daterange NOT NULL,
    CONSTRAINT UNIQUE premise_to_lease (lease_id, premise_id),
    EXCLUDE USING gist (premise_id WITH =, date_range WITH &&)
);

CREATE TABLE Leases (
    id SERIAL PRIMARY KEY,
    number text NOT NULL,
    agreement_dt date NOT NULL,
    tenant_id integer NOT NULL REFERENCES Tenants (id),
    start_dt date NULL,
    end_dt date NULL,
    date_range daterange NOT NULL,
    payment_dt date NOT NULL,
    utilities_in_price boolean NOT NULL,
    CONSTRAINT end_after_start CHECK (end_dt >= start_dt)
);
