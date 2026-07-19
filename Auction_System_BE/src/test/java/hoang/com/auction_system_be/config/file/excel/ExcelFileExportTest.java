package hoang.com.auction_system_be.config.file.excel;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelFileExportTest {

    @Test
    void export_shouldWriteHeadersAndTypedValues() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExportRow data = new ExportRow(
                "INV-1",
                new BigDecimal("125000.50"),
                LocalDateTime.of(2026, 7, 19, 10, 30));
        List<Function<ExportRow, Object>> columns = List.of(
                ExportRow::id,
                ExportRow::amount,
                ExportRow::createdAt);

        try (ExcelFileExport<ExportRow> exporter = new ExcelFileExport<>(
                "Transactions",
                new String[] { "ID", "Amount", "Created At" },
                columns)) {
            exporter.appendRows(List.of(data));
            exporter.writeTo(outputStream);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            var sheet = workbook.getSheet("Transactions");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("INV-1");
            assertThat(sheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(125000.50);
            assertThat(sheet.getRow(1).getCell(2).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2026, 7, 19, 10, 30));
        }
    }

    @Test
    void export_shouldCreateAnotherSheetWhenExcelRowLimitIsReached() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<Function<String, Object>> columns = List.of(value -> value);

        try (ExcelFileExport<String> exporter = new ExcelFileExport<>(
                "Transactions",
                new String[] { "ID" },
                columns,
                2)) {
            exporter.appendRows(List.of("INV-1", "INV-2", "INV-3"));
            exporter.writeTo(outputStream);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheet("Transactions").getRow(2).getCell(0).getStringCellValue())
                    .isEqualTo("INV-2");
            assertThat(workbook.getSheet("Transactions 2").getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("INV-3");
        }
    }

    private record ExportRow(String id, BigDecimal amount, LocalDateTime createdAt) {
    }
}
