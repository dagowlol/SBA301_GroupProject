package hoang.com.auction_system_be.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    String name;

    @NotBlank(message = "Description is required")
    String description;

    @NotNull(message = "Category ID is required")
    Long categoryId;

    @NotNull(message = "Starting price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Starting price must be greater than 0")
    BigDecimal startingPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Reserve price must be greater than 0")
    BigDecimal reservePrice;
}
