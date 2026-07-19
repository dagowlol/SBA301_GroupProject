package hoang.com.auction_system_be.config.file.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileParser<T> {

    List<ParseRow<T>> parse(InputStream inputStream) throws IOException;

    String[] getSupportedExtensions();

    default boolean supports(String filename) {
        if (filename == null)
            return false;
        String extension = getFileExtension(filename).toLowerCase();
        for (String ext : getSupportedExtensions()) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    default String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    Object getTargetClass();
}
