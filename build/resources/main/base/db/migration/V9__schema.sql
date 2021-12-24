ALTER TABLE rfq_hedge
    ADD create_datetime timestamp default CURRENT_TIMESTAMP;

ALTER TABLE rfq_hedge
    ADD update_datetime timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;