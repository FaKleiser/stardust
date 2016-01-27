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
 * Overlap fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Overlap<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Overlap() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return new Double(node.getIF()) / new Double(Math.min(node.getIF(), Math.min(node.getNF(), node.getIS())));
    }

    @Override
    public String getName() {
        return "Overlap";
    }

}
