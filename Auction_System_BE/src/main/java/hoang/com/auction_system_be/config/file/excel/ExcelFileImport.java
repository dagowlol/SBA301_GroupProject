package hoang.com.auction_system_be.config.file.excel;

import lombok.RequiredArgsConstructor;
import hoang.com.auction_system_be.config.file.base.FileParser;
import hoang.com.auction_system_be.config.file.base.ParseRow;
import hoang.com.auction_system_be.config.file.base.RowMapper;

import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExcelFileImport<T> implements FileParser<T> {

    private final RowMapper<T> rowMapper;

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { "xlsx", "xls" };
    }

    @Override
    public List<ParseRow<T>> parse(InputStream inputStream) throws IOException {
        List<ParseRow<T>> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getLastRowNum() < 0) {
                throw new IllegalArgumentException("File contains no data");
            }

            DataFormatter dataFormatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || isRowEmpty(headerRow)) {
                throw new IllegalArgumentException("Missing header row");
            }

            Map<String, Integer> headerMap = buildHeaderMap(headerRow, dataFormatter);
            validateHeaders(headerMap);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    continue;
                }

                ParseRow<T> wrapper = rowMapper.map(row, dataFormatter, headerMap);
                wrapper.setRowIndex(i + 1);
                results.add(wrapper);
            }
        }
        return results;
    }

    @Override
    public Object getTargetClass() {
        return rowMapper.getTargetClass();
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter dataFormatter) {
        Map<String, Integer> headerMap = new HashMap<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String headerName = dataFormatter.formatCellValue(cell).trim().toLowerCase();
                headerMap.put(headerName, cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        Set<String> requiredHeaders = rowMapper.getRequiredHeaders();
        Set<String> actualHeaders = headerMap.keySet();

        List<String> missingHeaders = requiredHeaders.stream()
                .filter(required -> !actualHeaders.contains(required))
                .collect(Collectors.toList());

        if (!missingHeaders.isEmpty()) {
            String missingFields = String.join(", ", missingHeaders);

            throw new IllegalArgumentException("Missing required headers: " + missingFields);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null || row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
