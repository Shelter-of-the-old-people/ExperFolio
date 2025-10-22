-- =====================================================
-- PostgreSQL Migration: Remove Email Verification & User Status
-- =====================================================
-- Execute this on PostgreSQL database
-- Usage: psql -U username -d database_name -f postgresql_migration.sql
-- =====================================================

\echo '================================================'
\echo 'Starting User Table Cleanup Migration...'
\echo '================================================'

BEGIN;

\echo ''
\echo '[1/4] Removing email verification columns...'

-- Drop indexes for email verification
DROP INDEX IF EXISTS idx_users_email_verified;
DROP INDEX IF EXISTS idx_users_email_verification_token;

-- Remove email verification columns
ALTER TABLE users DROP COLUMN IF EXISTS email_verified;
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_token;

\echo 'Email verification columns removed'

\echo ''
\echo '[2/4] Removing user status column...'

-- Drop status-related indexes
DROP INDEX IF EXISTS idx_users_status;
DROP INDEX IF EXISTS idx_users_role_status;
DROP INDEX IF EXISTS idx_users_email_status;
DROP INDEX IF EXISTS idx_users_active_users;

-- Remove status column (and its CHECK constraint)
ALTER TABLE users DROP COLUMN IF EXISTS status;

\echo 'User status column removed'

\echo ''
\echo '[3/4] Creating new optimized indexes...'

-- Create new composite index for role and deleted status
CREATE INDEX IF NOT EXISTS idx_users_role_deleted
ON users (role, deleted_at);

-- Create partial index for active users (not deleted)
CREATE INDEX IF NOT EXISTS idx_users_active_users
ON users (deleted_at) WHERE deleted_at IS NULL;

\echo 'New indexes created'

\echo ''
\echo '[4/4] Updating table metadata...'

-- Update table comment
COMMENT ON TABLE users IS
'User accounts for both job seekers and recruiters. Uses deleted_at for soft delete (active/inactive state).';

\echo 'Table metadata updated'

-- Verification
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
        CASE WHEN status_exists = 0 THEN 'REMOVED' ELSE 'STILL EXISTS' END;
    RAISE NOTICE '  email_verified column: %',
        CASE WHEN email_verified_exists = 0 THEN 'REMOVED' ELSE 'STILL EXISTS' END;
    RAISE NOTICE '  email_verification_token column: %',
        CASE WHEN email_token_exists = 0 THEN 'REMOVED' ELSE 'STILL EXISTS' END;
    RAISE NOTICE '';

    IF status_exists = 0 AND email_verified_exists = 0 AND email_token_exists = 0 THEN
        RAISE NOTICE 'SUCCESS: All target columns removed successfully!';
    ELSE
        RAISE WARNING 'Some columns were not removed. Please check manually.';
    END IF;
END $$;

COMMIT;

\echo ''
\echo '================================================'
\echo 'Migration Complete!'
\echo '================================================'

-- Display final structure
\echo ''
\echo 'Final Users Table Structure:'
\echo '------------------------------------'

SELECT
    column_name,
    data_type,
    CASE WHEN is_nullable = 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS nullable,
    COALESCE(column_default, '-') AS default_value
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

\echo ''
\echo 'Indexes on Users Table:'
\echo '------------------------------------'

SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;
