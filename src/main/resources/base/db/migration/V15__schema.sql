ALTER TABLE cash_transaction MODIFY COLUMN price decimal(31,18);
ALTER TABLE cash_transaction MODIFY COLUMN quantity decimal(31,18);
ALTER TABLE cash_transaction MODIFY COLUMN net_amount decimal(31,18);
ALTER TABLE cash_transaction MODIFY COLUMN gross_amount decimal(31,18);