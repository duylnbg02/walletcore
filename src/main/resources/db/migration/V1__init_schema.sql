
-- =============================================
-- WalletCore - Initial Database Schema
-- Version: V1
-- =============================================

-- 1. USERS
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       user_name VARCHAR(150) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       phone VARCHAR(20) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_users_status
                           CHECK (status IN ('ACTIVE', 'BLOCKED')),

                       CONSTRAINT chk_users_email_lowercase
                           CHECK (email = LOWER(email))
);

-- 2. ROLES
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       code VARCHAR(50) NOT NULL UNIQUE,
                       name VARCHAR(100) NOT NULL
);

-- 3. USER_ROLES
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,

                            CONSTRAINT pk_user_roles
                                PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id),

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
);

-- 4. WALLETS
CREATE TABLE wallets (
                         id UUID PRIMARY KEY,
                         user_id UUID NOT NULL UNIQUE,
                         balance BIGINT NOT NULL DEFAULT 0,
                         currency VARCHAR(3) NOT NULL DEFAULT 'VND',
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         version BIGINT NOT NULL DEFAULT 0,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_wallets_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id),

                         CONSTRAINT chk_wallets_balance
                             CHECK (balance >= 0),

                         CONSTRAINT chk_wallets_currency
                             CHECK (currency = 'VND'),

                         CONSTRAINT chk_wallets_status
                             CHECK (status IN ('ACTIVE', 'FROZEN'))
);

-- 5. INDEXES
CREATE INDEX idx_user_roles_role_id
    ON user_roles(role_id);
