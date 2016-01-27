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
 * Rogot2 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double frac1 = new Double(node.getIF()) / new Double(node.getIF() + node.getIS());
        final double frac2 = new Double(node.getIF()) / new Double(node.getIF() + node.getNF());
        final double frac3 = new Double(node.getNS()) / new Double(node.getNS() + node.getIS());
        final double frac4 = new Double(node.getNS()) / new Double(node.getNS() + node.getNF());
        return 0.25d * (frac1 + frac2 + frac3 + frac4);
    }

    @Override
    public String getName() {
        return "Rogot2";
    }

}
