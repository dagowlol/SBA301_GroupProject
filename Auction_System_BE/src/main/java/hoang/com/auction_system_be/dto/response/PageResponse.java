package hoang.com.auction_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    List<T> content;
    int pageNo;
    int pageSize;
    long totalElements;
    int totalPages;
    boolean last;
}
