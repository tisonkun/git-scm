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

package com.tisonkun.git.core.plumbing.hash;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HashUtils {
    public static HashCode readSha1(ByteBuf bytes) {
        final byte[] sha1 = new byte[HashConstants.SIZE];
        bytes.readBytes(sha1);
        return HashCode.fromBytes(sha1);
    }

    @SuppressWarnings("deprecation") // Git does use SHA-1
    public static HashCode calculateSha1(byte[] content, int start, int len) {
        return Hashing.sha1().hashBytes(content, start, len);
    }
}
