-- User Domain DDL
-- PostgreSQL Database Schema for User Entity

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('JOB_SEEKER', 'RECRUITER')),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_password_reset_token ON users (password_reset_token);
CREATE INDEX idx_users_created_at ON users (created_at);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);

-- Create composite indexes for common queries
CREATE INDEX idx_users_role_deleted ON users (role, deleted_at);
CREATE INDEX idx_users_active_users ON users (deleted_at) WHERE deleted_at IS NULL;

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at on row update
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE users IS 'User accounts for both job seekers and recruiters';
COMMENT ON COLUMN users.id IS 'Primary key - UUID generated automatically';
COMMENT ON COLUMN users.email IS 'Unique email address for login';
COMMENT ON COLUMN users.password IS 'Encrypted password';
COMMENT ON COLUMN users.role IS 'User role: JOB_SEEKER or RECRUITER';
COMMENT ON COLUMN users.password_reset_token IS 'Token for password reset';
COMMENT ON COLUMN users.password_reset_expires IS 'Expiration time for password reset token';
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last login';
COMMENT ON COLUMN users.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp';

-- ===============================================
-- JOB SEEKER PROFILE TABLE
-- ===============================================

-- Create job_seeker_profiles table
CREATE TABLE job_seeker_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(10),
    address VARCHAR(255),
    state VARCHAR(100),
    postal_code VARCHAR(255),
    profile_image_url VARCHAR(255),
    summary TEXT,
    career_objective TEXT,
    desired_position VARCHAR(255),
    desired_salary_min INTEGER,
    desired_salary_max INTEGER,
    desired_location VARCHAR(255),
    available_start_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create job_seeker_urls table for ElementCollection
CREATE TABLE job_seeker_urls (
    job_seeker_id UUID NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (job_seeker_id, url)
);

-- Create indexes for job_seeker_profiles
CREATE INDEX idx_job_seeker_profiles_user_id ON job_seeker_profiles (user_id);
CREATE INDEX idx_job_seeker_profiles_desired_position ON job_seeker_profiles (desired_position);
CREATE INDEX idx_job_seeker_profiles_desired_location ON job_seeker_profiles (desired_location);
CREATE INDEX idx_job_seeker_profiles_salary_range ON job_seeker_profiles (desired_salary_min, desired_salary_max);
CREATE INDEX idx_job_seeker_profiles_available_date ON job_seeker_profiles (available_start_date);
CREATE INDEX idx_job_seeker_profiles_created_at ON job_seeker_profiles (created_at);

-- Create trigger for job_seeker_profiles updated_at
CREATE TRIGGER update_job_seeker_profiles_updated_at 
    BEFORE UPDATE ON job_seeker_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ===============================================
-- RECRUITER PROFILE TABLE
-- ===============================================

-- Create recruiter_profiles table
CREATE TABLE recruiter_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    company_name VARCHAR(255) NOT NULL,
    company_website VARCHAR(255),
    company_description TEXT,
    department VARCHAR(100),
    position VARCHAR(100),
    business_registration_number VARCHAR(255),
    is_company_verified BOOLEAN NOT NULL DEFAULT FALSE,
    company_verification_document_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for recruiter_profiles
CREATE INDEX idx_recruiter_profiles_user_id ON recruiter_profiles (user_id);
CREATE INDEX idx_recruiter_profiles_company_name ON recruiter_profiles (company_name);
CREATE INDEX idx_recruiter_profiles_department ON recruiter_profiles (department);
CREATE INDEX idx_recruiter_profiles_position ON recruiter_profiles (position);
CREATE INDEX idx_recruiter_profiles_company_verified ON recruiter_profiles (is_company_verified);
CREATE INDEX idx_recruiter_profiles_created_at ON recruiter_profiles (created_at);

-- Create trigger for recruiter_profiles updated_at
CREATE TRIGGER update_recruiter_profiles_updated_at 
    BEFORE UPDATE ON recruiter_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ===============================================
-- TABLE COMMENTS
-- ===============================================

-- Job Seeker Profile Comments
COMMENT ON TABLE job_seeker_profiles IS 'Detailed profiles for job seekers';
COMMENT ON COLUMN job_seeker_profiles.id IS 'Primary key - UUID generated automatically';
COMMENT ON COLUMN job_seeker_profiles.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN job_seeker_profiles.first_name IS 'Job seeker first name';
COMMENT ON COLUMN job_seeker_profiles.last_name IS 'Job seeker last name';
COMMENT ON COLUMN job_seeker_profiles.phone IS 'Contact phone number';
COMMENT ON COLUMN job_seeker_profiles.birth_date IS 'Date of birth';
COMMENT ON COLUMN job_seeker_profiles.gender IS 'Gender information';
COMMENT ON COLUMN job_seeker_profiles.address IS 'Full address';
COMMENT ON COLUMN job_seeker_profiles.state IS 'State or province';
COMMENT ON COLUMN job_seeker_profiles.postal_code IS 'ZIP or postal code';
COMMENT ON COLUMN job_seeker_profiles.profile_image_url IS 'URL to profile image';
COMMENT ON COLUMN job_seeker_profiles.summary IS 'Professional summary';
COMMENT ON COLUMN job_seeker_profiles.career_objective IS 'Career objectives and goals';
COMMENT ON COLUMN job_seeker_profiles.desired_position IS 'Desired job position';
COMMENT ON COLUMN job_seeker_profiles.desired_salary_min IS 'Minimum desired salary';
COMMENT ON COLUMN job_seeker_profiles.desired_salary_max IS 'Maximum desired salary';
COMMENT ON COLUMN job_seeker_profiles.desired_location IS 'Preferred work location';
COMMENT ON COLUMN job_seeker_profiles.available_start_date IS 'When available to start work';
COMMENT ON COLUMN job_seeker_profiles.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN job_seeker_profiles.updated_at IS 'Record last update timestamp';

COMMENT ON TABLE job_seeker_urls IS 'URLs associated with job seeker profiles (GitHub, portfolio, etc.)';
COMMENT ON COLUMN job_seeker_urls.job_seeker_id IS 'Foreign key to job_seeker_profiles';
COMMENT ON COLUMN job_seeker_urls.url IS 'URL value';

-- Recruiter Profile Comments
COMMENT ON TABLE recruiter_profiles IS 'Detailed profiles for recruiters and HR personnel';
COMMENT ON COLUMN recruiter_profiles.id IS 'Primary key - UUID generated automatically';
COMMENT ON COLUMN recruiter_profiles.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN recruiter_profiles.first_name IS 'Recruiter first name';
COMMENT ON COLUMN recruiter_profiles.last_name IS 'Recruiter last name';
COMMENT ON COLUMN recruiter_profiles.phone IS 'Contact phone number';
COMMENT ON COLUMN recruiter_profiles.company_name IS 'Company name';
COMMENT ON COLUMN recruiter_profiles.company_website IS 'Company website URL';
COMMENT ON COLUMN recruiter_profiles.company_description IS 'Company description';
COMMENT ON COLUMN recruiter_profiles.department IS 'Department or division';
COMMENT ON COLUMN recruiter_profiles.position IS 'Job position within company';
COMMENT ON COLUMN recruiter_profiles.business_registration_number IS 'Business registration or tax ID';
COMMENT ON COLUMN recruiter_profiles.is_company_verified IS 'Whether company has been verified';
COMMENT ON COLUMN recruiter_profiles.company_verification_document_url IS 'URL to verification documents';
COMMENT ON COLUMN recruiter_profiles.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN recruiter_profiles.updated_at IS 'Record last update timestamp';