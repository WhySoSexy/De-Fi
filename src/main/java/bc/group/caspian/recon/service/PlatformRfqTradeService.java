package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTrade;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeRequest;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResponse;
import bc.group.caspian.recon.api.platform.rfqTrades.PlatformRfqTradeResult;
import bc.group.caspian.recon.service.api.platform.PlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PlatformRfqTradeService {

    private final PlatformClient platformClient;
    private final static Logger logger = LoggerFactory.getLogger(PlatformRfqTradeService.class);

    public PlatformRfqTradeService(PlatformClient platformClient) {
        this.platformClient = platformClient;
    }

    public Optional<PlatformRfqTradeResult> getRfqTradesResult(String from, String to, List<String> siteGroups, Long offset, Long batchSize) {
        PlatformRfqTradeRequest request = PlatformRfqTradeRequest.builder()
                .dateCreatedFrom(from)
                .dateCreatedTo(to)
                .siteGroups(siteGroups)
                .offset(offset)
                .batchSize(batchSize)
                .build();;

        return getRfqTradesResult(request);
    }

    public Optional<PlatformRfqTradeResult> getRfqTradesResult(List<String> tradeIdList, Long offset, Long batchSize) {
        PlatformRfqTradeRequest request = PlatformRfqTradeRequest.builder()
                .tradeIdList(tradeIdList)
                .offset(offset)
                .batchSize(batchSize)
                .build();

        return getRfqTradesResult(request);
    }

    private Optional<PlatformRfqTradeResult> getRfqTradesResult(PlatformRfqTradeRequest request) {
        try {
            logger.info("Get Rfq trade request {}", request.toString());
            PlatformRfqTradeResponse response = platformClient.getRfqTrade(request);
            PlatformRfqTradeResult result = response.getResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            logger.error("Failed to fetch rfq trades from Platform api : %s", e);
            throw e;
        }
    }
}
