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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ConfigSection {
    private final String name;
    private final List<ConfigOption> options = new ArrayList<>();
    private final List<ConfigSubsection> subsections = new ArrayList<>();

    // Section's name is in a case-insensitive comparison.
    public boolean isName(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    /**
     * Returns an existing subsection with the given name or creates a new one.
     */
    public ConfigSubsection subsection(String name) {
        for (ConfigSubsection subsection : subsections.reversed()) {
            if (subsection.isName(name)) {
                return subsection;
            }
        }
        final ConfigSubsection subsection = new ConfigSubsection(name);
        subsections.add(subsection);
        return subsection;
    }

    public List<ConfigSubsection> subsections() {
        return subsections;
    }

    public Optional<ConfigOption> option(String key) {
        return options.stream().filter(opt -> opt.isKey(key)).findFirst();
    }

    public List<ConfigOption> optionAll(String key) {
        return options.stream().filter(opt -> opt.isKey(key)).collect(Collectors.toList());
    }

    public List<ConfigOption> options() {
        return options;
    }

    /**
     * Adds a new Option to the Section and returns the updated Section.
     */
    public ConfigSection addOption(String key, String value) {
        options.add(new ConfigOption(key, value));
        return this;
    }

    public boolean hasOption(String key) {
        for (ConfigOption option : options.reversed()) {
            if (option.isKey(key)) {
                return true;
            }
        }
        return false;
    }
}
