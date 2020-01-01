CREATE KEYSPACE junkins WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}
;

CREATE TABLE IF NOT EXISTS junkins.builds (
    build_id uuid,
    job_id uuid,
    number int,
    status text,
    result text,
    start_ts timestamp,
    finish_ts timestamp,
    last_update_ts timestamp,
    PRIMARY KEY ((job_id), number)
)
WITH CLUSTERING ORDER BY (number DESC)
;

CREATE TABLE IF NOT EXISTS junkins.build_steps (
    build_id uuid,
    step_id int,
    parent_id int,
    name text,
    result text,
    log text,
    start_ts timestamp,
    finish_ts timestamp,
    last_update_ts timestamp,
    PRIMARY KEY ((build_id), step_id)
)
WITH CLUSTERING ORDER BY (step_id DESC)
;
