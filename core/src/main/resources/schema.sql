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

CREATE TABLE IF NOT EXISTS balance_histories (
    id VARCHAR PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    created_time BIGINT NOT NULL,
    modified_by VARCHAR NOT NULL,
    modified_time BIGINT NOT NULL,
    version BIGINT NOT NULL,
    member_id VARCHAR NOT NULL,
    transaction_id VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    amount BIGINT NOT NULL,
    action VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    created_time BIGINT NOT NULL,
    modified_by VARCHAR NOT NULL,
    modified_time BIGINT NOT NULL,
    version BIGINT NOT NULL,
    member_id VARCHAR NOT NULL,
    reference_id VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    amount BIGINT NOT NULL,
    metadata jsonb
);


CREATE UNIQUE INDEX IF NOT EXISTS members_emailOrPhoneNumber_idx ON members(email, phone_number);
CREATE INDEX IF NOT EXISTS balances_typeAndMemberId_idx ON balances(type, member_id);
CREATE UNIQUE INDEX IF NOT EXISTS transactions_typeAndReferenceId_idx ON transactions(type, reference_id);
CREATE INDEX IF NOT EXISTS transactions_memberIdAndType_idx ON transactions(member_id, type);
