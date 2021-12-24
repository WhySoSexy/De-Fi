SET @time = '2020-07-12 16:00:00';

UPDATE cash_transaction
SET update_timestamp = @time
WHERE update_timestamp > @time;