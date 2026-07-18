CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    owner_user_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);
