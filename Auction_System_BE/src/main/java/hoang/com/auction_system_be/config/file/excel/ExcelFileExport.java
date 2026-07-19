package hoang.com.auction_system_be.config.file.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Memory-bounded XLSX writer. Rows outside the configured window are flushed
 * to temporary files instead of being retained in the JVM heap.
 */
public final class ExcelFileExport<T> implements AutoCloseable {

    private static final int ROW_ACCESS_WINDOW_SIZE = 100;
    private static final int MIN_COLUMN_WIDTH = 12;
    private static final int MAX_COLUMN_WIDTH = 40;

    private final SXSSFWorkbook workbook;
    private final String baseSheetName;
    private final String[] headers;
    private final List<Function<T, Object>> valueExtractors;
    private final CellStyle dataStyle;
    private final CellStyle dateTimeStyle;
    private final int maxDataRowsPerSheet;
    private Sheet sheet;
    private int sheetNumber;
    private int nextRowIndex = 1;

    public ExcelFileExport(String sheetName, String[] headers, List<Function<T, Object>> valueExtractors) {
        this(sheetName, headers, valueExtractors, SpreadsheetVersion.EXCEL2007.getLastRowIndex());
    }

    ExcelFileExport(
            String sheetName,
            String[] headers,
            List<Function<T, Object>> valueExtractors,
            int maxDataRowsPerSheet) {
        if (headers.length != valueExtractors.size()) {
            throw new IllegalArgumentException("Headers and value extractors must have the same size");
        }
        if (maxDataRowsPerSheet < 1 || maxDataRowsPerSheet > SpreadsheetVersion.EXCEL2007.getLastRowIndex()) {
            throw new IllegalArgumentException("Invalid maximum data rows per sheet");
        }

        this.workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        this.workbook.setCompressTempFiles(true);
        this.baseSheetName = WorkbookUtil.createSafeSheetName(sheetName);
        this.headers = headers.clone();
        this.valueExtractors = List.copyOf(valueExtractors);
        this.maxDataRowsPerSheet = maxDataRowsPerSheet;
        this.dataStyle = createDataStyle();
        this.dateTimeStyle = createDateTimeStyle(dataStyle);

        createNextSheet();
    }

    public void appendRows(List<T> rows) {
        for (T item : rows) {
            if (nextRowIndex > maxDataRowsPerSheet) {
                createNextSheet();
            }
            Row row = sheet.createRow(nextRowIndex++);
            for (int column = 0; column < valueExtractors.size(); column++) {
                writeCell(row, column, valueExtractors.get(column).apply(item));
            }
        }
    }

    private void createNextSheet() {
        sheetNumber++;
        String suffix = sheetNumber == 1 ? "" : " " + sheetNumber;
        int baseLength = Math.min(baseSheetName.length(), 31 - suffix.length());
        sheet = workbook.createSheet(baseSheetName.substring(0, baseLength) + suffix);
        nextRowIndex = 1;
        writeHeader(headers);
        setColumnWidths(headers);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        outputStream.flush();
    }

    private void writeHeader(String[] headers) {
        Row row = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        applyBorders(headerStyle);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int column = 0; column < headers.length; column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(headers[column]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        applyBorders(style);
        return style;
    }

    private CellStyle createDateTimeStyle(CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    private void applyBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void setColumnWidths(String[] headers) {
        for (int column = 0; column < headers.length; column++) {
            int width = Math.max(MIN_COLUMN_WIDTH, headers[column].length() + 4);
            sheet.setColumnWidth(column, Math.min(width, MAX_COLUMN_WIDTH) * 256);
        }
    }

    private void writeCell(Row row, int column, Object value) {
        Cell cell = row.createCell(column);
        CellStyle style = dataStyle;

        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean booleanValue) {
            cell.setCellValue(booleanValue);
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(toDate(dateTime));
            style = dateTimeStyle;
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(toDate(date.atStartOfDay()));
            style = dateTimeStyle;
        } else {
            cell.setCellValue(sanitizeText(value.toString()));
        }
        cell.setCellStyle(style);
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String sanitizeText(String value) {
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        try {
            workbook.close();
        } finally {
            workbook.dispose();
        }
    }
}
