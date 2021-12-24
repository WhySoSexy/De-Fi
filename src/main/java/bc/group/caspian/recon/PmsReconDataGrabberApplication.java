package bc.group.caspian.recon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@PropertySource("classpath:git.properties")
@SpringBootApplication
@EnableScheduling
public class PmsReconDataGrabberApplication {
    @PostConstruct
    public void init(){
        // Setting Spring Boot SetTimeZone
        //TODO: do not set timezone if we use instant type for query data from pms
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(PmsReconDataGrabberApplication.class, args);
    }

}
