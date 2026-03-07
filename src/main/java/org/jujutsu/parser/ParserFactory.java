package org.jujutsu.parser;

import java.io.File;
import java.util.List;

public class ParserFactory {

    private static final List<MissionParser> PARSERS = List.of(
            new TxtParser(),
            new JsonParser(),
            new XmlParser()
    );

    public static MissionParser getParser(File file) {
        return PARSERS.stream()
                .filter(p -> p.supports(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неподдерживаемый формат файла: " + file.getName()));
    }
}
