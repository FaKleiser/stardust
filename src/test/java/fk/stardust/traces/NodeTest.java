/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.traces;

import org.testng.Assert;
import org.testng.annotations.Test;

import fk.stardust.test.data.SimpleSpectraProvider;

public class NodeTest {

    /**
     * Test data taken from Table 1 from:
     * 
     * Lee Naish, Hua Jie Lee, and Kotagiri Ramamohanarao. 2011. A model for spectra-based software diagnosis. ACM
     * Trans. Softw. Eng. Methodol. 20, 3, Article 11 (August 2011), 32 pages. DOI=10.1145/2000791.2000795
     * http://doi.acm.org/10.1145/2000791.2000795
     * 
     * @throws Exception
     * 
     * @see http://dl.acm.org/citation.cfm?id=2000795
     */
    @Test
    public void computeINFSMetricsForSimpleSpectra() throws Exception {
        final ISpectra<String> s = new SimpleSpectraProvider().loadSpectra();

        Assert.assertTrue(s.hasNode("S1"));
        Assert.assertTrue(s.hasNode("S2"));
        Assert.assertTrue(s.hasNode("S3"));

        Assert.assertEquals(s.getNode("S1").getNS(), 0);
        Assert.assertEquals(s.getNode("S1").getNF(), 1);
        Assert.assertEquals(s.getNode("S1").getIS(), 3);
        Assert.assertEquals(s.getNode("S1").getIF(), 1);

        Assert.assertEquals(s.getNode("S2").getNS(), 2);
        Assert.assertEquals(s.getNode("S2").getNF(), 0);
        Assert.assertEquals(s.getNode("S2").getIS(), 1);
        Assert.assertEquals(s.getNode("S2").getIF(), 2);

        Assert.assertEquals(s.getNode("S3").getNS(), 1);
        Assert.assertEquals(s.getNode("S3").getNF(), 1);
        Assert.assertEquals(s.getNode("S3").getIS(), 2);
        Assert.assertEquals(s.getNode("S3").getIF(), 1);
    }

    @Test
    public void computeForSpectraWithoutTraces() {
        final ISpectra<String> s = new Spectra<>();
        final INode<String> n = s.getNode("sampleNode");
        Assert.assertEquals(n.getNS(), 0);
        Assert.assertEquals(n.getNF(), 0);
        Assert.assertEquals(n.getIS(), 0);
        Assert.assertEquals(n.getIF(), 0);
    }
}
