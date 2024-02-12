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
public class ConfigSubsection {
    private final String name;
    private final List<ConfigOption> options = new ArrayList<>();

    // Section's name is in a case-sensitive comparison.
    public boolean isName(String name) {
        return this.name.equals(name);
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
    public ConfigSubsection addOption(String key, String value) {
        this.options.add(new ConfigOption(key, value));
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
