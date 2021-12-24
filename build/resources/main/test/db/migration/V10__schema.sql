create table if not exists otc_trade_booking
(
    id bigint auto_increment,
    trade_id bigint not null,
    otc_booking_type varchar(40) not null,
    last_updated timestamp default CURRENT_TIMESTAMP,
    create_datetime timestamp default CURRENT_TIMESTAMP,
    update_datetime timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key (id)
);
