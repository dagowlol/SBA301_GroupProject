package hoang.com.auction_system_be.config.file.base;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileParserFactory {

    private final List<FileParser<?>> parsers;

    @SuppressWarnings("unchecked")
    public <T> FileParser<T> getParser(String filename, Class<T> targetClass) {
        return (FileParser<T>) parsers.stream()
                .filter(parser -> parser.supports(filename))
                .filter(parser -> parser.getTargetClass().equals(targetClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parser found for " + targetClass.getSimpleName()));
    }
}
