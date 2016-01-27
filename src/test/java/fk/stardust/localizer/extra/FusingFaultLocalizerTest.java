/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.extra;

import org.testng.annotations.Test;

import fk.stardust.localizer.NormalizedRanking.NormalizationStrategy;
import fk.stardust.localizer.Ranking;
import fk.stardust.localizer.extra.FusingFaultLocalizer.DataFusionTechnique;
import fk.stardust.localizer.extra.FusingFaultLocalizer.SelectionTechnique;
import fk.stardust.test.data.SimpleSpectraProvider;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

public class FusingFaultLocalizerTest {

    @Test
    public void selectOverlapBased() throws Exception {
        final SimpleSpectraProvider t = new SimpleSpectraProvider();
        final ISpectra<String> s = t.loadSpectra();
        final FusingFaultLocalizer<String> f = new FusingFaultLocalizer<>(NormalizationStrategy.ZeroOne,
                SelectionTechnique.OVERLAP_RATE, DataFusionTechnique.COMB_ANZ);
        final Ranking<String> r = f.localize(s);
        for (final INode<String> n : r) {
            System.out.println(String.format("Node %s: %f", n.getIdentifier(), r.getSuspiciousness(n)));
        }
    }
}
