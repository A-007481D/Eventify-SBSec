-- Create an admin user
-- Password is 'admin123' (bcrypt hashed)
INSERT INTO users (name, email, password, role) 
VALUES ('Admin User', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Create a test organizer
-- Password is 'organizer123'
INSERT INTO users (name, email, password, role) 
VALUES ('Organizer User', 'organizer@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_ORGANIZER')
ON CONFLICT (email) DO NOTHING;

-- Create a test user
-- Password is 'user123'
INSERT INTO users (name, email, password, role) 
VALUES ('Test User', 'user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_USER')
ON CONFLICT (email) DO NOTHING;
