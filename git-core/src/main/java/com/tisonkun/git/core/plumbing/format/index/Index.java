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

package com.tisonkun.git.core.plumbing.format.index;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.io.Files;
import com.tisonkun.git.core.plumbing.hash.HashFn;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Index {
    private final int version;
    private final List<IndexEntry> entries;
    private final List<IndexExtension> extensions;

    public static Index create(File source) throws IOException {
        final byte[] content = Files.asByteSource(source).read();
        Preconditions.checkState(content.length >= 12, "malformed index header (length: %s)", content.length);

        final ByteBuf bytes = Unpooled.wrappedBuffer(content);
        final CharSequence signature = bytes.readCharSequence(4, StandardCharsets.US_ASCII);
        Preconditions.checkState("DIRC".contentEquals(signature), "malformed index header signature", signature);

        // read version
        final int version = bytes.readInt();

        // read entries
        final int entryLen = bytes.readInt();
        final List<IndexEntry> entries = new ArrayList<>();
        for (int i = 0; i < entryLen; i++) {
            entries.add(IndexEntry.create(bytes, version));
        }

        // read extensions
        final List<IndexExtension> extensions = new ArrayList<>();
        while (bytes.readableBytes() > HashFn.DEFAULT.size()) {
            extensions.add(IndexExtension.create(bytes));
        }

        // compare checksum
        final int offset = bytes.readerIndex();
        final HashCode actualChecksum = HashFn.DEFAULT.calculate(content, 0, offset);
        final HashCode expectedChecksum = HashFn.DEFAULT.read(bytes);
        Preconditions.checkState(
                expectedChecksum.equals(actualChecksum),
                "checksum mismatch (expected = %s, actual = %s)",
                expectedChecksum,
                actualChecksum);

        return new Index(version, entries, extensions);
    }
}
