package hoang.com.auction_system_be.config.file.pdf;

import org.librepdf.openpdf.fonts.Liberation;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/** Writes each supplied batch immediately so transaction DTOs are not retained. */
public final class PdfFileExport<T> implements AutoCloseable {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Document document;
    private final String[] headers;
    private final List<Function<T, Object>> valueExtractors;
    private final Font headerFont;
    private final Font dataFont;

    public PdfFileExport(
            OutputStream outputStream,
            String title,
            String[] headers,
            List<Function<T, Object>> valueExtractors) throws IOException {
        if (headers.length != valueExtractors.size()) {
            throw new IllegalArgumentException("Headers and value extractors must have the same size");
        }

        this.headers = headers.clone();
        this.valueExtractors = List.copyOf(valueExtractors);
        this.headerFont = Liberation.SANS_BOLD.create(8);
        this.dataFont = Liberation.SANS.create(8);
        this.document = new Document(PageSize.A4.rotate(), 24, 24, 28, 28);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.addTitle(title);
            document.open();
            Font titleFont = Liberation.SANS_BOLD.create(15);
            Paragraph heading = new Paragraph(title, titleFont);
            heading.setSpacingAfter(12);
            document.add(heading);
        } catch (DocumentException exception) {
            throw new IOException("Unable to initialize PDF export", exception);
        }
    }

    public void appendRows(List<T> rows) throws IOException {
        if (rows.isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitRows(true);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(0, 61, 91));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (T item : rows) {
            for (Function<T, Object> extractor : valueExtractors) {
                PdfPCell cell = new PdfPCell(new Phrase(formatValue(extractor.apply(item)), dataFont));
                cell.setPadding(4);
                table.addCell(cell);
            }
        }

        try {
            document.add(table);
        } catch (DocumentException exception) {
            throw new IOException("Unable to write PDF transaction batch", exception);
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        return value.toString();
    }

    @Override
    public void close() {
        if (document.isOpen()) {
            document.close();
        }
    }
}
