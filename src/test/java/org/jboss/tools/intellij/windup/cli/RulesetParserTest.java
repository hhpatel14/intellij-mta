package org.jboss.tools.intellij.windup.cli;
import org.jboss.tools.intellij.windup.model.KantraConfiguration;
import junit.framework.TestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class RulesetParserTest extends TestCase {
        public void testParseRuleset() {
            String testFilePath = "src/test/java/org/jboss/tools/intellij/windup/cli/test.yaml";
            List<KantraConfiguration.Ruleset> result = RulesetParser.parseRuleset(testFilePath);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            for (KantraConfiguration.Ruleset ruleset : result) {
                assertTrue(ruleset.getSkipped().isEmpty());
            }
            assertEquals(3,result.size());
        }

    }
