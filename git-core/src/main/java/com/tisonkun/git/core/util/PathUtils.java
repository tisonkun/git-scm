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

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
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

    public static void main(String[] args) throws Throwable {
        final Linker linker = Linker.nativeLinker();
        final SymbolLookup libc = linker.defaultLookup();
        final MethodHandle handle = linker.downcallHandle(
                libc.find("getpwnam").orElseThrow(), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        try (Arena arena = Arena.ofConfined()) {
            final MemorySegment passwd = (MemorySegment) handle.invoke(arena.allocateUtf8String("root"));
            System.out.println("passwd=" + passwd);
            System.out.println("pw_name="
                    + passwd.reinterpret(Long.MAX_VALUE)
                            .get(ValueLayout.ADDRESS, 48)
                            .reinterpret(Long.MAX_VALUE)
                            .getUtf8String(0));
        }
    }
}
