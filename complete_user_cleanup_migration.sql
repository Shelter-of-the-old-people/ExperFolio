-- =====================================================
-- Complete User Table Cleanup Migration
-- =====================================================
-- This script removes:
-- 1. Email verification fields (email_verified, email_verification_token)
-- 2. User status field and related constraints
-- =====================================================

BEGIN;

PRINT '================================================';
PRINT 'Starting User Table Cleanup Migration...';
PRINT '================================================';

-- =====================================================
-- PART 1: Remove Email Verification Columns
-- =====================================================

PRINT '';
PRINT '[1/4] Removing email verification columns...';

-- Drop indexes for email verification
DROP INDEX IF EXISTS idx_users_email_verified;
DROP INDEX IF EXISTS idx_users_email_verification_token;

-- Remove email verification columns
ALTER TABLE users DROP COLUMN IF EXISTS email_verified;
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_token;

PRINT '✓ Email verification columns removed';

-- =====================================================
-- PART 2: Remove User Status Column
-- =====================================================

PRINT '';
PRINT '[2/4] Removing user status column...';

-- Drop status-related indexes
DROP INDEX IF EXISTS idx_users_status;
DROP INDEX IF EXISTS idx_users_role_status;
DROP INDEX IF EXISTS idx_users_email_status;
DROP INDEX IF EXISTS idx_users_active_users;

-- Remove status column (and its CHECK constraint)
ALTER TABLE users DROP COLUMN IF EXISTS status;

PRINT '✓ User status column removed';

-- =====================================================
-- PART 3: Create New Optimized Indexes
-- =====================================================

PRINT '';
PRINT '[3/4] Creating new optimized indexes...';

-- Create new composite index for role and deleted status
CREATE INDEX IF NOT EXISTS idx_users_role_deleted
ON users (role, deleted_at);

-- Create partial index for active users (not deleted)
CREATE INDEX IF NOT EXISTS idx_users_active_users
ON users (deleted_at) WHERE deleted_at IS NULL;

PRINT '✓ New indexes created';

-- =====================================================
-- PART 4: Update Table Metadata
-- =====================================================

PRINT '';
PRINT '[4/4] Updating table metadata...';

-- Update table comment
COMMENT ON TABLE users IS
'User accounts for both job seekers and recruiters. Uses deleted_at for soft delete (active/inactive state).';

PRINT '✓ Table metadata updated';

-- =====================================================
-- VERIFICATION
-- =====================================================

PRINT '';
PRINT '================================================';
PRINT 'Verification Results:';
PRINT '================================================';

-- Check removed columns
DO $$
DECLARE
    status_exists INTEGER;
    email_verified_exists INTEGER;
    email_token_exists INTEGER;
BEGIN
    SELECT COUNT(*) INTO status_exists
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'status';

    SELECT COUNT(*) INTO email_verified_exists
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'email_verified';

    SELECT COUNT(*) INTO email_token_exists
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'email_verification_token';

    RAISE NOTICE '';
    RAISE NOTICE 'Column Removal Status:';
    RAISE NOTICE '  status column: %',
        CASE WHEN status_exists = 0 THEN '✓ REMOVED' ELSE '✗ STILL EXISTS' END;
    RAISE NOTICE '  email_verified column: %',
        CASE WHEN email_verified_exists = 0 THEN '✓ REMOVED' ELSE '✗ STILL EXISTS' END;
    RAISE NOTICE '  email_verification_token column: %',
        CASE WHEN email_token_exists = 0 THEN '✓ REMOVED' ELSE '✗ STILL EXISTS' END;

    IF status_exists = 0 AND email_verified_exists = 0 AND email_token_exists = 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '✓✓✓ SUCCESS: All target columns removed successfully!';
    ELSE
        RAISE WARNING 'Some columns were not removed. Please check manually.';
    END IF;
END $$;

COMMIT;

PRINT '';
PRINT '================================================';
PRINT 'Migration Complete!';
PRINT '================================================';

-- =====================================================
-- Display Final Table Structure
-- =====================================================

PRINT '';
PRINT 'Final Users Table Structure:';
PRINT '------------------------------------';

SELECT
    column_name AS "Column Name",
    data_type AS "Data Type",
    CASE WHEN is_nullable = 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS "Nullable",
    COALESCE(column_default, '-') AS "Default Value"
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

PRINT '';
PRINT 'Indexes on Users Table:';
PRINT '------------------------------------';

SELECT
    indexname AS "Index Name",
    indexdef AS "Index Definition"
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;

-- =====================================================
-- Quick Rollback Script (Use with extreme caution!)
-- =====================================================

/*
-- ROLLBACK SCRIPT (Execute only if you need to undo changes)
-- WARNING: This should only be used immediately after migration
--          and before any new data has been created

BEGIN;

-- Restore email verification columns
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(255);

-- Restore status column with default 'ACTIVE' for all existing users
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED'));

-- Recreate old indexes
CREATE INDEX idx_users_email_verified ON users (email_verified);
CREATE INDEX idx_users_email_verification_token ON users (email_verification_token);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role_status ON users (role, status);
CREATE INDEX idx_users_email_status ON users (email, status);

-- Drop new indexes
DROP INDEX idx_users_role_deleted;
DROP INDEX idx_users_active_users;

-- Recreate old active users index
CREATE INDEX idx_users_active_users ON users (status, deleted_at) WHERE deleted_at IS NULL;

-- Restore original comments
COMMENT ON TABLE users IS 'User accounts for both job seekers and recruiters';
COMMENT ON COLUMN users.status IS 'Account status: ACTIVE, INACTIVE, PENDING, SUSPENDED';
COMMENT ON COLUMN users.email_verified IS 'Email verification status';
COMMENT ON COLUMN users.email_verification_token IS 'Token for email verification';

COMMIT;
*/
