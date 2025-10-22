-- =====================================================
-- Direct SQL Migration: Remove UserStatus from Database
-- =====================================================
-- Execute this script directly on your PostgreSQL database
-- to remove the status column and update indexes
-- =====================================================

-- Connect to your database first:
-- psql -U your_username -d your_database_name

BEGIN;

-- 1. Drop composite indexes that include status column
PRINT 'Dropping old indexes...';
DROP INDEX IF EXISTS idx_users_role_status;
DROP INDEX IF EXISTS idx_users_email_status;
DROP INDEX IF EXISTS idx_users_active_users;
DROP INDEX IF EXISTS idx_users_status;

-- 2. Remove the status column (this also removes the CHECK constraint)
PRINT 'Removing status column...';
ALTER TABLE users DROP COLUMN IF EXISTS status;

-- 3. Create new composite indexes without status
PRINT 'Creating new indexes...';
CREATE INDEX IF NOT EXISTS idx_users_role_deleted ON users (role, deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_active_users ON users (deleted_at) WHERE deleted_at IS NULL;

-- 4. Update table comment
COMMENT ON TABLE users IS 'User accounts for both job seekers and recruiters - Status removed, using deleted_at for active/inactive state';

-- 5. Verify the changes
DO $$
DECLARE
    status_column_exists INTEGER;
BEGIN
    SELECT COUNT(*) INTO status_column_exists
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'status';

    IF status_column_exists = 0 THEN
        RAISE NOTICE 'SUCCESS: Status column has been removed';
    ELSE
        RAISE WARNING 'WARNING: Status column still exists!';
    END IF;
END $$;

COMMIT;

-- Show final table structure
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

-- Show all indexes on users table
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;
