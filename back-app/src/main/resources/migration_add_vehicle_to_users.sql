-- Migration: Add vehicle relationship to users table
-- Description: Allows dealers to have a vehicle assigned permanently
-- Date: 2025-11-10

-- Add vehicle_id column to users table
ALTER TABLE users ADD COLUMN vehicle_id VARCHAR(36) NULL;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_vehicle
    FOREIGN KEY (vehicle_id)
    REFERENCES vehicles(id)
    ON DELETE SET NULL;

-- Create index for better query performance
CREATE INDEX idx_users_vehicle_id ON users(vehicle_id);

-- Comments for documentation
COMMENT ON COLUMN users.vehicle_id IS 'ID of the vehicle assigned to this user (dealer)';