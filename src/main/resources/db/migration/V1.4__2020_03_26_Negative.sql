ALTER TABLE indicator_type
  ADD IF NOT EXISTS negative BOOLEAN NOT NULL DEFAULT FALSE;

