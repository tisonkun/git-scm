/*
 * Copyright 2024 tison <wander4096@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tisonkun.git.core.plumbing.format.config;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("OptionalAssignedToNull") // includeCondition
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {
    private static final Pattern SECTION_NAME_PAT = Pattern.compile("^([a-zA-Z0-9.\\-]+)(?:\\s+\"(.*)\")?$");

    private final List<ConfigSection> sections;
    private final List<ConfigInclude> includes;

    // @see https://git-scm.com/docs/git-config#_configuration_file
    public static Config create(File source) throws IOException {
        final Iterator<String> lines =
                Files.readLines(source, StandardCharsets.UTF_8).iterator();
        final Config config = new Config(new ArrayList<>(), new ArrayList<>());

        String currentSection = null;
        String currentSubsection = null;
        Optional<String> includeCondition = null;

        while (lines.hasNext()) {
            final String line = lines.next();
            final String strippedLine = line.strip();

            if (strippedLine.isEmpty()) {
                continue;
            }
            if (strippedLine.startsWith(";")) {
                continue;
            }
            if (strippedLine.startsWith("#")) {
                continue;
            }

            if (strippedLine.startsWith("[")) {
                final String moreStrippedLine =
                        strippedLine.replaceFirst("[;#].*$", "").strip();
                Preconditions.checkState(moreStrippedLine.endsWith("]"), "malformed section syntax: %s", line);
                final StringBuilder sectionName = new StringBuilder();
                final StringBuilder subsectionName = new StringBuilder();
                if (parseSectionName(moreStrippedLine, sectionName, subsectionName) != 0) {
                    currentSection = sectionName.toString();
                    currentSubsection = subsectionName.toString();
                    if (currentSection.equalsIgnoreCase("include")) {
                        throw new IllegalStateException("malformed include section with cond: " + line);
                    } else if (currentSection.equalsIgnoreCase("includeIf")) {
                        includeCondition = Optional.of(currentSubsection);
                    } else {
                        includeCondition = null;
                        config.section(currentSection).subsection(currentSubsection);
                    }
                } else {
                    currentSection = sectionName.toString();
                    currentSubsection = null;
                    if (currentSection.equalsIgnoreCase("include")) {
                        includeCondition = Optional.empty();
                    } else if (currentSection.equalsIgnoreCase("includeIf")) {
                        throw new IllegalStateException("malformed includeIf section without cond: " + line);
                    } else {
                        includeCondition = null;
                        config.section(currentSection);
                    }
                }
                continue;
            }

            Preconditions.checkState(currentSection != null, "each variable must belong to some section", line);
            final StringBuilder keyBuilder = new StringBuilder();
            final StringBuilder valueBuilder = new StringBuilder();
            int parseOption = parseOptionKeyValue(strippedLine, keyBuilder, valueBuilder);
            final String key = keyBuilder.toString().toLowerCase();
            if (includeCondition != null) {
                Preconditions.checkState(key.equals("path"), "include section can contain only 'path'", line);
            }
            while (parseOption != 0 && lines.hasNext()) {
                final boolean quoted = parseOption != 1;
                final String nextLine = lines.next();
                parseOption = parseOptionValue(nextLine.stripTrailing(), valueBuilder, quoted);
            }
            final String value = valueBuilder.toString();
            if (includeCondition != null) {
                config.includes.add(new ConfigInclude(includeCondition.orElse(null), value));
            } else if (currentSubsection != null) {
                config.section(currentSection).subsection(currentSubsection).addOption(key, value);
            } else {
                config.section(currentSection).addOption(key, value);
            }
        }

        return config;
    }

    // Returns:
    //  * 0 if no subsection
    //  * 1 if subsection provided
    private static int parseSectionName(String line, StringBuilder sectionName, StringBuilder subsectionName) {
        final Matcher sectionMatcher = SECTION_NAME_PAT.matcher(line.substring(1, line.length() - 1));
        Preconditions.checkState(sectionMatcher.find(), "malformed section name: %s", line);
        sectionName.append(sectionMatcher.group(1).toLowerCase());

        final String subsection = sectionMatcher.group(2);
        if (subsection == null) {
            return 0;
        }

        int idx = 0;
        final int len = subsection.length();
        while (idx < len) {
            final char ch = subsection.charAt(idx);
            if (ch != '\\') {
                subsectionName.append(ch);
            } else {
                idx += 1;
                Preconditions.checkState(idx < len, "malformed section name: %s", line);
                subsectionName.append(subsection.charAt(idx));
            }
            idx += 1;
        }
        return 1;
    }

    // Returns:
    //  * 0 if no continuing value
    //  * 1 if unquoted continuing value
    //  * 2 if quoted continuing value
    private static int parseOptionKeyValue(String line, StringBuilder key, StringBuilder value) {
        // parse first character
        char ch = line.charAt(0);
        Preconditions.checkState(
                (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'), "malformed variable names: %s", line);
        key.append(ch);

        // parse remaining key
        int idx = 1;
        final int len = line.length();
        while (idx < len) {
            ch = line.charAt(idx);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-')) {
                key.append(ch);
                idx += 1;
            } else {
                break;
            }
        }
        if (idx >= len) {
            return 0;
        }

        // parse whitespace and '='
        while (idx < len && Character.isWhitespace(line.charAt(idx))) {
            idx += 1;
        }
        if (idx < len && line.charAt(idx) != '=') {
            switch (line.charAt(idx)) {
                case '=':
                    break;
                case ';', '#':
                    return 0;
                default:
                    throw new IllegalArgumentException("malformed variable: " + line);
            }
        }
        idx += 1;
        while (idx < len && Character.isWhitespace(line.charAt(idx))) {
            idx += 1;
        }
        if (idx >= len) {
            return 0;
        }

        return parseOptionValue(line.substring(idx), value, false);
    }

    // Returns:
    //  * 0 if no continuing value
    //  * 1 if unquoted continuing value
    //  * 2 if quoted continuing value
    private static int parseOptionValue(String line, StringBuilder value, boolean quoted) {
        if (line.isEmpty()) {
            Preconditions.checkState(!quoted, "malformed variable value: unclosed quote");
            return 0;
        }

        int idx = 0;
        final int len = line.length();
        while (idx < len) {
            final char ch = line.charAt(idx++);

            if (!quoted) {
                switch (ch) {
                        // HACK - how Git works
                    case '\t':
                        value.append(' ');
                        continue;
                    case ';', '#':
                        int trimIdx = value.length() - 1;
                        while (trimIdx >= 0 && Character.isWhitespace(value.charAt(trimIdx))) {
                            trimIdx -= 1;
                        }
                        value.setLength(trimIdx + 1);
                        ;
                        return 0;
                }
            }

            // normal case - not '\'
            if (ch != '\\') {
                if (ch != '"') {
                    value.append(ch);
                } else {
                    quoted = !quoted;
                }
                continue;
            }

            // ch = '\' to escape
            if (idx < len) {
                final char nextCh = line.charAt(idx++);
                switch (nextCh) {
                    case '\\' -> value.append('\\');
                    case '"' -> value.append('"');
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'b' -> value.append('\b');
                    default -> throw new IllegalStateException("cannot escape " + nextCh + " at: " + line);
                }
                continue;
            }

            // ch = '\' to continue
            return quoted ? 2 : 1;
        }

        Preconditions.checkState(!quoted, "malformed variable value: unclosed quote");
        return 0;
    }

    /**
     * Returns an existing section with the given name or creates a new one.
     */
    public ConfigSection section(String name) {
        for (ConfigSection section : sections.reversed()) {
            if (section.isName(name)) {
                return section;
            }
        }
        final ConfigSection section = new ConfigSection(name);
        sections.add(section);
        return section;
    }

    public boolean hasSection(String name) {
        for (ConfigSection section : sections.reversed()) {
            if (section.isName(name)) {
                return true;
            }
        }
        return false;
    }
}
