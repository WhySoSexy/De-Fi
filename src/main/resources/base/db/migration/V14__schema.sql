create table if not exists platform_hedge_trade
(
    `id` bigint auto_increment,
    `trade_id` bigint not null,
    `completed` boolean not null,
    `create_datetime` timestamp default CURRENT_TIMESTAMP,
    `update_datetime` timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     primary key (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=UTF8;
CREATE UNIQUE INDEX idx_platform_hedge_trade_trade_id ON platform_hedge_trade (trade_id);
CREATE INDEX idx_platform_hedge_trade_completed ON platform_hedge_trade (completed);