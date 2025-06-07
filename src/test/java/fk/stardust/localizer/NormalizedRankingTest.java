/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fk.stardust.localizer.NormalizedRanking.NormalizationStrategy;
import fk.stardust.traces.ISpectra;
import fk.stardust.traces.IMutableTrace;
import fk.stardust.traces.Spectra;

public class NormalizedRankingTest {

    private ISpectra<String> data;

    @BeforeMethod
    public void before() {
        this.data = this.data();
    }

    @AfterMethod
    public void after() {
        this.data = null;
    }

    @Test
    public void getZeroOneNoModification() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), 0.0);
        ranking.rank(this.data.getNode("S2"), 0.2);
        ranking.rank(this.data.getNode("S3"), 0.3);
        ranking.rank(this.data.getNode("S4"), 1.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ZeroOne);
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(0.2d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.3d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getZeroOneDivideByTwo() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), 0.0);
        ranking.rank(this.data.getNode("S2"), 0.5);
        ranking.rank(this.data.getNode("S3"), 1.0);
        ranking.rank(this.data.getNode("S4"), 2.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ZeroOne);
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(0.25d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getZeroOneWithNegativeSusp() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), -1.0);
        ranking.rank(this.data.getNode("S2"), 0);
        ranking.rank(this.data.getNode("S3"), 0.5);
        ranking.rank(this.data.getNode("S4"), 1.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ZeroOne);
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.75d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getZeroOneWithInfinity() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.rank(this.data.getNode("S2"), 0);
        ranking.rank(this.data.getNode("S3"), 0.5);
        ranking.rank(this.data.getNode("S4"), Double.POSITIVE_INFINITY);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ZeroOne);
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getZeroOneWithMultipleInfinity() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.rank(this.data.getNode("S2"), Double.NEGATIVE_INFINITY);
        ranking.rank(this.data.getNode("S3"), 0);
        ranking.rank(this.data.getNode("S4"), 0.5);
        ranking.rank(this.data.getNode("S5"), Double.POSITIVE_INFINITY);
        ranking.rank(this.data.getNode("S6"), Double.POSITIVE_INFINITY);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ZeroOne);
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.0d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S5")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S6")));
    }

    @Test
    public void getReciprocalNoModification() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), 0.25);
        ranking.rank(this.data.getNode("S2"), 1.0d / 3.0d);
        ranking.rank(this.data.getNode("S3"), 0.5);
        ranking.rank(this.data.getNode("S4"), 1.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(1.0d / 3.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getReciprocalDivideByTwo() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), 0.0);
        ranking.rank(this.data.getNode("S2"), 0.5);
        ranking.rank(this.data.getNode("S3"), 1.0);
        ranking.rank(this.data.getNode("S4"), 2.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(1.0d / 3.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getReciprocalWithNegativeSusp() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), -1.0);
        ranking.rank(this.data.getNode("S2"), 0);
        ranking.rank(this.data.getNode("S3"), 0.5);
        ranking.rank(this.data.getNode("S4"), 1.0);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(1.0d / 3.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }

    @Test
    public void getReciprocalWithInfinity() {
        final Ranking<String> ranking = new Ranking<>();

        ranking.rank(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.rank(this.data.getNode("S2"), 0);
        ranking.rank(this.data.getNode("S3"), 0.5);
        ranking.rank(this.data.getNode("S4"), Double.POSITIVE_INFINITY);

        final NormalizedRanking<String> n = new NormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getSuspiciousness(this.data.getNode("S1")));
        Assert.assertEquals(1.0d / 3.0d, n.getSuspiciousness(this.data.getNode("S2")));
        Assert.assertEquals(0.5d, n.getSuspiciousness(this.data.getNode("S3")));
        Assert.assertEquals(1.0d, n.getSuspiciousness(this.data.getNode("S4")));
    }


    private ISpectra<String> data() {
        final Spectra<String> s = new Spectra<>();
        final IMutableTrace<String> t1 = s.addTrace(false);
        t1.setInvolvement("S1", true);
        t1.setInvolvement("S2", true);
        t1.setInvolvement("S3", true);
        t1.setInvolvement("S4", true);
        t1.setInvolvement("S5", true);
        t1.setInvolvement("S6", true);
        return s;
    }
}
