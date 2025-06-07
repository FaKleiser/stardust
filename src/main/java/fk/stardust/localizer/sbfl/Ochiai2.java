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
 * Ochiai2 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ochiai2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Ochiai2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double denom1 = node.getIF() + node.getIS();
        final double denom2 = node.getNS() + node.getNF();
        final double denom3 = node.getIF() + node.getNF();
        final double denom4 = node.getIS() + node.getNS();
        return (double) (node.getIF() * node.getNS()) / Math.sqrt(denom1 * denom2 * denom3 * denom4);
    }

    @Override
    public String getName() {
        return "Ochiai2";
    }

}
