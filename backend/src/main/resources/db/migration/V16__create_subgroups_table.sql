CREATE TABLE IF NOT EXISTS subgroups (
  id SERIAL PRIMARY KEY,
  "name" VARCHAR(64) NOT NULL,
  "divisionId" INT NOT NULL,
  CONSTRAINT fk_subgroups_divisionid__id FOREIGN KEY ("divisionId") REFERENCES divisions(id) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE subgroups
ADD CONSTRAINT subgroups_name_divisionid_unique UNIQUE ("name", "divisionId");

INSERT INTO subgroups (id, "name", "divisionId")
VALUES (1, 'Весь класс', 1);
