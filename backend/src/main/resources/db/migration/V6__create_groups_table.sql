CREATE TABLE IF NOT EXISTS "groups" (
  id SERIAL PRIMARY KEY,
  "number" INT NOT NULL,
  letter CHAR NOT NULL
);

ALTER TABLE "groups"
ADD CONSTRAINT groups_number_letter_unique UNIQUE ("number", letter);
