DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'member_role') THEN
            CREATE TYPE member_role AS ENUM ('MEMBER', 'OWNER');
        END IF;
    END
$$;

ALTER TABLE travel_members
    ADD COLUMN IF NOT EXISTS role member_role NOT NULL DEFAULT 'MEMBER';

UPDATE travel_members tm
SET role = 'OWNER'
FROM travels t
WHERE tm.id_travel = t.id
  AND tm.id_user = t.owner_id;
