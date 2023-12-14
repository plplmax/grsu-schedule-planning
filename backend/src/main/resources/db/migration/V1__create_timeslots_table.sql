CREATE TABLE IF NOT EXISTS timeslots (
  id SERIAL PRIMARY KEY,
  "start" TIMESTAMP NOT NULL,
  "end" TIMESTAMP NOT NULL
);
ALTER TABLE
  timeslots
ADD
  CONSTRAINT timeslots_start_end_unique UNIQUE ("start", "end");
