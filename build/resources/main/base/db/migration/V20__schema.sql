ALTER table cash_transaction rename transaction;
ALTER TABLE transaction ADD traded_ccy varchar(255);
ALTER TABLE transaction ADD order_feed_id varchar(255);