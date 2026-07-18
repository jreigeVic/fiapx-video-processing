CREATE TABLE videos (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    source_object_key VARCHAR(1024) NOT NULL,
    result_object_key VARCHAR(1024),
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_videos_owner_user_id ON videos (owner_user_id);

CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);
