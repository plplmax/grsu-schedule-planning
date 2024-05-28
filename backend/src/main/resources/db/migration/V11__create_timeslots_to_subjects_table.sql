CREATE TABLE IF NOT EXISTS timeslotstosubjects (
  "timeslotId" INT,
  "subjectId" INT,
  CONSTRAINT pk_TimeslotsToSubjects PRIMARY KEY ("timeslotId", "subjectId"),
  CONSTRAINT fk_timeslotstosubjects_timeslotid__id FOREIGN KEY ("timeslotId") REFERENCES timeslots(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_timeslotstosubjects_subjectid__id FOREIGN KEY ("subjectId") REFERENCES subjects(id) ON DELETE CASCADE ON UPDATE CASCADE
);
