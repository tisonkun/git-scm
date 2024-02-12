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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tisonkun.git.core.test.TestUtils;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigTest {
    @Test
    public void testSample() throws Exception {
        // git configuration example from: https://git-scm.com/docs/git-config#_example
        final File file = new File(TestUtils.testResourceDir(), "gitconfig/sample-config.ini");

        final Config config = Config.create(file);
        assertThat(config.hasSection("core")).isTrue();

        final ConfigSection core = config.section("core");
        assertThat(core.options()).hasSize(3);
        assertThat(core.option("filemode")).map(ConfigOption::value).hasValue("false");
        assertThat(core.optionAll("gitproxy"))
                .map(ConfigOption::value)
                .containsExactlyInAnyOrder("ssh for kernel.org", "default-proxy");

        assertThat(config.hasSection("diff")).isTrue();
        final ConfigSection diff = config.section("diff");
        assertThat(diff.options()).hasSize(2);
        assertThat(diff.option("external")).map(ConfigOption::value).hasValue("/usr/local/bin/diff-wrapper");
        assertThat(diff.option("renames")).map(ConfigOption::value).hasValue("true");

        assertThat(config.hasSection("branch")).isTrue();
        final ConfigSection branch = config.section("branch");
        assertThat(branch.subsections()).hasSize(1);
        final ConfigSubsection devel = branch.subsection("devel");
        assertThat(devel.options()).hasSize(2);
        assertThat(devel.option("remote")).map(ConfigOption::value).hasValue("origin");
        assertThat(devel.option("merge")).map(ConfigOption::value).hasValue("refs/heads/devel");

        assertThat(config.hasSection("remote")).isTrue();
        final ConfigSection remote = config.section("remote");
        assertThat(remote.subsections()).hasSize(1);
        final ConfigSubsection origin = remote.subsection("origin");
        assertThat(origin.options()).hasSize(1);
        assertThat(origin.option("url")).map(ConfigOption::value).hasValue("https://example.com/git");

        final List<ConfigInclude> includes = config.includes();
        assertThat(includes).hasSize(9);
        assertThat(includes)
                .containsExactlyInAnyOrder(
                        new ConfigInclude(null, "/path/to/foo.inc"),
                        new ConfigInclude(null, "foo.inc"),
                        new ConfigInclude(null, "~/foo.inc"),
                        new ConfigInclude("gitdir:/path/to/foo/.git", "/path/to/foo.inc"),
                        new ConfigInclude("gitdir:~/to/group/", "/path/to/foo.inc"),
                        new ConfigInclude("gitdir:/path/to/group/", "/path/to/foo.inc"),
                        new ConfigInclude("gitdir:/path/to/group/", "foo.inc"),
                        new ConfigInclude("onbranch:foo-branch", "foo.inc"),
                        new ConfigInclude("hasconfig:remote.*.url:https://example.com/**", "foo.inc"));
    }

    @Test
    public void testCornerCase() throws Exception {
        final File file = new File(TestUtils.testResourceDir(), "gitconfig/corner-config.ini");
        final Config config = Config.create(file);
        assertThat(config.sections()).hasSize(3);

        assertThat(config.hasSection("core")).isTrue();
        final ConfigSection core = config.section("core");
        assertThat(core.options()).hasSize(3);
        assertThat(core.option("filemode")).map(ConfigOption::value).hasValue("false");
        assertThat(core.option("buttonoption")).map(ConfigOption::value).hasValue("");
        assertThat(core.option("buttonoptionagain")).map(ConfigOption::value).hasValue("");

        assertThat(config.hasSection("bar.baz")).isTrue();
        final ConfigSection barBaz = config.section("bar.baz");
        assertThat(barBaz.options()).hasSize(2);
        assertThat(barBaz.option("foo")).map(ConfigOption::value).hasValue("bar");
        assertThat(barBaz.option("url")).map(ConfigOption::value).hasValue("https://example.com/git");

        assertThat(config.hasSection("url")).isTrue();
        final ConfigSubsection subsection = config.section("url").subsection("git@example.com:");
        assertThat(subsection.options()).hasSize(1);
        final ConfigOption option = subsection.options().getFirst();
        assertThat(option.key()).isEqualTo("insteadof");
        assertThat(option.value()).isEqualTo("https://example.com/");
    }

    @Test
    public void testMalformed() {
        final String[] messages =
                new String[] {"malformed section name", "malformed variable names", "malformed section name"};

        for (int i = 0; i < messages.length; i++) {
            final String filename = "gitconfig/malformed-config-%s.ini".formatted(i + 1);
            final File file = new File(TestUtils.testResourceDir(), filename);
            assertThatThrownBy(() -> Config.create(file))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(messages[i]);
        }
    }
}
