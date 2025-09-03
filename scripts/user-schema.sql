-- User 관련 테이블 DDL

-- 사용자 역할 타입
CREATE TYPE user_role AS ENUM ('JOB_SEEKER', 'RECRUITER');

-- 사용자 상태 타입  
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED');

-- 사용자 기본 정보 테이블
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    status user_status NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- 구직자 프로필 테이블
CREATE TABLE job_seeker_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    
    -- 개인 정보
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(10),
    
    -- 주소 정보
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'South Korea',
    
    -- 포트폴리오 기본 정보
    profile_image_url VARCHAR(500),
    summary TEXT,
    career_objective TEXT,
    
    -- 희망 조건
    desired_position VARCHAR(100),
    desired_salary_min INTEGER,
    desired_salary_max INTEGER,
    desired_location VARCHAR(255),
    available_start_date DATE,
    
    -- 추가 정보
    website_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT salary_range_check CHECK (desired_salary_min IS NULL OR desired_salary_max IS NULL OR desired_salary_min <= desired_salary_max)
);

-- 채용담당자 프로필 테이블  
CREATE TABLE recruiter_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    
    -- 개인 정보
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    
    -- 회사 정보
    company_name VARCHAR(255) NOT NULL,
    company_website VARCHAR(500),
    company_description TEXT,
    department VARCHAR(100),
    position VARCHAR(100),
    
    -- 인증 정보
    business_registration_number VARCHAR(50),
    is_company_verified BOOLEAN NOT NULL DEFAULT FALSE,
    company_verification_document_url VARCHAR(500),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 학력 정보 테이블
CREATE TABLE educations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_profile_id UUID NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    
    institution_name VARCHAR(255) NOT NULL,
    degree VARCHAR(100),
    major VARCHAR(100),
    minor VARCHAR(100),
    gpa DECIMAL(3,2),
    max_gpa DECIMAL(3,2),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT gpa_range_check CHECK (gpa IS NULL OR (gpa >= 0 AND gpa <= max_gpa))
);

-- 경력 정보 테이블
CREATE TABLE work_experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_profile_id UUID NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    
    company_name VARCHAR(255) NOT NULL,
    position VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    employment_type VARCHAR(50), -- FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    achievements TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT work_date_check CHECK (end_date IS NULL OR start_date <= end_date)
);

-- 기술 스택 마스터 테이블
CREATE TABLE skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50), -- PROGRAMMING_LANGUAGE, FRAMEWORK, DATABASE, TOOL, etc.
    description TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 구직자 기술 스택 테이블
CREATE TABLE job_seeker_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_profile_id UUID NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level INTEGER NOT NULL DEFAULT 1, -- 1-5 scale
    years_of_experience INTEGER,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(job_seeker_profile_id, skill_id),
    CONSTRAINT proficiency_range CHECK (proficiency_level >= 1 AND proficiency_level <= 5)
);

-- 자격증 테이블
CREATE TABLE certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_profile_id UUID NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    
    name VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    credential_id VARCHAR(255),
    credential_url VARCHAR(500),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT cert_date_check CHECK (expiry_date IS NULL OR issue_date <= expiry_date)
);

-- 파일 업로드 테이블
CREATE TABLE uploaded_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_type VARCHAR(50), -- RESUME, COVER_LETTER, PORTFOLIO, CERTIFICATE, etc.
    
    -- OCR 추출 정보
    extracted_text TEXT,
    ocr_processed BOOLEAN NOT NULL DEFAULT FALSE,
    ocr_processed_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT file_size_check CHECK (file_size > 0)
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_job_seeker_profiles_user_id ON job_seeker_profiles(user_id);
CREATE INDEX idx_recruiter_profiles_user_id ON recruiter_profiles(user_id);
CREATE INDEX idx_recruiter_profiles_company_name ON recruiter_profiles(company_name);

CREATE INDEX idx_educations_job_seeker_profile_id ON educations(job_seeker_profile_id);
CREATE INDEX idx_work_experiences_job_seeker_profile_id ON work_experiences(job_seeker_profile_id);
CREATE INDEX idx_job_seeker_skills_job_seeker_profile_id ON job_seeker_skills(job_seeker_profile_id);
CREATE INDEX idx_job_seeker_skills_skill_id ON job_seeker_skills(skill_id);
CREATE INDEX idx_certifications_job_seeker_profile_id ON certifications(job_seeker_profile_id);

CREATE INDEX idx_skills_name ON skills(name);
CREATE INDEX idx_skills_category ON skills(category);

CREATE INDEX idx_uploaded_files_user_id ON uploaded_files(user_id);
CREATE INDEX idx_uploaded_files_file_type ON uploaded_files(file_type);
CREATE INDEX idx_uploaded_files_created_at ON uploaded_files(created_at);

-- 업데이트 트리거 생성 (updated_at 자동 업데이트)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_job_seeker_profiles_updated_at BEFORE UPDATE ON job_seeker_profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_recruiter_profiles_updated_at BEFORE UPDATE ON recruiter_profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_educations_updated_at BEFORE UPDATE ON educations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_work_experiences_updated_at BEFORE UPDATE ON work_experiences FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_certifications_updated_at BEFORE UPDATE ON certifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();