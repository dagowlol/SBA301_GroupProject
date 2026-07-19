package hoang.com.auction_system_be.config.file.pdf;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PdfFileExportTest {

    @Test
    void export_shouldCreatePdfWithUtf8TransactionData() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExportRow row = new ExportRow(
                "INV-1",
                "Bình gốm Huế",
                new BigDecimal("125000.50"),
                LocalDateTime.of(2026, 7, 19, 10, 30));
        List<Function<ExportRow, Object>> columns = List.of(
                ExportRow::id,
                ExportRow::product,
                ExportRow::amount,
                ExportRow::createdAt);

        try (PdfFileExport<ExportRow> exporter = new PdfFileExport<>(
                outputStream,
                "Earning Transactions",
                new String[] { "ID", "Product", "Amount", "Created At" },
                columns)) {
            exporter.appendRows(List.of(row));
        }

        byte[] pdf = outputStream.toByteArray();
        assertThat(pdf).startsWith("%PDF-".getBytes());
        assertThat(pdf.length).isGreaterThan(1_000);
    }

    private record ExportRow(
            String id,
            String product,
            BigDecimal amount,
            LocalDateTime createdAt) {
    }
}
