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

package com.tisonkun.git.core.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SystemProperties;

@UtilityClass
public class PathUtils {
    public static String replaceTildeWithHome(String path) {
        if (path.startsWith("~")) {
            final int firstSlash = path.indexOf('/');
            if (firstSlash == 1) {
                final String home = SystemProperties.getUserHome();
                return path.replaceFirst("~", home);
            } else if (firstSlash > 1) {
                throw new UnsupportedOperationException("tilde username as arbitrary user home is not yet implemented");
            }
        }
        return path;
    }
}
