CREATE TYPE user_role    AS ENUM ('BETTOR', 'BOOKMAKER');
CREATE TYPE event_status AS ENUM ('OPEN', 'CLOSED', 'RESOLVED');
CREATE TYPE bet_status   AS ENUM ('PENDING', 'WON', 'LOST');

-- Utilisateurs
CREATE TABLE users (
                       id         BIGSERIAL     PRIMARY KEY,
                       username   VARCHAR(30)   NOT NULL UNIQUE,
                       email      VARCHAR(255)  NOT NULL UNIQUE,
                       password   VARCHAR(255)  NOT NULL,
                       role       user_role     NOT NULL DEFAULT 'BETTOR',
                       balance    DECIMAL(10,2) NOT NULL DEFAULT 1000.00,
                       created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

-- Événements
CREATE TABLE events (
                        id          BIGSERIAL     PRIMARY KEY,
                        title       VARCHAR(200)  NOT NULL,
                        description TEXT,
                        category    VARCHAR(50),
                        close_at    TIMESTAMPTZ   NOT NULL,
                        status      event_status  NOT NULL DEFAULT 'OPEN',
                        created_by  BIGINT        NOT NULL REFERENCES users(id),
                        created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                        resolved_at TIMESTAMPTZ
);

-- Issues
CREATE TABLE outcomes (
                          id        BIGSERIAL    PRIMARY KEY,
                          event_id  BIGINT       NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                          label     VARCHAR(100) NOT NULL,
                          odds      DECIMAL(6,2) NOT NULL,
                          is_winner BOOLEAN      NOT NULL DEFAULT FALSE,
                          CONSTRAINT chk_odds_positive CHECK (odds > 1.0)
);

-- Paris
CREATE TABLE bets (
                      id             BIGSERIAL     PRIMARY KEY,
                      user_id        BIGINT        NOT NULL REFERENCES users(id),
                      outcome_id     BIGINT        NOT NULL REFERENCES outcomes(id),
                      amount         DECIMAL(10,2) NOT NULL,
                      potential_gain DECIMAL(10,2) NOT NULL,
                      status         bet_status    NOT NULL DEFAULT 'PENDING',
                      placed_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                      CONSTRAINT chk_amount_positive         CHECK (amount > 0),
                      CONSTRAINT chk_potential_gain_positive CHECK (potential_gain > 0)
);

-- Index
CREATE INDEX idx_events_status      ON events(status);
CREATE INDEX idx_events_close_at    ON events(close_at);
CREATE INDEX idx_outcomes_event_id  ON outcomes(event_id);
CREATE INDEX idx_bets_user_id       ON bets(user_id);
CREATE INDEX idx_bets_outcome_id    ON bets(outcome_id);
CREATE INDEX idx_bets_status        ON bets(status);