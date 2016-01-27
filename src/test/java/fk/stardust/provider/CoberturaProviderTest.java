/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.provider;

import org.testng.Assert;
import org.testng.annotations.Test;

import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;
import fk.stardust.traces.ITrace;

public class CoberturaProviderTest {

    @Test
    public void loadSimpleCoverage() throws Exception {
        final CoberturaProvider c = new CoberturaProvider();
        c.addTraceFile("src/test/resources/fk/stardust/provider/simple-coverage.xml", true);
        final ISpectra<String> s = c.loadSpectra();

        // assert loaded count is correct
        Assert.assertEquals(s.getNodes().size(), 3);
        Assert.assertEquals(s.getTraces().size(), 1);

        // assert we have nodes
        Assert.assertTrue(s.hasNode("cobertura/CoverageTest.java:3"));
        Assert.assertTrue(s.hasNode("cobertura/CoverageTest.java:9"));
        Assert.assertTrue(s.hasNode("cobertura/CoverageTest.java:10"));

        // assert trace has correct involvement loaded
        final ITrace<String> t = s.getTraces().get(0);
        Assert.assertFalse(t.isInvolved(s.getNode("cobertura/CoverageTest.java:3")));
        Assert.assertTrue(t.isInvolved(s.getNode("cobertura/CoverageTest.java:9")));
        Assert.assertTrue(t.isInvolved(s.getNode("cobertura/CoverageTest.java:10")));
    }

    @Test
    public void loadLargeCoverage() throws Exception {
        final CoberturaProvider c = new CoberturaProvider();
        c.addTraceFile("src/test/resources/fk/stardust/provider/large-coverage.xml", true);
        final ISpectra<String> s = c.loadSpectra();

        // assert loaded count is correct
        Assert.assertEquals(s.getNodes().size(), 16246);
        Assert.assertEquals(s.getTraces().size(), 1);

        // assert we have 3563 involved nodes
        // (match count of the regex 'hits="[^0]' on large-coverage.xml divided by 2, as all hits are mentioned twice)
        int count = 0;
        final ITrace<String> t = s.getTraces().get(0);
        for (final INode<String> node : s.getNodes()) {
            if (t.isInvolved(node)) {
                count++;
            }
        }
        Assert.assertEquals(count, 3563);
    }
}
