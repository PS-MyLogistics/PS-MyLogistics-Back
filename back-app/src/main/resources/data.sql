-- Archivo data.sql para carga automática al arrancar la app (H2 en memoria)
-- Contiene tenant, usuario, productos, clientes y órdenes de prueba

-- Insertar tenant
INSERT INTO tenants (id, name, contact_email, contact_phone, owner_id, address, active, plan_type, status, max_users, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'MiEmpresa2222', 'matybon2302@hotmail.com', '98765432312', '22222222-2222-2222-2222-222222222222', 'Avenida Siempre Viva 742', true, 'PREMIUM', 'ACTIVE', 15, NOW(), NOW());

-- Insertar usuario OWNER (password: ContraSeña_Perro1)
INSERT INTO users (id, username, email, password, status, telephone, address, city, state_or_province, owner, tenant_id)
VALUES ('22222222-2222-2222-2222-222222222222', 'usuario1', 'matybon2010@hotmail.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '12345678392', 'Calle Falsa 123', 'Ciudad', 'Provincia', true, '11111111-1111-1111-1111-111111111111');

INSERT INTO user_roles (user_id, role)
VALUES ('22222222-2222-2222-2222-222222222222', 'OWNER');

-- Zonas de distribución
INSERT INTO zones (id, tenant_id, name, description, is_active, created_at, updated_at)
VALUES
('a0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Zona Norte', 'Zona Norte de Córdoba', true, NOW(), NOW()),
('a0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Zona Sur', 'Zona Sur de Córdoba', true, NOW(), NOW());

-- Productos de bebidas
INSERT INTO product (id, tenant_id, name, sku, description, price, status, category, image_url, is_available, created_at, updated_at)
VALUES
('b0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Coca Cola 2.25L', 'BEB-COCA-2L', 'Coca Cola 2.25 litros', 150.00, 'ACTIVE', 'Bebidas', NULL, true, NOW(), NOW()),
('b0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Fanta 2.25L', 'BEB-FANTA-2L', 'Fanta 2.25 litros', 140.00, 'ACTIVE', 'Bebidas', NULL, true, NOW(), NOW()),
('b0000003-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Sprite 2.25L', 'BEB-SPRITE-2L', 'Sprite 2.25 litros', 140.00, 'ACTIVE', 'Bebidas', NULL, true, NOW(), NOW()),
('b0000004-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'Agua Mineral 2L', 'BEB-AGUA-2L', 'Agua Mineral 2 litros', 80.00, 'ACTIVE', 'Bebidas', NULL, true, NOW(), NOW()),
('b0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'Cerveza Quilmes 1L', 'BEB-QUILMES-1L', 'Cerveza Quilmes 1 litro', 200.00, 'ACTIVE', 'Bebidas', NULL, true, NOW(), NOW());

-- Clientes de Córdoba, Argentina
INSERT INTO customer (id, tenant_id, name, email, phone_number, address, latitude, longitude, postal_code, city, state, country, notes, doorbell, type, is_active, zone_id, created_at, updated_at)
VALUES
-- Zona Norte
('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Daniel Bogdan', 'danibogdan_17@hotmail.com', '3512371898', 'Agua de la pilona 7852', -31.3278095, -64.2703887, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'María González', 'mgonzalez@email.com', '3514523698', 'padre lozano 627', -31.4150762, -64.2194577, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000002-0000-0000-0000-000000000002', NOW(), NOW()),
('217a364f-56ce-4eb4-af4b-94688c0817fc', '11111111-1111-1111-1111-111111111111', 'Juan Pérez', 'jperez@email.com', '3517894561', 'octavio pinto 3120', -31.3846725, -64.2203902, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('89122b74-9c64-4244-badb-9c57c39ee087', '11111111-1111-1111-1111-111111111111', 'Ana Martínez', 'amartinez@email.com', '3519876543', 'rafael nuñez 4014', -31.3713224, -64.2305245, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('19a87354-0400-4782-aa5a-f90790623345', '11111111-1111-1111-1111-111111111111', 'Carlos López', 'clopez@email.com', '3513216549', 'fragata sarmiento 570', -31.4139880, -64.2184453, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000002-0000-0000-0000-000000000002', NOW(), NOW()),
-- Zona Sur
('c0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Laura Fernández', 'lfernandez@email.com', '3518529637', 'padre lozano 400', -31.4260172, -64.2221656, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000002-0000-0000-0000-000000000002', NOW(), NOW()),
('c0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Roberto Silva', 'rsilva@email.com', '3516547892', 'octavio pinto 3195', -31.3841622, -64.2213757, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('c0000003-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Sofía Romero', 'sromero@email.com', '3512589631', 'boulevard los granaderos 2353', -31.3500000, -64.2200000, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('c0000004-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'Diego Morales', 'dmorales@email.com', '3519638527', 'rodriguez del busto 2781', -31.3758561, -64.2228176, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000001-0000-0000-0000-000000000001', NOW(), NOW()),
('c0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'Lucía Torres', 'ltorres@email.com', '3514567891', 'mercedario 1764', -31.4324420, -64.2143365, '5000', 'Córdoba', 'Córdoba', 'Argentina', NULL, '1', 'REGULAR', true, 'a0000002-0000-0000-0000-000000000002', NOW(), NOW());

-- Órdenes de prueba (una para cada cliente)
INSERT INTO orders (id, tenant_id, order_number, customer_id, order_date, status, total_amount, notes, created_at, updated_at)
VALUES
('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'ORD-0001', '44444444-4444-4444-4444-444444444444', NOW(), 'PENDING', 290.00, 'Pedido Daniel Bogdan', NOW(), NOW()),
('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111', 'ORD-0002', '55555555-5555-5555-5555-555555555555', NOW(), 'PENDING', 280.00, 'Pedido María González', NOW(), NOW()),
('7a1b2525-a1e1-4e45-a23d-c569b381b5c1', '11111111-1111-1111-1111-111111111111', 'ORD-0003', '217a364f-56ce-4eb4-af4b-94688c0817fc', NOW(), 'PENDING', 150.00, 'Pedido Juan Pérez', NOW(), NOW()),
('9bf5a088-f8f2-432d-9a07-6a421a726010', '11111111-1111-1111-1111-111111111111', 'ORD-0004', '89122b74-9c64-4244-badb-9c57c39ee087', NOW(), 'PENDING', 140.00, 'Pedido Ana Martínez', NOW(), NOW()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'ORD-0005', '19a87354-0400-4782-aa5a-f90790623345', NOW(), 'PENDING', 200.00, 'Pedido Carlos López', NOW(), NOW()),
('f0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'ORD-0006', 'c0000001-0000-0000-0000-000000000001', NOW(), 'PENDING', 220.00, 'Pedido Laura Fernández', NOW(), NOW()),
('f0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'ORD-0007', 'c0000002-0000-0000-0000-000000000002', NOW(), 'PENDING', 140.00, 'Pedido Roberto Silva', NOW(), NOW()),
('f0000003-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'ORD-0008', 'c0000003-0000-0000-0000-000000000003', NOW(), 'PENDING', 80.00, 'Pedido Sofía Romero', NOW(), NOW()),
('f0000004-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'ORD-0009', 'c0000004-0000-0000-0000-000000000004', NOW(), 'PENDING', 150.00, 'Pedido Diego Morales', NOW(), NOW()),
('f0000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'ORD-0010', 'c0000005-0000-0000-0000-000000000005', NOW(), 'PENDING', 340.00, 'Pedido Lucía Torres', NOW(), NOW());

-- Items de pedido (productos de bebidas)
INSERT INTO order_item (id, order_id, product_id, quantity, unit_price, subtotal, notes)
VALUES
-- Pedido 1: Daniel Bogdan
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '66666666-6666-6666-6666-666666666666', 'b0000001-0000-0000-0000-000000000001', 1, 150.00, 150.00, NULL),
('d0000001-0000-0000-0000-000000000001', '66666666-6666-6666-6666-666666666666', 'b0000002-0000-0000-0000-000000000002', 1, 140.00, 140.00, NULL),
-- Pedido 2: María González
('cccccccc-cccc-cccc-cccc-cccccccccccc', '77777777-7777-7777-7777-777777777777', 'b0000002-0000-0000-0000-000000000002', 2, 140.00, 280.00, NULL),
-- Pedido 3: Juan Pérez
('dddddddd-dddd-dddd-dddd-dddddddddddd', '7a1b2525-a1e1-4e45-a23d-c569b381b5c1', 'b0000001-0000-0000-0000-000000000001', 1, 150.00, 150.00, NULL),
-- Pedido 4: Ana Martínez
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '9bf5a088-f8f2-432d-9a07-6a421a726010', 'b0000003-0000-0000-0000-000000000003', 1, 140.00, 140.00, NULL),
-- Pedido 5: Carlos López
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'b0000005-0000-0000-0000-000000000005', 1, 200.00, 200.00, NULL),
-- Pedido 6: Laura Fernández
('d0000002-0000-0000-0000-000000000002', 'f0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', 1, 150.00, 150.00, NULL),
('d0000003-0000-0000-0000-000000000003', 'f0000001-0000-0000-0000-000000000001', 'b0000004-0000-0000-0000-000000000004', 1, 80.00, 80.00, NULL),
-- Pedido 7: Roberto Silva
('d0000004-0000-0000-0000-000000000004', 'f0000002-0000-0000-0000-000000000002', 'b0000003-0000-0000-0000-000000000003', 1, 140.00, 140.00, NULL),
-- Pedido 8: Sofía Romero
('d0000005-0000-0000-0000-000000000005', 'f0000003-0000-0000-0000-000000000003', 'b0000004-0000-0000-0000-000000000004', 1, 80.00, 80.00, NULL),
-- Pedido 9: Diego Morales
('d0000006-0000-0000-0000-000000000006', 'f0000004-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000001', 1, 150.00, 150.00, NULL),
-- Pedido 10: Lucía Torres
('d0000007-0000-0000-0000-000000000007', 'f0000005-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000001', 1, 150.00, 150.00, NULL),
('d0000008-0000-0000-0000-000000000008', 'f0000005-0000-0000-0000-000000000005', 'b0000004-0000-0000-0000-000000000004', 1, 80.00, 80.00, NULL),
('d0000009-0000-0000-0000-000000000009', 'f0000005-0000-0000-0000-000000000005', 'b0000003-0000-0000-0000-000000000003', 1, 140.00, 140.00, NULL);

-- Vehículos
INSERT INTO vehicles (id, tenant_id, plate, model, capacity, created_at, updated_at)
VALUES
('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', 'ABC-123', 'Ford Transit', 100, NOW(), NOW()),
('e0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'DEF-456', 'Mercedes Sprinter', 150, NOW(), NOW()),
('e0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'GHI-789', 'Renault Master', 120, NOW(), NOW());

-- Usuarios dealers (password: ContraSeña_Perro1 para dealer1 y dealer2, Dani123! para dan)
-- Hash BCrypt para "Dani123!": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, username, email, password, status, telephone, address, city, state_or_province, owner, tenant_id)
VALUES
('99999999-9999-9999-9999-999999999999', 'dealer1', 'dealer1@example.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '3513336666', 'Av. Colón 1234', 'Córdoba', 'Córdoba', false, '11111111-1111-1111-1111-111111111111'),
('d0000001-0000-0000-0000-000000000001', 'dealer2', 'dealer2@example.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '3517778888', 'Av. Vélez Sarsfield 567', 'Córdoba', 'Córdoba', false, '11111111-1111-1111-1111-111111111111'),
('d0000002-0000-0000-0000-000000000002', 'dan', 'danibogdan_17@hotmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', '3512371898', 'Barrio Alto Verde', 'Córdoba', 'Córdoba', false, '11111111-1111-1111-1111-111111111111');

INSERT INTO user_roles (user_id, role)
VALUES
('99999999-9999-9999-9999-999999999999', 'DEALER'),
('d0000001-0000-0000-0000-000000000001', 'DEALER'),
('d0000002-0000-0000-0000-000000000002', 'DEALER');
