CREATE TABLE IF NOT EXISTS timeslots (
  id SERIAL PRIMARY KEY,
  "dayOfWeek" INT NOT NULL,
  "start" TIME NOT NULL,
  "end" TIME NOT NULL
);

ALTER TABLE timeslots
ADD CONSTRAINT timeslots_dayofweek_start_end_unique UNIQUE ("dayOfWeek", "start", "end");
