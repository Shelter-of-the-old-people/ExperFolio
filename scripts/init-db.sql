-- Database initialization script for Experfolio
-- This script is executed when PostgreSQL container starts for the first time

-- Create database if not exists (handled by Docker environment variables)
-- CREATE DATABASE IF NOT EXISTS experfolio;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create user roles enum type
CREATE TYPE user_role AS ENUM ('JOB_SEEKER', 'RECRUITER');

-- Create initial indexes for better performance
-- These will be created by JPA/Hibernate, but we can prepare the database

-- Log the initialization
INSERT INTO pg_stat_statements_info (query) VALUES ('Database initialized for Experfolio application') 
ON CONFLICT DO NOTHING;