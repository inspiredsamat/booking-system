CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE units (
                       id BIGSERIAL PRIMARY KEY,
                       owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       title VARCHAR(255),
                       description TEXT,
                       cost_per_day NUMERIC(10, 2) NOT NULL CHECK (cost_per_day > 0),
                       type VARCHAR(20) NOT NULL CHECK (type IN ('HOME', 'FLAT', 'APARTMENTS')),
                       number_of_rooms SMALLINT NOT NULL CHECK (number_of_rooms > 0),
                       floor SMALLINT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_units_owner ON units(owner_id);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          unit_id BIGINT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,
                          status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'PAID', 'EXPIRED')),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          CONSTRAINT booking_date_valid CHECK (start_date < end_date)
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_unit ON bookings(unit_id);

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
                          payment_time TIMESTAMP,
                          amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
                          status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'FAILED')),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_payments_booking ON payments(booking_id);

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        entity_type VARCHAR(50) NOT NULL,
                        entity_id BIGINT NOT NULL,
                        event_type VARCHAR(50) NOT NULL,
                        payload JSONB,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_events_entity ON events(entity_type, entity_id);