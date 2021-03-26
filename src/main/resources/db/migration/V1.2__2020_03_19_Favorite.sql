ALTER TABLE indicator_type
  ADD IF NOT EXISTS favorite BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS indicator_indicator_type_id_year_month_index ON public.indicator (indicator_type_id, year, month);