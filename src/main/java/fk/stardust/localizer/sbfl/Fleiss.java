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
 * Fleiss fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Fleiss<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Fleiss() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double enu1 = 4.0d * node.getIF() * node.getNS();
        final double enu2 = 4.0d * node.getNF() * node.getIS();
        final double enu3 = node.getNF() - node.getIS();
        final double enu = enu1 - enu2 - (enu3 * enu3);

        final double denom1 = 2.0d * node.getIF() + node.getNF() + node.getIS();
        final double denom2 = 2.0d * node.getNS() + node.getNF() + node.getIS();
        final double denom = denom1 + denom2;

        return enu / denom; // No new Double() was used here, direct division
    }

    @Override
    public String getName() {
        return "Fleiss";
    }

}
