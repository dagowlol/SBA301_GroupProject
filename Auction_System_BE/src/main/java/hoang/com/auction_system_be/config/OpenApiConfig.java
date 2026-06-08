package hoang.com.auction_system_be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auction System API")
                        .version("1.0")
                        .description("REST API documentation for Auction System Backend")
                        .contact(new Contact()
                                .name("SBA301 Group Project")
                                .email("support@auctionsystem.com")));
    }
}
