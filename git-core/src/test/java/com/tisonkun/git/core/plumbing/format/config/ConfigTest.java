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
    public void testParseConfigFile() throws Exception {
        {
            // git configuration example from: https://git-scm.com/docs/git-config#_example
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/testParseConfigFile.1");
            final Config config = Config.create(file);
            assertThat(config.hasSection("core")).isTrue();
            final ConfigSection core = config.section("core");
            assertThat(core.getOptions().size()).isEqualTo(3);
            assertThat(core.hasOption("filemode")).isTrue();
            assertThat(core.hasOption("gitProxy")).isTrue();
            for (ConfigOption option : core.getOptions()) {
                switch (option.getKey()) {
                    case "filemode" -> assertThat(option.getValue()).isEqualTo("false");
                        // option's key is set to lower case manually
                    case "gitproxy" -> assertThat(option.getValue()).isIn("ssh for kernel.org", "default-proxy");
                    default -> throw new AssertionError("unexpected option: " + option.getKey());
                }
            }
            assertThat(config.hasSection("diff")).isTrue();
            final ConfigSection diff = config.section("diff");
            assertThat(diff.getOptions().size()).isEqualTo(2);
            assertThat(diff.hasOption("external")).isTrue();
            assertThat(diff.hasOption("renames")).isTrue();
            for (ConfigOption option : diff.getOptions()) {
                switch (option.getKey()) {
                    case "external" -> assertThat(option.getValue()).isEqualTo("/usr/local/bin/diff-wrapper");
                    case "renames" -> assertThat(option.getValue()).isEqualTo("true");
                    default -> throw new AssertionError("unexpected option: " + option.getKey());
                }
            }

            assertThat(config.hasSection("branch")).isTrue();
            assertThat(config.section("branch").getSubsections().size()).isEqualTo(1);
            final ConfigSubsection devel = config.section("branch").subsection("devel");
            assertThat(devel.getOptions().size()).isEqualTo(2);
            assertThat(devel.hasOption("remote")).isTrue();
            assertThat(devel.hasOption("merge")).isTrue();
            for (ConfigOption option : devel.getOptions()) {
                switch (option.getKey()) {
                    case "remote" -> assertThat(option.getValue()).isEqualTo("origin");
                    case "merge" -> assertThat(option.getValue()).isEqualTo("refs/heads/devel");
                    default -> throw new AssertionError("unexpected option: " + option.getKey());
                }
            }

            assertThat(config.hasSection("remote")).isTrue();
            assertThat(config.section("remote").getSubsections().size()).isEqualTo(1);
            final ConfigSubsection origin = config.section("remote").subsection("origin");
            assertThat(origin.getOptions().size()).isEqualTo(1);
            assertThat(origin.hasOption("url")).isTrue();
            for (ConfigOption option : origin.getOptions()) {
                if (option.getKey().equals("url")) {
                    assertThat(option.getValue()).isEqualTo("https://example.com/git");
                } else {
                    throw new AssertionError("unexpected option: " + option.getKey());
                }
            }

            final List<ConfigInclude> includes = config.getIncludes();
            assertThat(includes.size()).isEqualTo(9);
            for (ConfigInclude include : includes) {
                switch (include.getCondition()) {
                    case null -> assertThat(include.getPath()).isIn("/path/to/foo.inc", "foo.inc", "~/foo.inc");
                    case "gitdir:/path/to/foo/.git", "gitdir:~/to/group/" -> assertThat(include.getPath())
                            .isEqualTo("/path/to/foo.inc");
                    case "gitdir:/path/to/group/" -> assertThat(include.getPath())
                            .isIn("/path/to/foo.inc", "foo.inc");
                    case "onbranch:foo-branch", "hasconfig:remote.*.url:https://example.com/**" -> assertThat(
                                    include.getPath())
                            .isEqualTo("foo.inc");
                    default -> throw new AssertionError("Unexpected value: " + include.getCondition());
                }
            }
        }
        {
            // corner cases test
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/testParseConfigFile.2");
            final Config config = Config.create(file);
            assertThat(config.getSections().size()).isEqualTo(3);

            assertThat(config.hasSection("core")).isTrue();
            final ConfigSection core = config.section("core");
            assertThat(core.getOptions().size()).isEqualTo(3);
            assertThat(core.hasOption("filemode")).isTrue();
            for (ConfigOption option : core.getOptions()) {
                switch (option.getKey()) {
                    case "filemode" -> assertThat(option.getValue()).isEqualTo("false");
                    case "buttonoption", "buttonoptionagain" -> assertThat(option.getValue())
                            .isEqualTo("");
                    default -> throw new AssertionError("unexpected option: " + option.getKey());
                }
            }
            assertThat(config.hasSection("bar.baz")).isTrue();
            final ConfigSection barBaz = config.section("bar.baz");
            assertThat(barBaz.getOptions().size()).isEqualTo(2);
            assertThat(barBaz.hasOption("foo")).isTrue();
            for (ConfigOption option : barBaz.getOptions()) {
                switch (option.getKey()) {
                    case "foo" -> assertThat(option.getValue()).isEqualTo("bar");
                    case "url" -> assertThat(option.getValue()).isEqualTo("https://example.com/git");
                    default -> throw new AssertionError("unexpected option: " + option.getKey());
                }
            }

            assertThat(config.hasSection("url")).isTrue();
            final ConfigSubsection subsection = config.section("url").subsection("git@example.com:");
            assertThat(subsection.getOptions().size()).isEqualTo(1);
            final ConfigOption option = subsection.getOptions().getFirst();
            assertThat(option.getKey()).isEqualTo("insteadof");
            assertThat(option.getValue()).isEqualTo("https://example.com/");
        }
        {
            // illegal format 1
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/illegalConfigFile.1");
            assertThatThrownBy(() -> Config.create(file))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("malformed section name");
        }
        {
            // illegal format 2
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/illegalConfigFile.2");
            assertThatThrownBy(() -> Config.create(file))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("malformed variable names");
        }
        {
            // illegal format 3
            final File file = new File(TestUtils.testResourceDir(), "gitconfig/illegalConfigFile.3");
            assertThatThrownBy(() -> Config.create(file))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("malformed section name");
        }
    }
}
