create table if not exists frobot
(
    id                     uuid        not null primary key,
    user_id                bigint      not null,
    battery_level          text        not null,
    rock_garden_message_id bigint      null,
    rock_garden_board      varchar(64) null,
    created_at             timestamp   not null,
    updated_at             timestamp   not null
);
