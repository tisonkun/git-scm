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

import static org.assertj.core.api.Assertions.assertThat;
import com.tisonkun.git.core.test.TestUtils;
import java.io.File;
import org.junit.jupiter.api.Test;

class ConfigTest {
    @Test
    public void testParseConfigFile() throws Exception {
        {
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/testParseConfigFile.1");
            final Config config = Config.create(file);
            assertThat(config.hasSection("core")).isTrue();
            assertThat(config.section("core").hasOption("gitProxy")).isTrue();
            assertThat(config.section("branch").subsection("devel").hasOption("remote"))
                    .isTrue();
        }
        {
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/testParseConfigFile.2");
            final Config config = Config.create(file);
            assertThat(config.hasSection("core")).isTrue();
        }
    }
}
