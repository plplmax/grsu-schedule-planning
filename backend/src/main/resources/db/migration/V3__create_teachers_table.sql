CREATE TABLE IF NOT EXISTS teachers (
  id SERIAL PRIMARY KEY,
  firstname VARCHAR(32) NOT NULL,
  lastname VARCHAR(32) NOT NULL
);

ALTER TABLE teachers
ADD CONSTRAINT teachers_firstname_lastname_unique UNIQUE (firstname, lastname);
