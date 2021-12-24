create table if not exists platform_ledger_balance
(
    `id` bigint auto_increment,
    `user_id` varchar(36) not null,
    `completed` boolean not null,
    primary key (`id`)
    )ENGINE=InnoDB DEFAULT CHARSET=UTF8;
CREATE UNIQUE INDEX idx_platform_ledger_balance_user_id ON platform_ledger_balance (user_id);
CREATE INDEX idx_platform_ledger_balance_completed ON platform_ledger_balance (completed);