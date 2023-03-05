CREATE TABLE IF NOT EXISTS members (
    id VARCHAR PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    created_time BIGINT NOT NULL,
    modified_by VARCHAR NOT NULL,
    modified_time BIGINT NOT NULL,
    version BIGINT NOT NULL,
    email VARCHAR NOT NULL,
    phone_number VARCHAR NOT NULL,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS balances (
    id VARCHAR PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    created_time BIGINT NOT NULL,
    modified_by VARCHAR NOT NULL,
    modified_time BIGINT NOT NULL,
    version BIGINT NOT NULL,
    member_id VARCHAR NOT NULL,
    amount BIGINT NOT NULL,
    type VARCHAR NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS members_emailOrPhoneNumber_idx ON members(email, phone_number);
CREATE INDEX IF NOT EXISTS balances_memberIdAndType_idx ON balances(member_id, type);