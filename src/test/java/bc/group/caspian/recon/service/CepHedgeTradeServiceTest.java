package bc.group.caspian.recon.service;

import bc.group.caspian.recon.api.cep.CepHedgeTrade;
import bc.group.caspian.recon.api.cep.CepHedgeTradeResponse;
import bc.group.caspian.recon.api.platform.TradeSideEnum;
import bc.group.caspian.recon.domain.reconInflux.CepHedgeTradeMeasurement;
import bc.group.caspian.recon.service.config.CepHedgeTradeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CepHedgeTradeServiceTest {

    @InjectMocks
    CepHedgeTradeService cepHedgeTradeService;

    @Mock
    CepHedgeTradeProperties properties;

    CepHedgeTrade client;
    CepHedgeTrade hedge1;
    CepHedgeTrade hedge2;

    @BeforeEach
    public void init() {
        client = new CepHedgeTrade();
        hedge1 = new CepHedgeTrade();
        hedge2 = new CepHedgeTrade();

        client.setId("440");
        client.setSettlementCurrencyAmount(new BigDecimal("3076.29000000"));
        client.setTradedCurrency("ETH");
        client.setStatus("HEDGED");
        client.setQuoteProvider("cc67ba0c-c229-4e1e-a537-a1790fce3ba6");
        client.setCustomer("");
        client.setSettlementCurrency("HKD");
        client.setSiteGroup("OSL_GROUP");
        client.setOriginTradeId("10021");
        client.setCcyPair("ETHUSD");
        client.setTradedCurrencyAmount(new BigDecimal("2.00000000"));
        client.setDateCreated(new BigDecimal("1536548145000"));
        client.setUserBuy(false);


        hedge1.setId("440");
        hedge1.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge1.setTradedCurrency("ETH");
        hedge1.setStatus("HEDGED");
        hedge1.setLastUpdated(new BigDecimal("1549939034000"));
        hedge1.setVenue("JUMP");
        hedge1.setSettlementCurrency("USD");
        hedge1.setFillId("57377393");
        hedge1.setPrice(new BigDecimal("212.10000000"));
        hedge1.setSiteGroup("OSL_GROUP");
        hedge1.setOriginTradeId("10021");
        hedge1.setCcyPair("ETHUSD");
        hedge1.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge1.setHedgeBuy(false);

        hedge2.setId("440");
        hedge2.setSettlementCurrencyAmount(new BigDecimal("41265.7763929950000000"));
        hedge2.setTradedCurrency("ETH");
        hedge2.setStatus("HEDGED");
        hedge2.setLastUpdated(new BigDecimal("1549939034000"));
        hedge2.setVenue("JUMP");
        hedge2.setSettlementCurrency("USD");
        hedge2.setFillId("57377394");
        hedge2.setPrice(new BigDecimal("213.10000000"));
        hedge2.setSiteGroup("OSL_GROUP");
        hedge2.setOriginTradeId("10021");
        hedge2.setCcyPair("ETHUSD");
        hedge2.setTradedCurrencyAmount(new BigDecimal("194.55811595"));
        hedge2.setHedgeBuy(false);

        Mockito.when(properties.getValid()).thenReturn(Arrays.asList("HEDGED", "CLOSED"));
    }

    @Test
    public void getCepHedgeTradesWhenStatusClosedTest() {
        Mockito.when(properties.getSiteGroups()).thenReturn(Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP"));
        client.setStatus("CLOSED");
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "gopalsamy.vaiyapuri+uatoslam_treasury@osl.com"));

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(2, result.size());
        CepHedgeTradeMeasurement measurement = result.get(0);

        assertEquals(hedge1.getFillId(), measurement.getHedgeId());
        assertEquals(hedge1.getSettlementCurrency(), measurement.getSettlementCcy());
        assertEquals(String.valueOf(hedge1.getPrice().multiply(hedge1.getTradedCurrencyAmount())), measurement.getSettlementQty());
        assertEquals(hedge1.getSiteGroup(), measurement.getSiteGroup());
        assertEquals(hedge1.getTradedCurrency(), measurement.getTradeCcy());
        assertEquals(String.valueOf(hedge1.getTradedCurrencyAmount()), measurement.getTradeQty());
        assertEquals(hedge1.getVenue(), measurement.getVenue());
        assertEquals(String.valueOf(hedge1.getLastUpdated()), measurement.getTradeDate());
        assertEquals(TradeSideEnum.BUY.name(), measurement.getSide());
    }

    @Test
    public void getCepHedgeTradesWhenStatusClosedAndHedgeTradesMissedTest() {
        client.setStatus("CLOSED");
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "gopalsamy.vaiyapuri+uatoslam_treasury@osl.com"));

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Collections.emptyList());
        response.setLatestId("1000");

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(0, result.size());
    }

    @Test
    public void getCepHedgeTradesWhenTreasuryUserIsNullTest() {
        Mockito.when(properties.getSiteGroups()).thenReturn(Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP"));
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "gopalsamy.vaiyapuri+uatoslam_treasury@osl.com"));

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(2, result.size());
        CepHedgeTradeMeasurement measurement = result.get(0);

        assertEquals(hedge1.getFillId(), measurement.getHedgeId());
        assertEquals(hedge1.getSettlementCurrency(), measurement.getSettlementCcy());
        assertEquals(String.valueOf(hedge1.getPrice().multiply(hedge1.getTradedCurrencyAmount())), measurement.getSettlementQty());
        assertEquals(hedge1.getSiteGroup(), measurement.getSiteGroup());
        assertEquals(hedge1.getTradedCurrency(), measurement.getTradeCcy());
        assertEquals(String.valueOf(hedge1.getTradedCurrencyAmount()), measurement.getTradeQty());
        assertEquals(hedge1.getVenue(), measurement.getVenue());
        assertEquals(String.valueOf(hedge1.getLastUpdated()), measurement.getTradeDate());
        assertEquals(TradeSideEnum.BUY.name(), measurement.getSide());
    }

    @Test
    public void getCepHedgeTradesWhenTreasuryUserNotNullTest() {
        Mockito.when(properties.getSiteGroups()).thenReturn(Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP"));
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        client.setTreasuryUser("prakash.konagi+treasury_ds@osl.com");
        client.setTreasuryUserSiteGroup("OSLSG_GROUP");

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(2, result.size());
        CepHedgeTradeMeasurement measurement = result.get(0);

        assertEquals(hedge1.getFillId(), measurement.getHedgeId());
        assertEquals(hedge1.getSettlementCurrency(), measurement.getSettlementCcy());
        assertEquals(String.valueOf(hedge1.getPrice().multiply(hedge1.getTradedCurrencyAmount())), measurement.getSettlementQty());
        assertEquals(client.getTreasuryUserSiteGroup(), measurement.getSiteGroup());
        assertEquals(hedge1.getTradedCurrency(), measurement.getTradeCcy());
        assertEquals(String.valueOf(hedge1.getTradedCurrencyAmount()), measurement.getTradeQty());
        assertEquals(hedge1.getVenue(), measurement.getVenue());
        assertEquals(String.valueOf(hedge1.getLastUpdated()), measurement.getTradeDate());
        assertEquals(TradeSideEnum.BUY.name(), measurement.getSide());
    }

    @Test
    public void processCepHedgeTradesWhenSiteGroupAreValid() {
        List<String> validSiteGroups = Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP");
        Mockito.when(properties.getSiteGroups()).thenReturn(validSiteGroups);
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        client.setTreasuryUser("prakash.konagi+treasury_ds@osl.com");
        client.setTreasuryUserSiteGroup("OSLSG_GROUP");

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        assertTrue(validSiteGroups.contains(hedge1.getSiteGroup()));
        assertTrue(validSiteGroups.contains(hedge2.getSiteGroup()));

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(2, result.size());
    }

    @Test
    public void processCepHedgeTradesWhenSiteGroupAreInvalid() {
        List<String> validSiteGroups = Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP");
        Mockito.when(properties.getSiteGroups()).thenReturn(validSiteGroups);
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        client.setTreasuryUser("prakash.konagi+treasury_ds@osl.com");
        client.setTreasuryUserSiteGroup("OSLSG_GROUP");

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        hedge1.setSiteGroup("OSL_GROUP_INVALID");
        hedge2.setSiteGroup("OSL_GROUP_INVALID");

        assertFalse(validSiteGroups.contains(hedge1.getSiteGroup()));
        assertFalse(validSiteGroups.contains(hedge2.getSiteGroup()));

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(0, result.size());
    }

    @Test
    public void processCepHedgeTradesWhenSiteGroupAreValidAndInvalid() {
        List<String> validSiteGroups = Arrays.asList("OSLLC_GROUP", "OSL_GROUP", "OSLSG_GROUP", "OSLAM_GROUP");
        Mockito.when(properties.getSiteGroups()).thenReturn(validSiteGroups);
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        client.setTreasuryUser("prakash.konagi+treasury_ds@osl.com");
        client.setTreasuryUserSiteGroup("OSLSG_GROUP");

        CepHedgeTradeResponse response = new CepHedgeTradeResponse();
        response.setRfqTradeList(Collections.singletonList(client));
        response.setRfqTradeFillList(Arrays.asList(hedge1, hedge2));
        response.setLatestId("1000");

        hedge1.setSiteGroup("OSL_GROUP_INVALID");

        assertFalse(validSiteGroups.contains(hedge1.getSiteGroup()));
        assertTrue(validSiteGroups.contains(hedge2.getSiteGroup()));

        List<CepHedgeTradeMeasurement> result = cepHedgeTradeService.process(response);

        assertEquals(1, result.size());
    }

    @Test
    public void getSiteGroupWhenTreasuryUserNotNullTest() {
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        client.setTreasuryUser("prakash.konagi+treasury_ds@osl.com");
        client.setTreasuryUserSiteGroup("OSLSG_GROUP");
        String siteGroup = cepHedgeTradeService.getSiteGroup(client);

        assertEquals(client.getTreasuryUserSiteGroup(), siteGroup);
    }

    @Test
    public void getSiteGroupWhenTreasuryUserIsNullTest() {
        Mockito.when(properties.getTreasuryUsers()).thenReturn(Arrays.asList("prakash.konagi+treasury_ds@osl.com", "oslds.rfqtreasury@osl.com"));
        String siteGroup = cepHedgeTradeService.getSiteGroup(client);

        assertEquals(client.getSiteGroup(), siteGroup);
    }
}
