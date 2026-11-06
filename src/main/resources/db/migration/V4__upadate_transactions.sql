ALTER TABLE transactions
    ADD COLUMN sender_wallet_id UUID,
    ADD COLUMN receiver_wallet_id UUID,
    ADD COLUMN description VARCHAR(255);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_sender
        FOREIGN KEY (sender_wallet_id) REFERENCES wallets(id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_receiver
        FOREIGN KEY (receiver_wallet_id) REFERENCES wallets(id);

ALTER TABLE transactions
    DROP COLUMN wallet_id;