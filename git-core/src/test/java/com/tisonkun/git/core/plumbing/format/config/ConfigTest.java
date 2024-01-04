package com.tisonkun.git.core.plumbing.format.config;

import static org.assertj.core.api.Assertions.assertThat;
import com.tisonkun.git.core.test.TestUtils;
import java.io.File;
import org.junit.jupiter.api.Test;

class ConfigTest {
    @Test
    public void testParseConfigFile() throws Exception {
        final File file = new File(TestUtils.testResourceDir(), "gitconfig/testParseConfigFile");
        final Config config = Config.create(file);
        assertThat(config.hasSection("core")).isTrue();
        assertThat(config.section("core").hasOption("gitProxy")).isTrue();
        assertThat(config.section("branch").subsection("devel").hasOption("remote")).isTrue();
    }
}
