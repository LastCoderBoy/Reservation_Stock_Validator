-- ========================================
-- LIMITED STOCK DROP - DATABASE SCHEMA
-- ========================================

-- ========================================
-- USERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);

-- ========================================
-- PRODUCTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    image_key VARCHAR(255),
    total_stock INTEGER NOT NULL,
    reserved_stock INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_product_price_range ON products(price);

-- ========================================
-- RESERVATIONS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reservation_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservation_product ON reservations(product_id);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservation_expires ON reservations(expires_at);

-- ========================================
-- ORDERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE REFERENCES reservations(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_product ON orders(product_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);

-- ========================================
-- INVENTORY LOGS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    reservation_id BIGINT REFERENCES reservations(id),
    order_id BIGINT REFERENCES orders(id),
    action VARCHAR(30) NOT NULL,
    quantity_change INTEGER NOT NULL,
    stock_before INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory_logs(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_action ON inventory_logs(action);
CREATE INDEX IF NOT EXISTS idx_inventory_created ON inventory_logs(created_at);

-- ========================================
-- REFRESH TOKENS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_user_id ON refresh_tokens(user_id);

-- ========================================
-- SCHEMA COMPLETE
-- ========================================
