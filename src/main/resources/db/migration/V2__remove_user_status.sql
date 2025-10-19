-- =====================================================
-- Migration Script: Remove UserStatus from users table
-- Version: V2
-- Description: Remove status column and related indexes/constraints
-- Author: System Migration
-- Date: 2025-10-20
-- =====================================================

-- Step 1: Drop composite indexes that include status column
DROP INDEX IF EXISTS idx_users_role_status;
DROP INDEX IF EXISTS idx_users_email_status;
DROP INDEX IF EXISTS idx_users_active_users;

-- Step 2: Drop single column index for status
DROP INDEX IF EXISTS idx_users_status;

-- Step 3: Remove the status column
-- Note: This will also automatically remove the CHECK constraint
ALTER TABLE users DROP COLUMN IF EXISTS status;

-- Step 4: Create new composite indexes without status
CREATE INDEX IF NOT EXISTS idx_users_role_deleted ON users (role, deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_active_users ON users (deleted_at) WHERE deleted_at IS NULL;

-- Step 5: Add comment documenting the change
COMMENT ON TABLE users IS 'User accounts for both job seekers and recruiters - Status removed in V2';

-- =====================================================
-- Verification Queries (commented out - for manual verification)
-- =====================================================

-- Verify column removal:
-- SELECT column_name, data_type
-- FROM information_schema.columns
-- WHERE table_name = 'users' AND column_name = 'status';
-- (Should return 0 rows)

-- Verify indexes:
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'users';

-- Check remaining columns:
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'users'
-- ORDER BY ordinal_position;

-- =====================================================
-- Rollback Script (if needed - use with caution)
-- =====================================================

/*
-- To rollback this migration (NOT RECOMMENDED after data migration):

-- Re-add status column
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED'));

-- Recreate old indexes
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role_status ON users (role, status);
CREATE INDEX idx_users_email_status ON users (email, status);

-- Drop new indexes
DROP INDEX idx_users_role_deleted;
DROP INDEX idx_users_active_users;

-- Recreate old active users index
CREATE INDEX idx_users_active_users ON users (status, deleted_at) WHERE deleted_at IS NULL;

-- Restore comment
COMMENT ON TABLE users IS 'User accounts for both job seekers and recruiters';
COMMENT ON COLUMN users.status IS 'Account status: ACTIVE, INACTIVE, PENDING, SUSPENDED';
*/
