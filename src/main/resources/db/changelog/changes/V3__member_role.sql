CREATE TYPE member_role AS ENUM ('MEMBER', 'OWNER');

ALTER TABLE travel_members
    ADD COLUMN role member_role NOT NULL DEFAULT 'MEMBER';

UPDATE travel_members tm
SET role = 'OWNER'
FROM travels t
WHERE tm.id_travel = t.id
  AND tm.id_user = t.owner_id;
