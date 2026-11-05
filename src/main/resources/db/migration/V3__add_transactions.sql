CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT chk_transactions_type CHECK (type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),
    CONSTRAINT chk_transactions_amount CHECK (amount >= 0),
    CONSTRAINT chk_transactions_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
