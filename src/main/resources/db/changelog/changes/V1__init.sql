-- Table users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL
    );

-- Table password
CREATE TABLE IF NOT EXISTS password (
    user_id BIGINT PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT fk_password_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Table travels
CREATE TABLE IF NOT EXISTS travels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ,
    owner_id BIGINT NOT NULL,
    status travel_status NOT NULL,
    CONSTRAINT fk_travels_owner
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_travels_date CHECK (end_date IS NULL OR start_date < end_date)
    );

-- Table travel_members
CREATE TABLE IF NOT EXISTS travel_members (
    id BIGSERIAL PRIMARY KEY,
    id_user BIGINT NOT NULL,
    id_travel BIGINT NOT NULL,
    status member_status NOT NULL,
    CONSTRAINT fk_travel_members_user
    FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_travel_members_travel
    FOREIGN KEY (id_travel) REFERENCES travels(id) ON DELETE CASCADE,
    CONSTRAINT uq_travel_members_user_travel UNIQUE (id_user, id_travel)
    );

-- Table categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    id_travel BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT fk_categories_travel
    FOREIGN KEY (id_travel) REFERENCES travels(id) ON DELETE CASCADE
    );

-- Table expenses
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    payer_id BIGINT NOT NULL,
    travel_id BIGINT NOT NULL,
    category_id BIGINT,
    sum NUMERIC(14,2) NOT NULL,
    date TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_expenses_payer
    FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_expenses_travel
    FOREIGN KEY (travel_id) REFERENCES travels(id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT chk_positive_money_expenses CHECK (sum >= 0)
    );

-- Table members_expenses
CREATE TABLE IF NOT EXISTS members_expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expense_id BIGINT NOT NULL,
    share NUMERIC(14,2) NOT NULL,
    CONSTRAINT fk_members_expenses_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_members_expenses_expense
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
    );

-- Table transfers
CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    travel_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    sum NUMERIC(14,2) NOT NULL,
    date TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_transfers_travel
    FOREIGN KEY (travel_id) REFERENCES travels(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfers_sender
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_receiver
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_positive_money_transfers CHECK (sum >= 0),
    CONSTRAINT chk_no_self_transfer CHECK (sender_id <> recipient_id)
    );

-- Table history
CREATE TABLE IF NOT EXISTS history (
    id BIGSERIAL PRIMARY KEY,
    travel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    date TIMESTAMPTZ NOT NULL,
    type history_type NOT NULL,
    description TEXT NOT NULL,
    CONSTRAINT fk_history_travel
    FOREIGN KEY (travel_id) REFERENCES travels(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
    );

-- Table events
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    travel_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    status BOOLEAN NOT NULL,
    CONSTRAINT fk_events_travel
    FOREIGN KEY (travel_id) REFERENCES travels(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_travels_owner ON travels(owner_id);
CREATE INDEX IF NOT EXISTS idx_expenses_travel ON expenses(travel_id);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category_id);
CREATE INDEX IF NOT EXISTS idx_travel_members_travel ON travel_members(id_travel);
