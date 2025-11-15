-- Archivo de datos iniciales para insertar tenant, usuario y producto
-- Reemplazar <BCRYPT_HASH_AQUI> por el hash generado con BCrypt para la contraseña: ContraSeña_Perro1
-- Ejemplo para generar hash en Java:
-- System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("ContraSeña_Perro1"));

-- Ten en cuenta: ajustar funciones de fecha (NOW()) según la DB (H2/Postgres/MariaDB)

-- Insertar tenant
INSERT INTO tenants (id, name, contact_email, contact_phone, owner_id, address, active, plan_type, status, max_users, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'MiEmpresa2222', 'matiasbon2302@hotmail.com', '98765432312', '22222222-2222-2222-2222-222222222222', 'Avenida Siempre Viva 742', true, 'FREE', 'ACTIVE', 10, NOW(), NOW());

-- Insertar usuario (password debe estar en formato BCrypt)
INSERT INTO users (id, username, email, password, status, telephone, address, city, state_or_province, owner, tenant_id)
VALUES ('22222222-2222-2222-2222-222222222222', 'usuario1', 'matybon2010@hotmail.com', '$2a$10$bdnEVjEWdjC1XOmm2qzUm.rc61SE8WYLA9YznjnVaku6AjbVGDHh.', 'ACTIVE', '12345678392', 'Calle Falsa 123', 'Ciudad', 'Provincia', true, '11111111-1111-1111-1111-111111111111');

-- Asignar rol al usuario
INSERT INTO user_roles (user_id, role)
VALUES ('22222222-2222-2222-2222-222222222222', 'OWNER');

-- Insertar producto de prueba
INSERT INTO product (id, tenant_id, name, sku, description, price, status, category, image_url, is_available, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Producto Demo', 'SKU-PRUEBA-001', 'Producto creado para pruebas', 100.00, 'ACTIVE', 'General', NULL, true, NOW(), NOW());

-- Si necesitás actualizar la contraseña después de generar el hash:
-- UPDATE users SET password = '<BCRYPT_HASH_AQUI>' WHERE id = '22222222-2222-2222-2222-222222222222';

-- Fin
