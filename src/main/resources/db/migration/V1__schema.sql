CREATE TABLE app_users (
 id VARCHAR(36) PRIMARY KEY, name VARCHAR(80) NOT NULL, email VARCHAR(160) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL, role VARCHAR(16) NOT NULL, enabled BOOLEAN NOT NULL,
 created_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT chk_user_role CHECK (role IN ('ADMIN','CUSTOMER'))
);
CREATE TABLE categories (id VARCHAR(36) PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE food_items (
 id VARCHAR(36) PRIMARY KEY, name VARCHAR(80) NOT NULL, description VARCHAR(300) NOT NULL,
 price DECIMAL(12,2) NOT NULL, image VARCHAR(500) NOT NULL, vegetarian BOOLEAN NOT NULL,
 status VARCHAR(24) NOT NULL, deleted BOOLEAN NOT NULL DEFAULT FALSE, category_id VARCHAR(36) NOT NULL,
 FOREIGN KEY (category_id) REFERENCES categories(id),
 CONSTRAINT chk_food_price CHECK(price > 0),
 CONSTRAINT chk_food_status CHECK(status IN ('AVAILABLE','OUT_OF_STOCK'))
);
CREATE TABLE carts (id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL UNIQUE, FOREIGN KEY (user_id) REFERENCES app_users(id));
CREATE TABLE cart_items (
 id VARCHAR(36) PRIMARY KEY, cart_id VARCHAR(36) NOT NULL, food_id VARCHAR(36) NOT NULL, quantity INT NOT NULL,
 UNIQUE(cart_id,food_id), FOREIGN KEY(cart_id) REFERENCES carts(id), FOREIGN KEY(food_id) REFERENCES food_items(id),
 CONSTRAINT chk_cart_quantity CHECK(quantity BETWEEN 1 AND 20)
);
CREATE TABLE customer_orders (
 id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL, status VARCHAR(24) NOT NULL,
 subtotal DECIMAL(12,2) NOT NULL, delivery_fee DECIMAL(12,2) NOT NULL, total DECIMAL(12,2) NOT NULL,
 address VARCHAR(300) NOT NULL, phone VARCHAR(25) NOT NULL, created_at TIMESTAMP(6) NOT NULL, request_key VARCHAR(64) NOT NULL,
 UNIQUE(user_id,request_key), FOREIGN KEY(user_id) REFERENCES app_users(id),
 CONSTRAINT chk_order_status CHECK(status IN ('PLACED','PREPARING','DELIVERED','CANCELLED'))
);
CREATE TABLE order_items (
 id VARCHAR(36) PRIMARY KEY, order_id VARCHAR(36) NOT NULL, food_id VARCHAR(36) NOT NULL,
 food_name VARCHAR(80) NOT NULL, image VARCHAR(500) NOT NULL, unit_price DECIMAL(12,2) NOT NULL, quantity INT NOT NULL,
 FOREIGN KEY(order_id) REFERENCES customer_orders(id), FOREIGN KEY(food_id) REFERENCES food_items(id),
 CONSTRAINT chk_order_quantity CHECK(quantity BETWEEN 1 AND 20)
);
CREATE TABLE payments (
 id VARCHAR(36) PRIMARY KEY, order_id VARCHAR(36) NOT NULL UNIQUE, status VARCHAR(24) NOT NULL,
 method VARCHAR(32) NOT NULL, amount DECIMAL(12,2) NOT NULL, created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6),
 FOREIGN KEY(order_id) REFERENCES customer_orders(id),
 CONSTRAINT chk_payment_status CHECK(status IN ('PENDING','COMPLETED','FAILED'))
);
CREATE INDEX idx_order_user_date ON customer_orders(user_id,created_at);
CREATE INDEX idx_food_category ON food_items(category_id);
