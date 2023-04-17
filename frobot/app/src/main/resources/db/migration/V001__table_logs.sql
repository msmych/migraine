create table if not exists logs
(
    id        serial primary key,
    timestamp timestamp,
    level     varchar(10),
    logger    varchar(100),
    message   text
);
