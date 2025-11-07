DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'travel_status') THEN
CREATE TYPE travel_status AS ENUM ('ACTIVE','CLOSED');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'member_status') THEN
CREATE TYPE member_status AS ENUM ('INVITED','ACCEPTED','REJECTED','LEAVE');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'history_type') THEN
CREATE TYPE history_type AS ENUM ('CREATE','UPDATE','DELETE');
END IF;
END
$$;
