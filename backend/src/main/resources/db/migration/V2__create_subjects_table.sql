CREATE TABLE IF NOT EXISTS subjects (
  id SERIAL PRIMARY KEY,
  "name" VARCHAR(64) NOT NULL
);

ALTER TABLE subjects
ADD CONSTRAINT subjects_name_unique UNIQUE ("name");
