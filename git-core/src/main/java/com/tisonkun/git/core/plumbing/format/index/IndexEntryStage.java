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

/**
 * Index entry stage during merge.
 */
public enum IndexEntryStage {
    /**
     * Merged is the default stage; fully merged.
     */
    Merged,
    /**
     * AncestorMode is the base revision.
     */
    AncestorMode,
    /**
     * OurMode is the first tree revision, ours.
     */
    OurMode,
    /**
     * TheirMode is the second tree revision, theirs.
     */
    TheirMode;

    public static IndexEntryStage of(int bits) {
        return switch (bits) {
            case 0 -> Merged;
            case 1 -> AncestorMode;
            case 2 -> OurMode;
            case 3 -> TheirMode;
            default -> throw new IllegalArgumentException("malformed stage bits: " + bits);
        };
    }
}
