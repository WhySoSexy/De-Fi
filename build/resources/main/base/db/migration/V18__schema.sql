create table if not exists platform_rfq_trade
(
    `id` bigint auto_increment,
    `trade_id` bigint not null,
    `completed` boolean not null,
    `date_created` timestamp default CURRENT_TIMESTAMP,
    `last_updated` timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key (`id`)
    )ENGINE=InnoDB DEFAULT CHARSET=UTF8;
CREATE UNIQUE INDEX idx_platform_rfq_trade_trade_id ON platform_rfq_trade (trade_id);
CREATE INDEX idx_platform_rfq_trade_completed ON platform_rfq_trade (completed);