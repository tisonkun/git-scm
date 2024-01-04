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

/**
 * Current default implementation of {@link HashFn}. See also
 * <a href="https://git-scm.com/docs/hash-function-transition/">
 * "Migrate Git from SHA-1 to a stronger hash function"</a> for a future plan.
 */
public class HashFnSha1 implements HashFn {
    @Override
    public int size() {
        return 20;
    }

    @SuppressWarnings("deprecation")
    @Override
    public HashCode calculate(byte[] bytes, int start, int len) {
        return Hashing.sha1().hashBytes(bytes, start, len);
    }
}
