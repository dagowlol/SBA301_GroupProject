package hoang.com.auction_system_be.config.file.base;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;
import java.util.Set;

public interface RowMapper<T> {

    ParseRow<T> map(Row row, DataFormatter formatter, Map<String, Integer> headerMap);

    Set<String> getRequiredHeaders();

    Class<T> getTargetClass();
}
