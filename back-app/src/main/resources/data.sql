/*
-- Archivo data.sql para carga automática al arrancar la app (H2 en memoria)
-- Contiene tenant, usuario y producto de prueba

-- Insertar tenant
INSERT INTO tenants (id, name, contact_email, contact_phone, owner_id, address, active, plan_type, status, max_users, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'MiEmpresa2222', 'matybon2302@hotmail.com', '98765432312', '22222222-2222-2222-2222-222222222222', 'Avenida Siempre Viva 742', true, 'FREE', 'ACTIVE', 10, NOW(), NOW());

-- Insertar usuario (password en BCrypt)
INSERT INTO users (id, username, email, password, status, telephone, address, city, state_or_province, owner, tenant_id)
VALUES ('22222222-2222-2222-2222-222222222222', 'usuario1', 'matybon2010@hotmail.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '12345678392', 'Calle Falsa 123', 'Ciudad', 'Provincia', true, '11111111-1111-1111-1111-111111111111');

-- Asignar rol al usuario
INSERT INTO user_roles (user_id, role)
VALUES ('22222222-2222-2222-2222-222222222222', 'OWNER');

-- Insertar producto de prueba
INSERT INTO product (id, tenant_id, name, sku, description, price, status, category, image_url, is_available, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Producto Demo', 'SKU-PRUEBA-001', 'Producto creado para pruebas', 100.00, 'ACTIVE', 'General', NULL, true, NOW(), NOW());

-- Clientes de prueba
INSERT INTO customer (id, tenant_id, name, email, phone_number, address, latitude, longitude, postal_code, city, state, country, notes, doorbell, type, created_at, updated_at)
VALUES
('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Cliente Uno', 'cliente1@example.com', '1111111111', 'Calle Alfa 1', -34.6037, -58.3816, '1000', 'Ciudad', 'Provincia', 'Pais', 'Notas cliente 1', NULL, 'REGULAR', NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Cliente Dos', 'cliente2@example.com', '2222222222', 'Calle Beta 2', -34.6100, -58.4000, '2000', 'Ciudad', 'Provincia', 'Pais', 'Notas cliente 2', NULL, 'REGULAR', NOW(), NOW());

-- Órdenes de prueba (vinculadas a los customers)
INSERT INTO orders (id, tenant_id, order_number, customer_id, order_date, status, total_amount, notes, created_at, updated_at)
VALUES
('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'ORD-0001', '44444444-4444-4444-4444-444444444444', '2025-11-07 10:00:00', 'PENDING', 150.00, 'Orden prueba 1', NOW(), NOW()),
('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111', 'ORD-0002', '55555555-5555-5555-5555-555555555555', '2025-11-07 11:00:00', 'PENDING', 200.00, 'Orden prueba 2', NOW(), NOW());

-- Vehículo de prueba
INSERT INTO vehicles (id, tenant_id, plate, model, capacity, created_at, updated_at)
VALUES ('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', 'ABC-123', 'Van Modelo', 100, NOW(), NOW());

-- Usuario dealer
INSERT INTO users (id, username, email, password, status, telephone, address, city, state_or_province, owner, tenant_id)
VALUES ('99999999-9999-9999-9999-999999999999', 'dealer1', 'dealer1@example.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '3333333333', 'Calle Dealer 1', 'Ciudad', 'Provincia', false, '11111111-1111-1111-1111-111111111111');

INSERT INTO user_roles (user_id, role)
VALUES ('99999999-9999-9999-9999-999999999999', 'DEALER');
*/