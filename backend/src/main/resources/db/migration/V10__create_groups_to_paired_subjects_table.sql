CREATE TABLE IF NOT EXISTS groupstopairedsubjects (
  id SERIAL PRIMARY KEY,
  "groupId" INT NOT NULL,
  "pairedSubjectsId" INT NOT NULL,
  CONSTRAINT fk_groupstopairedsubjects_groupid__id FOREIGN KEY ("groupId") REFERENCES "groups"(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_groupstopairedsubjects_pairedsubjectsid__id FOREIGN KEY ("pairedSubjectsId") REFERENCES pairedsubjects(id) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE groupstopairedsubjects
ADD CONSTRAINT groupstopairedsubjects_groupid_pairedsubjectsid_unique UNIQUE ("groupId", "pairedSubjectsId");
