/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.sbfl;

import fk.stardust.traces.INode;

/**
 * HarmonicMean fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class HarmonicMean<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public HarmonicMean() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = node.getIF() * node.getNS() - node.getNF() * node.getIS();
        final double enu21 = (node.getIF() + node.getIS()) * (node.getNS() + node.getNF());
        final double enu22 = (node.getIF() + node.getNF()) * (node.getIS() + node.getNS());
        final double enu = enu1 * (enu21 + enu22);

        final double denom1 = node.getIF() + node.getIS();
        final double denom2 = node.getNS() + node.getNF();
        final double denom3 = node.getIF() + node.getNF();
        final double denom4 = node.getIS() + node.getNS();
        final double denom = denom1 * denom2 * denom3 * denom4;

        return enu / denom;
    }

    @Override
    public String getName() {
        return "HarmonicMean";
    }

}
