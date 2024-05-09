CREATE TABLE IF NOT EXISTS lessons (
  id SERIAL PRIMARY KEY,
  "groupId" INT NOT NULL,
  "teacherId" INT NOT NULL,
  "subjectId" INT NOT NULL,
  "roomId" INT NOT NULL,
  "timeslotId" INT NULL,
  CONSTRAINT fk_lessons_groupid__id FOREIGN KEY ("groupId") REFERENCES "groups"(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_lessons_roomid__id FOREIGN KEY ("roomId") REFERENCES rooms(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_lessons_timeslotid__id FOREIGN KEY ("timeslotId") REFERENCES timeslots(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_lessons_teacherid_subjectid__teacherid_subjectid FOREIGN KEY ("teacherId", "subjectId") REFERENCES teacherstosubjects("teacherId", "subjectId") ON DELETE CASCADE ON UPDATE CASCADE
);
