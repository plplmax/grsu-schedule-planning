CREATE TABLE IF NOT EXISTS pairedsubjects (
  id SERIAL PRIMARY KEY,
  "firstSubjectId" INT NOT NULL,
  "secondSubjectId" INT NOT NULL,
  "count" INT NOT NULL,
  CONSTRAINT fk_pairedsubjects_firstsubjectid__id FOREIGN KEY ("firstSubjectId") REFERENCES subjects(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pairedsubjects_secondsubjectid__id FOREIGN KEY ("secondSubjectId") REFERENCES subjects(id) ON DELETE CASCADE ON UPDATE CASCADE
);
