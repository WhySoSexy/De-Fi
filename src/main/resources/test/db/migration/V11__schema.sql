CREATE UNIQUE INDEX idx_otc_trade_booking_otc_booking_type_trade_id ON otc_trade_booking (otc_booking_type, trade_id);
CREATE INDEX idx_otc_trade_booking_otc_booking_type_last_updated ON otc_trade_booking (otc_booking_type, last_updated DESC);
CREATE INDEX idx_rfq_hedge_completed ON rfq_hedge (completed);
