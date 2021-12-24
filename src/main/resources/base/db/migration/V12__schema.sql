create table if not exists scheduler_status
(
    `id` bigint auto_increment,
    `offset` bigint not null,
    `name` varchar(40) not null,
    `from_ts` timestamp default CURRENT_TIMESTAMP,
    `to_ts` timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     primary key (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=UTF8;