package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.cep.*;
import bc.group.caspian.recon.api.platform.TradeSideEnum;
import bc.group.caspian.recon.domain.reconInflux.CepHedgeTradeMeasurement;
import bc.group.caspian.recon.service.api.cep.CepClient;
import bc.group.caspian.recon.service.config.CepHedgeTradeProperties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class CepHedgeTradeService {

    private final static Logger logger = LoggerFactory.getLogger(CepHedgeTradeService.class);
    private final CepClient cepClient;
    private final CepHedgeTradeProperties properties;

    public CepHedgeTradeService(CepClient cepClient, CepHedgeTradeProperties properties) {
        this.cepClient = cepClient;
        this.properties = properties;
    }

    public CepHedgeTradeResponse getResponse(Long id) {
        CepHedgeTradeResponse response;
        try {
            CepHedgeTradeRequest request = getRequest(id);
            response = cepClient.getCepTrades(request);
            if (response != null && response.getRfqTradeList() != null) {
                return response;
            }
        } catch (Exception er) {
            logger.error("Failed to get rfq hedge trades", er);
            throw er;
        }
        return response;
    }

    public CepHedgeTradeResponse getResponseRetry(List<Long> ids) {
        CepHedgeTradeResponse response;
        try {
            CepHedgeTradeRequest request = getRequestRetry(ids);
            response = cepClient.getCepTrades(request);
            if (response != null && response.getRfqTradeList() != null) {
                return response;
            }
        } catch (Exception er) {
            logger.error("Failed to get rfq hedge trades from retry", er);
            throw er;
        }
        return response;
    }

    private CepHedgeTradeRequest getRequest(Long from) {
        return CepHedgeTradeWithRangeIdRequest.builder()
                .fromId(from)
                .toId(from + properties.getBatchSize())
                .build();
    }

    private CepHedgeTradeRequest getRequestRetry(List<Long> list) {
        return CepHedgeTradeWithIdListRequest.builder()
                .idList(list)
                .build();
    }

    public List<CepHedgeTradeMeasurement> process(CepHedgeTradeResponse response) {
        return response.getRfqTradeFillList()
                .stream()
                .filter(trade -> properties.getValid().contains(trade.getStatus()))
                .filter(trade -> properties.getSiteGroups().contains(trade.getSiteGroup()))
                .map(hedge -> {

                    for (CepHedgeTrade trade : response.getRfqTradeList()) {
                        if (hedge.getOriginTradeId().equals(trade.getOriginTradeId())) {
                            BigDecimal settlementQty = hedge.getPrice().multiply(hedge.getTradedCurrencyAmount());

                            return CepHedgeTradeMeasurement.builder()
                                    .hedgeId(hedge.getFillId())
                                    .settlementCcy(hedge.getSettlementCurrency())
                                    .settlementQty(String.valueOf(settlementQty))
                                    .siteGroup(getSiteGroup(trade))
                                    .tradeCcy(hedge.getTradedCurrency())
                                    .tradeQty(String.valueOf(hedge.getTradedCurrencyAmount()))
                                    .venue(hedge.getVenue())
                                    .side(trade.getUserBuy() ? TradeSideEnum.SELL.name() : TradeSideEnum.BUY.name())
                                    .tradeDate(String.valueOf(hedge.getLastUpdated()))
                                    .cepId(trade.getId())
                                    .build();
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String getSiteGroup(CepHedgeTrade trade) {
        if (trade.getTreasuryUser() != null && properties.getTreasuryUsers().contains(trade.getTreasuryUser())) {
            return trade.getTreasuryUserSiteGroup();
        }
        return trade.getSiteGroup();
    }
}