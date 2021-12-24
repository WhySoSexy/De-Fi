package bc.group.caspian.recon.service;

import bc.group.caspian.recon.config.caspian.CaspianConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DataFeedService {

    private final static Logger logger = LoggerFactory.getLogger(DataFeedService.class);

    @Value("${spring.influx.database}")
    @Getter
    private String influxDatabase;

    @Autowired
    @Getter
    private InfluxDB influxDB;

    @Autowired
    private CaspianConfiguration caspianConfiguration;


    public <T,E> T getTransactionDto(E transactionModel , Class<T> clazz) {
        ObjectMapper objectMapper = caspianConfiguration.objectMapper();
        try {
            String transactionJson = objectMapper.writeValueAsString(transactionModel);
            return objectMapper.readValue(transactionJson, clazz);
        }catch (IOException e) {
            logger.error("Failed to convert data",e);
        }
        return null;
    }

    public <T> void publishToInflux(T payload) {
        Point point = Point.measurementByPOJO(payload.getClass())
                .addFieldsFromPOJO(payload)
                .build();
        BatchPoints batchPoints = BatchPoints
                .database(influxDatabase)
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .point(point)
                .build();
        influxDB.write(batchPoints);
    }

}
