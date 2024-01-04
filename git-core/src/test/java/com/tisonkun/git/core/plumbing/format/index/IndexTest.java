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

import static org.assertj.core.api.Assumptions.assumeThat;
import com.tisonkun.git.core.test.TestUtils;
import java.io.File;
import org.junit.jupiter.api.Test;

class IndexTest {
    @Test
    public void testParseIndexFile() throws Exception {
        final File file = new File(TestUtils.baseDir(), ".git/index");
        assumeThat(file.exists()).describedAs("runs only with .git folder").isTrue();

        final Index index = Index.create(file);
        assumeThat(index.getEntries()).anyMatch(ent -> ent.getPathname().equals(".editorconfig"));
    }
}
