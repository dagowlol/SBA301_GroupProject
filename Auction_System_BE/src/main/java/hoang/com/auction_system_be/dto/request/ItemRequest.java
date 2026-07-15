package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.enums.ItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    String itemName;

    @NotBlank(message = "Description is required")
    String description;

    @NotNull(message = "Category ID is required")
    Long categoryId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Starting price must be greater than 0")
    BigDecimal startingPrice;

    @NotNull(message = "Reserve price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Reserve price must be greater than 0")
    BigDecimal reservePrice;

    @NotNull(message = "Minimum increment is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum increment must be greater than 0")
    BigDecimal minIncrement;

    String condition;

    ItemStatus status;

    List<MultipartFile> images;
}
