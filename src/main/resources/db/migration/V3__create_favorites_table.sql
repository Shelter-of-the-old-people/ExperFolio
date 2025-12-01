-- Favorites 테이블 생성 (MVP - 최소 기능)
-- 리크루터가 구직자의 포트폴리오를 즐겨찾기하는 기능
-- Portfolio 중심 설계로 User FK 제거

CREATE TABLE favorites (
                           id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           recruiter_id        VARCHAR(255) NOT NULL,
                           job_seeker_id       VARCHAR(255) NOT NULL,
                           created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 제약조건 (FK 제거, Portfolio 중심 설계)
                           CONSTRAINT unique_favorite UNIQUE (recruiter_id, job_seeker_id),
                           CONSTRAINT check_different_users CHECK (recruiter_id != job_seeker_id)
);

-- 인덱스 생성 (최소한만)
CREATE INDEX idx_favorites_recruiter_id ON favorites(recruiter_id);
CREATE INDEX idx_favorites_created_at ON favorites(created_at DESC);

-- 코멘트 추가
COMMENT ON TABLE favorites IS '리크루터의 구직자 포트폴리오 즐겨찾기 (MVP - Portfolio 중심 설계)';
COMMENT ON COLUMN favorites.recruiter_id IS '즐겨찾기를 추가한 리크루터 ID (User ID)';
COMMENT ON COLUMN favorites.job_seeker_id IS '즐겨찾기된 구직자 ID (Portfolio의 userId)';
COMMENT ON COLUMN favorites.created_at IS '즐겨찾기 추가 시점';
