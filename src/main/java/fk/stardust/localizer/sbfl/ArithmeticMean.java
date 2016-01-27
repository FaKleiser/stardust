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
 * ArithmeticMean fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class ArithmeticMean<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public ArithmeticMean() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = 2 * node.getIF() * node.getNS();
        final double enu2 = 2 * node.getNF() * node.getIS();
        final double enu = enu1 - enu2;

        final double denom1 = (node.getIF() + node.getIS()) * (node.getNS() + node.getNF());
        final double denom2 = (node.getIF() + node.getNF()) * (node.getIS() + node.getNS());
        final double denom = denom1 + denom2;

        return enu / denom;
    }

    @Override
    public String getName() {
        return "ArithmeticMean";
    }

}
