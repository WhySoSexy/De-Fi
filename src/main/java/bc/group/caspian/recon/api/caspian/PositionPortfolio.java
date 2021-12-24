package bc.group.caspian.recon.api.caspian;

public enum PositionPortfolio {
    OTCFLOW, EXCHANGE, RFQ;

    public static boolean isOTC(String portfolio) {
        return portfolio.equalsIgnoreCase(OTCFLOW.name());
    }

    public static boolean isRFQ(String portfolio) {
        return portfolio.equalsIgnoreCase(RFQ.name());
    }

    public static boolean isExchange(String portfolio) {
        return portfolio.equalsIgnoreCase(EXCHANGE.name());
    }

    public static  String getOTCExchangePortfolio() {
        return OTCFLOW.name() + "/" + EXCHANGE.name();
    }
}
