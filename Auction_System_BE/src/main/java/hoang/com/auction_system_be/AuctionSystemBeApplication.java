package hoang.com.auction_system_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class AuctionSystemBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuctionSystemBeApplication.class, args);
    }
}
