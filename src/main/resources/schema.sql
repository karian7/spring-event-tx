create table if not exists my_event
(
    id          int          not null auto_increment primary key,
    description varchar(255) not null
);

create table if not exists child
(
    id       int          not null auto_increment primary key,
    event_id varchar(255) not null
);

