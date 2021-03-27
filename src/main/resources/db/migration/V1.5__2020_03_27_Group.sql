ALTER TABLE indicator_type
  ADD IF NOT EXISTS group_caption varchar(50) NOT NULL DEFAULT '-';

