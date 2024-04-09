CREATE TABLE IF NOT EXISTS teacherstosubjects (
  "teacherId" INT,
  "subjectId" INT,
  CONSTRAINT pk_TeachersToSubjects PRIMARY KEY ("teacherId", "subjectId"),
  CONSTRAINT fk_teacherstosubjects_teacherid__id FOREIGN KEY ("teacherId") REFERENCES teachers(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_teacherstosubjects_subjectid__id FOREIGN KEY ("subjectId") REFERENCES subjects(id) ON DELETE CASCADE ON UPDATE CASCADE
);
