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
import io.netty.buffer.ByteBuf;

public interface HashFn {
    HashFn DEFAULT = new HashFnSha1();

    /**
     * @return the size of bytes representing the hash code.
     */
    int size();

    /**
     * Read the hash code from bytes[0, size()).
     */
    default HashCode read(ByteBuf bytes) {
        final byte[] hashCode = new byte[size()];
        bytes.readBytes(hashCode);
        return HashCode.fromBytes(hashCode);
    }

    /**
     * Calculate the hash code for bytes[start, start + len).
     */
    HashCode calculate(byte[] bytes, int start, int len);
}
