/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.sbfl;

import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.Ranking;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

/**
 * Class is used to simplify the creation of spectrum based fault localizers.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public abstract class AbstractSpectrumBasedFaultLocalizer<T> implements IFaultLocalizer<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<T> localize(final ISpectra<T> spectra) {
        final Ranking<T> ranking = new Ranking<>();
        for (final INode<T> node : spectra.getNodes()) {
            final double suspiciousness = this.suspiciousness(node);
            ranking.rank(node, suspiciousness);
        }
        return ranking;
    }

    /**
     * Computes the suspiciousness of a single node.
     *
     * @param node
     *            the node to compute the suspiciousness of
     * @return the suspiciousness of the node
     */
    public abstract double suspiciousness(INode<T> node);

}
