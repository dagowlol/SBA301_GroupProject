package hoang.com.auction_system_be.config.file.base;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseRow<T> {
    private T data;
    private List<ParseError> errors = new ArrayList<>();
    private int rowIndex;

    public boolean hasParseErrors() {
        return !errors.isEmpty();
    }

    public record ParseError(String field, Object rejectedValue, String message) {
    }
}
