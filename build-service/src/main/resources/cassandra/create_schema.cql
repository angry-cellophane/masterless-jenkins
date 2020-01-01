CREATE KEYSPACE junkins WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}
;

CREATE TABLE IF NOT EXISTS junkins.builds (
    build_id text,
    job_id text,
    number int,
    status text,
    result text,
    started_ts bigint,
    finished_ts bigint,
    last_update_ts bigint,
    PRIMARY KEY ((job_id), number)
)
WITH CLUSTERING ORDER BY (number DESC)
;

CREATE TABLE IF NOT EXISTS junkins.build_steps (
    build_id text,
    step_id int,
    parent_id int,
    name text,
    result text,
    log text,
    started_ts bigint,
    finished_ts bigint,
    last_update_ts bigint,
    PRIMARY KEY ((build_id), step_id)
)
WITH CLUSTERING ORDER BY (step_id DESC)
;
