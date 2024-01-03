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
import com.tisonkun.git.core.plumbing.hash.HashUtils;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(builderClassName = "Builder", builderMethodName = "")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexEntry {
    private final int ctimeSeconds;
    private final int ctimeNanoseconds;
    private final int mtimeSeconds;
    private final int mtimeNanoseconds;
    private final int dev;
    private final int ino;
    private final int mode;
    private final int uid;
    private final int gid;
    private final int fileSize;
    private final HashCode sha1;
    private final String pathname;
    private final IndexEntryStage stage;
    private final boolean skipWorktree;
    private final boolean intentToAdd;

    public static IndexEntry create(ByteBuf bytes, int version) {
        final Builder builder = new Builder();
        builder.ctimeSeconds(bytes.readInt());
        builder.ctimeNanoseconds(bytes.readInt());
        builder.mtimeSeconds(bytes.readInt());
        builder.mtimeNanoseconds(bytes.readInt());
        builder.dev(bytes.readInt());
        builder.ino(bytes.readInt());
        builder.mode(bytes.readInt());
        builder.uid(bytes.readInt());
        builder.gid(bytes.readInt());
        builder.fileSize(bytes.readInt());
        builder.sha1(HashUtils.readSha1(bytes));

        final short flag = bytes.readShort();
        final int nameLen = flag & 0xFFF;
        builder.stage(IndexEntryStage.of((flag >> 12) & 0x3));

        int entryLen = 62;
        if ((flag & 0x4000) != 0) { // extended
            Preconditions.checkArgument(version >= 3, "version (%s) < 3 cannot have extended flags", version);
            final int extraFlags = bytes.readShort();
            builder.skipWorktree((extraFlags & 0x4000) != 0);
            builder.intentToAdd((extraFlags & 0x2000) != 0);
            entryLen += 2;
        }

        final int fixedNameLen = nameLen < 0xFFF ? nameLen : bytes.bytesBefore((byte) 0);
        final CharSequence pathname = bytes.readCharSequence(nameLen, StandardCharsets.UTF_8);
        builder.pathname(pathname.toString());
        entryLen += fixedNameLen;

        // In version 4, the padding after the pathname does not exist.
        if (version < 4) {
            final int padLen = 8 - Math.floorMod(entryLen, 8);
            bytes.readBytes(padLen);
        }

        return builder.build();
    }

    private static class Builder {}
}
