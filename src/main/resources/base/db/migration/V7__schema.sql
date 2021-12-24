create table if not exists rfq_hedge
(
    id bigint auto_increment,
    completed boolean not null,
    primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=UTF8;
