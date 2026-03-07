package org.jujutsu.parser;

import org.jujutsu.model.Mission;
import java.io.File;

public interface MissionParser {
    Mission parse(File file) throws Exception;
    boolean supports(File file);
}
